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
import javafx.util.Duration;
import main.util.ResourceUtils;
import model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.stage.Stage;


public class ProfileController {

    // LEFT
    @FXML private ListView<String> profilesList;

    // TOP CARD
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

    // TABLE
    @FXML private TableView<MatchRecord> matchesTable;
    @FXML private TableColumn<MatchRecord, String> playedAtCol;
    @FXML private TableColumn<MatchRecord, String> opponentCol;
    @FXML private TableColumn<MatchRecord, String> resultCol;
    @FXML private TableColumn<MatchRecord, Integer> scoreCol;
    @FXML private TableColumn<MatchRecord, Long> durationCol;
    @FXML private TableColumn<MatchRecord, Integer> boardSizeCol;
    @FXML private TableColumn<MatchRecord, String> difficultyCol;
    @FXML private Label giftProgressLabel;
    @FXML private ProgressBar giftProgressBar;
    @FXML private Button openGiftBtn;
    @FXML private ImageView giftImageView;


    private final ObservableList<MatchRecord> matches = FXCollections.observableArrayList();
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private PlayerProfile activeProfile;     // השחקן המחובר (Session)
    private PlayerProfile selectedProfile;   // השחקן שנבחר מהרשימה

    @FXML
    private void initialize() {
        setupTable();
        loadActiveProfile();
        loadProfilesList();
        loadGiftIcon();

    }

    private void setupTable() {
        playedAtCol.setCellValueFactory(cell -> {
            String iso = cell.getValue().getPlayedAtIso();
            return new SimpleStringProperty(formatPlayedAt(iso));
        });
        opponentCol.setCellValueFactory(new PropertyValueFactory<>("opponent"));
        resultCol.setCellValueFactory(new PropertyValueFactory<>("result"));
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
        durationCol.setCellValueFactory(new PropertyValueFactory<>("durationSeconds"));
        boardSizeCol.setCellValueFactory(new PropertyValueFactory<>("boardSize"));
        difficultyCol.setCellValueFactory(new PropertyValueFactory<>("difficulty"));

        matchesTable.setItems(matches);
    }

    private void loadActiveProfile() {
        String playerName = Session.getActivePlayerName();
        activeProfile = ProfileStore.loadOrCreate(playerName);
        if (activeProfile != null) {
            activeProfile.ensureDefaults();
        }
    }

