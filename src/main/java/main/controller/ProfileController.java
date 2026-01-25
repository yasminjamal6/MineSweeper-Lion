package main.controller;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.util.ResourceUtils;
import model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class ProfileController {

    // LEFT (viewer)
    @FXML private ImageView avatarView;
    @FXML private Label playerNameLabel;
    @FXML private Label playerSubtitleLabel;
    @FXML private Label coinsLabel;
    @FXML private Label selectedAvatarLabel;
    @FXML private Button shopBtn;

    // STATS
    @FXML private Label totalGamesLabel;
    @FXML private Label winsLossesLabel;
    @FXML private Label winRateLabel;

    // AVATARS
    @FXML private FlowPane avatarsFlow;
    @FXML private Label avatarsHintLabel;

    @FXML private Label giftProgressLabel;
    @FXML private ProgressBar giftProgressBar;
    @FXML private Button openGiftBtn;
    @FXML private ImageView giftImageView;

    // RIGHT (Teammate)
    @FXML private ImageView avatarViewRight;
    @FXML private Label playerNameLabelRight;
    @FXML private Label playerSubtitleLabelRight;
    @FXML private Label coinsLabelRight;
    @FXML private Label selectedAvatarLabelRight;
    @FXML private Label totalGamesLabelRight;
    @FXML private Label winsLossesLabelRight;
    @FXML private Label winRateLabelRight;
    @FXML private FlowPane avatarsFlowRight;
    @FXML private Label avatarsHintLabelRight;
    @FXML private Label giftProgressLabelRight;
    @FXML private ProgressBar giftProgressBarRight;
    @FXML private Button openGiftBtnRight;
    @FXML private ImageView giftImageViewRight;

    // TABLE
    @FXML private TableView<MatchRecord> matchesTable;
    @FXML private TableColumn<MatchRecord, String> playedAtCol;
    @FXML private TableColumn<MatchRecord, String> TeammateCol;
    @FXML private TableColumn<MatchRecord, String> resultCol;
    @FXML private TableColumn<MatchRecord, Integer> scoreCol;
    @FXML private TableColumn<MatchRecord, Long> durationCol;
    @FXML private TableColumn<MatchRecord, Integer> boardSizeCol;
    @FXML private TableColumn<MatchRecord, String> difficultyCol;


    private final ObservableList<MatchRecord> matches = FXCollections.observableArrayList();
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private PlayerProfile viewerProfile;     // השחקן המחובר (Session)
    private PlayerProfile TeammateProfile;   // יריב אחרון מהיסטוריה

    @FXML
    private void initialize() {
        setupTable();
        javafx.application.Platform.runLater(() -> {
            loadViewerAndTeammate();
            loadGiftIcon(giftImageView);
            loadGiftIcon(giftImageViewRight);
        });

    }

    private void setupTable() {
        playedAtCol.setCellValueFactory(cell -> {
            String iso = cell.getValue().getPlayedAtIso();
            return new SimpleStringProperty(formatPlayedAt(iso));
        });
        TeammateCol.setCellValueFactory(new PropertyValueFactory<>("Teammate"));
        resultCol.setCellValueFactory(new PropertyValueFactory<>("result"));
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
        durationCol.setCellValueFactory(new PropertyValueFactory<>("durationSeconds"));
        boardSizeCol.setCellValueFactory(new PropertyValueFactory<>("boardSize"));
        difficultyCol.setCellValueFactory(new PropertyValueFactory<>("difficulty"));

        matchesTable.setItems(matches);
    }

    private void loadViewerAndTeammate() {
        String viewerName = Session.getActivePlayerName();
        viewerProfile = ProfileStore.loadOrCreate(viewerName);
        if (viewerProfile != null) {
            viewerProfile.ensureDefaults();
        }

        List<MatchRecord> viewerHistory = getHistoryFor(viewerName);
        String TeammateName = findLastTeammate(viewerHistory);

        TeammateProfile = findProfileByName(TeammateName);
        if (TeammateProfile != null) {
            TeammateProfile.ensureDefaults();
        }

        renderViewerPanel(viewerProfile, viewerHistory);
        renderTeammatePanel(TeammateProfile, TeammateName);
        loadMatchesTable(viewerHistory, TeammateName);
    }

    private List<MatchRecord> getHistoryFor(String playerName) {
        if (playerName == null || playerName.isBlank()) return List.of();
        PlayerProfile historyProfile = PlayerProfileManager.getProfiles().stream()
                .filter(p -> playerName.equals(p.getPlayerName()))
                .findFirst()
                .orElse(null);
        return (historyProfile != null) ? historyProfile.getMatches() : List.of();
    }

    private String findLastTeammate(List<MatchRecord> history) {
        if (history == null || history.isEmpty()) return null;
        String Teammate = history.get(0).getTeammate();
        return (Teammate == null || Teammate.isBlank()) ? null : Teammate;
    }

    private PlayerProfile findProfileByName(String name) {
        if (name == null || name.isBlank()) return null;
        return PlayerProfileManager.getProfiles().stream()
                .filter(p -> name.equals(p.getPlayerName()))
                .findFirst()
                .orElse(null);
    }

    private void renderOwnedAvatars(PlayerProfile profile, FlowPane target, boolean allowSelect) {
        target.getChildren().clear();

        profile.ensureDefaults();

        profile.getOwnedAvatars().stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(avatarId -> target.getChildren().add(createAvatarCard(profile, avatarId, allowSelect)));
    }


    private Button createAvatarCard(PlayerProfile profile, String avatarId, boolean allowSelect) {
        Button btn = new Button();
        btn.setPrefSize(90, 90);
        btn.getStyleClass().add("pf-avatar-card");

        ShopAvatarItem item = ShopCatalog.findAvatarById(avatarId);

        if (item != null) {
            ImageView iv = loadImage(item.getImagePath());
            if (iv != null) btn.setGraphic(iv);
            else btn.setText(item.getName());
            btn.setTooltip(new Tooltip(item.getName()));
        } else {
            btn.setText(avatarId);
        }

        if (avatarId.equals(profile.getSelectedAvatarId())) {
            btn.getStyleClass().add("pf-avatar-selected");
        }

        if (allowSelect) {
            btn.setOnAction(e -> {
                if (profile.selectAvatar(avatarId)) {
                    ProfileStore.save(profile);

                    // Keep PlayerProfileManager in sync so avatars render correctly across screens.
                    PlayerProfileManager.getOrCreateProfile(profile.getPlayerName(), profile.getSelectedAvatarId());

                    viewerProfile = ProfileStore.loadOrCreate(profile.getPlayerName());
                    renderProfileAreaFor(viewerProfile, coinsLabel, selectedAvatarLabel, avatarsHintLabel, avatarsFlow, shopBtn, true);
                    setAvatar(avatarView, viewerProfile.getAvatarId());
                }
            });
        } else {
            btn.setDisable(true);
        }

        return btn;
    }

    private ImageView loadImage(String path) {
        try (var is = ResourceUtils.stream(getClass(), path)) {
            if (is == null) return null;
            Image img = new Image(is);
            ImageView iv = new ImageView(img);
            iv.setFitWidth(60);
            iv.setFitHeight(60);
            iv.setPreserveRatio(true);
            return iv;
        } catch (Exception e) {
            return null;
        }
    }

    private void setAvatar(ImageView target, String avatarId) {
        Avatar avatar = Avatar.fromId(avatarId, Avatar.SIMBA);
        try (var is = ResourceUtils.stream(getClass(), avatar.resourcePath)) {
            if (is != null) {
                target.setImage(new Image(is));
                target.setVisible(true);
            } else {
                target.setImage(null);
                target.setVisible(false);
            }
        } catch (Exception e) {
            target.setImage(null);
            target.setVisible(false);
        }
    }

    private String formatPlayedAt(String iso) {
        if (iso == null || iso.isBlank()) return "";
        try {
            return LocalDateTime.parse(iso).format(DISPLAY_TIME);
        } catch (Exception e) {
            return iso;
        }
    }

    private void updateStats(List<MatchRecord> records, Label totalLabel, Label winsLosses, Label winRate) {
        int total = (records != null) ? records.size() : 0;
        int wins = 0, losses = 0;

        if (records != null) {
            for (MatchRecord r : records) {
                if ("Win".equalsIgnoreCase(r.getResult())) wins++;
                else if ("Loss".equalsIgnoreCase(r.getResult())) losses++;
            }
        }

        totalLabel.setText(String.valueOf(total));
        winsLosses.setText(wins + " / " + losses);
        int rate = (total == 0) ? 0 : (int) Math.round((wins * 100.0) / total);
        winRate.setText(rate + "%");
    }

    // ================= NAV =================
    @FXML
    private void onOpenShop(ActionEvent event) {
        // The shop loads the active session player, so keep it in sync.
        if (viewerProfile != null) {
            Session.setActivePlayerName(viewerProfile.getPlayerName());
        }
        switchScene(event, "/view/shopView.fxml");
    }


    @FXML
    private void onBackHome(ActionEvent event) {
        switchScene(event, "/view/home-view.fxml");
    }

    private void renderProfileAreaFor(PlayerProfile p,
                                      Label coins,
                                      Label selectedAvatar,
                                      Label avatarsHint,
                                      FlowPane avatarsPane,
                                      Button shopButton,
                                      boolean allowSelect) {
        if (p == null) return;

        p.ensureDefaults();

        coins.setText("Coins: " + p.getCoins());
        selectedAvatar.setText("Selected Avatar: " + p.getSelectedAvatarId());
        avatarsHint.setText("");

        renderOwnedAvatars(p, avatarsPane, allowSelect);
        if (shopButton != null) shopButton.setDisable(!allowSelect);
    }

    private void renderViewerPanel(PlayerProfile p, List<MatchRecord> history) {
        if (p == null) return;
        playerNameLabel.setText(p.getPlayerName());
        playerSubtitleLabel.setText("Your Profile");
        setAvatar(avatarView, p.getAvatarId());

        updateStats(history, totalGamesLabel, winsLossesLabel, winRateLabel);
        renderProfileAreaFor(p, coinsLabel, selectedAvatarLabel, avatarsHintLabel, avatarsFlow, shopBtn, true);
        refreshGiftArea(p, giftProgressLabel, giftProgressBar, openGiftBtn, true);
    }

    private void renderTeammatePanel(PlayerProfile p, String TeammateName) {
        if (TeammateName == null || TeammateName.isBlank() || p == null) {
            playerNameLabelRight.setText("No Teammate yet");
            playerSubtitleLabelRight.setText("Teammate Profile");
            avatarViewRight.setImage(null);
            avatarViewRight.setVisible(false);
            coinsLabelRight.setText("Coins: 0");
            selectedAvatarLabelRight.setText("Selected Avatar: -");
            avatarsHintLabelRight.setText("");
            avatarsFlowRight.getChildren().clear();
            totalGamesLabelRight.setText("0");
            winsLossesLabelRight.setText("0 / 0");
            winRateLabelRight.setText("0%");
            refreshGiftArea(null, giftProgressLabelRight, giftProgressBarRight, openGiftBtnRight, false);
            return;
        }

        playerNameLabelRight.setText(p.getPlayerName());
        playerSubtitleLabelRight.setText("Teammate Profile");
        setAvatar(avatarViewRight, p.getAvatarId());

        List<MatchRecord> history = getHistoryFor(p.getPlayerName());
        updateStats(history, totalGamesLabelRight, winsLossesLabelRight, winRateLabelRight);
        renderProfileAreaFor(p, coinsLabelRight, selectedAvatarLabelRight, avatarsHintLabelRight, avatarsFlowRight, null, false);
        refreshGiftArea(p, giftProgressLabelRight, giftProgressBarRight, openGiftBtnRight, false);
    }

    private void loadMatchesTable(List<MatchRecord> viewerHistory, String TeammateName) {
        if (viewerHistory == null) {
            matches.setAll(List.of());
            return;
        }
        if (TeammateName != null && !TeammateName.isBlank()) {
            List<MatchRecord> filtered = viewerHistory.stream()
                    .filter(r -> TeammateName.equals(r.getTeammate()))
                    .collect(java.util.stream.Collectors.toList());
            if (!filtered.isEmpty()) {
                matches.setAll(filtered);
                return;
            }
        }
        matches.setAll(viewerHistory);
    }

    private void switchScene(ActionEvent event, String fxml) {
        try {
            Parent root = FXMLLoader.load(ResourceUtils.url(getClass(), fxml));
            Scene scene = ((Node) event.getSource()).getScene();
            root.setOpacity(0);
            scene.setRoot(root);

            FadeTransition ft = new FadeTransition(Duration.millis(250), root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void refreshGiftArea(PlayerProfile p,
                                 Label progressLabel,
                                 ProgressBar progressBar,
                                 Button openGift,
                                 boolean allowOpen) {
        if (progressLabel == null || progressBar == null || openGift == null) return;
        if (p == null) {
            progressLabel.setText("Wins until gift: 0/3");
            progressBar.setProgress(0);
            openGift.setDisable(true);
            openGift.setText("🔒 Not ready");
            return;
        }

        p.ensureDefaults();

        int wins = p.getWinsSinceGift();
        boolean ready = p.isGiftReady();

        int shown = Math.min(3, Math.max(0, wins));

        progressLabel.setText("Wins until gift: " + shown + "/3");
        progressBar.setProgress(shown / 3.0);
        if (allowOpen) {
            openGift.setDisable(!ready);
            openGift.setText(ready ? "🎁 Open Gift" : "🔒 Not ready");
        } else {
            openGift.setDisable(true);
            openGift.setText("🔒 Not ready");
        }
    }

    @FXML
    private void onOpenGiftInProfile() {
        PlayerProfile p = viewerProfile;
        if (p == null) return;

        p.ensureDefaults();
        if (!p.isGiftReady()) return;

        giveRandomGiftTo(p); // Same reward logic as HomeController.

        p.consumeGift();     // Reset winsSinceGift after redeeming.
        ProfileStore.save(p);
        renderProfileAreaFor(p, coinsLabel, selectedAvatarLabel, avatarsHintLabel, avatarsFlow, shopBtn, true);
        refreshGiftArea(p, giftProgressLabel, giftProgressBar, openGiftBtn, true);
    }
    private void giveRandomGiftTo(PlayerProfile p) {
        int roll = java.util.concurrent.ThreadLocalRandom.current().nextInt(100);

        if (roll < 65) {
            int coins = java.util.concurrent.ThreadLocalRandom.current().nextInt(5, 26);
            p.addCoins(coins);
            ProfileStore.save(p);

            showGiftPopup("🎁 Gift opened!", "💰", "You won coins!", "You won " + coins + " coins 🪙");
        } else {
            String[] emojiIds = {"FIRE", "SMILE", "CROWN", "BOOM", "LION"};
            String pick = emojiIds[java.util.concurrent.ThreadLocalRandom.current().nextInt(emojiIds.length)];
            int amount = java.util.concurrent.ThreadLocalRandom.current().nextInt(1, 4);

            p.addEmoji(pick, amount);
            ProfileStore.save(p);

            String emojiChar = emojiToChar(pick);

            showGiftPopup("🎁 Gift opened!", emojiChar, "New emoji unlocked!", "You got " + amount + "x " + emojiChar);
        }
    }
    private String emojiToChar(String id) {
        return switch (id) {
            case "FIRE" -> "🔥";
            case "SMILE" -> "😄";
            case "CROWN" -> "👑";
            case "BOOM" -> "💥";
            case "LION" -> "🦁";
            default -> "✨";
        };
    }
    private void showGiftPopup(String title, String bigIcon, String rewardTitle, String desc) {
        try {
            var url = ResourceUtils.url(getClass(), "/view/gift-popup.fxml");
            if (url == null) return;

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            String css = ResourceUtils.externalForm(getClass(), "/css/gift-popup.css");
            if (css != null) root.getStylesheets().add(css);

            GiftPopupController c = loader.getController();
            c.setData(title, bigIcon, rewardTitle, desc);

            Stage stage = new Stage();
            stage.initOwner(playerNameLabel.getScene().getWindow());
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private void loadGiftIcon(ImageView target) {
        try (var is = ResourceUtils.stream(getClass(), "/images/gift.png")) {
            if (is != null) {
                target.setImage(new Image(is));
                target.setVisible(true);
            } else {
                target.setVisible(false);
            }
        } catch (Exception e) {
            target.setVisible(false);
        }
    }



}