    private void loadProfilesList() {
        List<PlayerProfile> profiles = PlayerProfileManager.getProfiles();

        profilesList.getItems().clear();
        for (PlayerProfile p : profiles) {
            profilesList.getItems().add(p.getPlayerName());
        }

        profilesList.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldV, newV) -> selectProfile(newV));

        // ברירת מחדל: לבחור את השחקן הפעיל אם הוא קיים ברשימה, אחרת הראשון
        if (activeProfile != null && profilesList.getItems().contains(activeProfile.getPlayerName())) {
            profilesList.getSelectionModel().select(activeProfile.getPlayerName());
        } else if (!profilesList.getItems().isEmpty()) {
            profilesList.getSelectionModel().select(0);
        }
    }

    private void selectProfile(String playerName) {
        if (playerName == null || playerName.isBlank()) return;

        selectedProfile = ProfileStore.loadOrCreate(playerName);

        if (selectedProfile == null) return;

        // כותרת + אווטאר + טבלה + סטטיסטיקות
        playerNameLabel.setText(selectedProfile.getPlayerName());
        playerSubtitleLabel.setText("Profile & Match History");
        setAvatar(selectedProfile.getAvatarId());

        // ✅ ההיסטוריה מגיעה מה-Manager
        PlayerProfile historyProfile = PlayerProfileManager.getProfiles().stream()
                .filter(p -> playerName.equals(p.getPlayerName()))
                .findFirst()
                .orElse(null);

        List<MatchRecord> history = (historyProfile != null) ? historyProfile.getMatches() : List.of();
        matches.setAll(history);
        updateStats(history);

        renderProfileAreaFor(selectedProfile);
        refreshGiftArea(selectedProfile);



    }

    private void renderOwnedAvatars(PlayerProfile profile) {
        avatarsFlow.getChildren().clear();

        profile.ensureDefaults();

        profile.getOwnedAvatars().stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(avatarId -> avatarsFlow.getChildren().add(createAvatarCard(profile, avatarId)));
    }


    private Button createAvatarCard(PlayerProfile profile, String avatarId) {
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

        btn.setOnAction(e -> {
            if (profile.selectAvatar(avatarId)) {
                ProfileStore.save(profile);

                // גם לשמור ב-PlayerProfileManager כדי שהאווטאר יוצג נכון במסך עצמו
                PlayerProfileManager.getOrCreateProfile(profile.getPlayerName(), profile.getSelectedAvatarId());

                selectedProfile = ProfileStore.loadOrCreate(profile.getPlayerName());
                renderProfileAreaFor(selectedProfile);
                setAvatar(selectedProfile.getAvatarId());

            }
        });

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

    private void setAvatar(String avatarId) {
        Avatar avatar = Avatar.fromId(avatarId, Avatar.SIMBA);
        try (var is = ResourceUtils.stream(getClass(), avatar.resourcePath)) {
            if (is != null) {
                avatarView.setImage(new Image(is));
                avatarView.setVisible(true);
            } else {
                avatarView.setImage(null);
                avatarView.setVisible(false);
            }
        } catch (Exception e) {
            avatarView.setImage(null);
            avatarView.setVisible(false);
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

    private void updateStats(List<MatchRecord> records) {
        int total = (records != null) ? records.size() : 0;
        int wins = 0, losses = 0;

        if (records != null) {
            for (MatchRecord r : records) {
                if ("Win".equalsIgnoreCase(r.getResult())) wins++;
                else if ("Loss".equalsIgnoreCase(r.getResult())) losses++;
            }
        }

        totalGamesLabel.setText(String.valueOf(total));
        winsLossesLabel.setText(wins + " / " + losses);
        int rate = (total == 0) ? 0 : (int) Math.round((wins * 100.0) / total);
        winRateLabel.setText(rate + "%");
    }

    // ================= NAV =================
    @FXML
    private void onOpenShop(ActionEvent event) {
        // חשוב: החנות תטען לפי Session, אז מעדכנים אותו לפי השחקן שנבחר
        if (selectedProfile != null) {
            Session.setActivePlayerName(selectedProfile.getPlayerName());
        }
        switchScene(event, "/view/shopView.fxml");
    }


    @FXML
    private void onBackHome(ActionEvent event) {
        switchScene(event, "/view/home-view.fxml");
    }

    private void renderProfileAreaFor(model.PlayerProfile p) {
        if (p == null) return;

        p.ensureDefaults();

        coinsLabel.setText("Coins: " + p.getCoins());
        selectedAvatarLabel.setText("Selected Avatar: " + p.getSelectedAvatarId());
        avatarsHintLabel.setText("");

        renderOwnedAvatars(p);
        if (shopBtn != null) shopBtn.setDisable(false);
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
    private void refreshGiftArea(PlayerProfile p) {
        if (p == null) return;

        p.ensureDefaults(); // אם יש לך

        int wins = p.getWinsSinceGift();     // 0..3
        boolean ready = p.isGiftReady();     // wins >= 3

        int shown = Math.min(3, Math.max(0, wins));

        if (giftProgressLabel != null) {
            giftProgressLabel.setText("Wins until gift: " + shown + "/3");
        }
        if (giftProgressBar != null) {
            giftProgressBar.setProgress(shown / 3.0);
        }
        if (openGiftBtn != null) {
            openGiftBtn.setDisable(!ready);
            openGiftBtn.setText(ready ? "🎁 Open Gift" : "🔒 Not ready");
        }
    }

    @FXML
    private void onOpenGiftInProfile() {
        PlayerProfile p = selectedProfile;
        if (p == null) return;

        p.ensureDefaults();
        if (!p.isGiftReady()) return;

        giveRandomGiftTo(p); // אותה לוגיקה כמו ב-HomeController

        p.consumeGift();     // מאפס winsSinceGift ל-0
        ProfileStore.save(p);
        renderProfileAreaFor(p);

        refreshGiftArea(p);
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
    private void loadGiftIcon() {
        try (var is = ResourceUtils.stream(getClass(), "/images/gift.png")) {
            if (is != null) {
                giftImageView.setImage(new Image(is));
                giftImageView.setVisible(true);
            } else {
                giftImageView.setVisible(false);
            }
        } catch (Exception e) {
            giftImageView.setVisible(false);
        }
    }



}
