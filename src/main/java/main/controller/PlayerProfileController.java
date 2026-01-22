package main.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import main.util.ResourceUtils;
import model.Avatar;
import model.MatchRecord;
import model.PlayerProfile;
import model.PlayerProfileManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PlayerProfileController {

    @FXML private ListView<String> profilesList;
    @FXML private ImageView avatarView;
    @FXML private Label playerNameLabel;
    @FXML private Label playerSubtitleLabel;
    @FXML private Label totalGamesLabel;
    @FXML private Label winsLossesLabel;
    @FXML private Label winRateLabel;

    @FXML private TableView<MatchRecord> matchesTable;
    @FXML private TableColumn<MatchRecord, String> playedAtCol;
    @FXML private TableColumn<MatchRecord, String> opponentCol;
    @FXML private TableColumn<MatchRecord, String> resultCol;
    @FXML private TableColumn<MatchRecord, Integer> scoreCol;
    @FXML private TableColumn<MatchRecord, Long> durationCol;
    @FXML private TableColumn<MatchRecord, Integer> boardSizeCol;
    @FXML private TableColumn<MatchRecord, String> difficultyCol;

    private final ObservableList<MatchRecord> matches = FXCollections.observableArrayList();
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private void initialize() {
        setupTable();
        loadProfiles();
    }

    private void setupTable() {
        playedAtCol.setCellValueFactory(cell -> {
            String iso = cell.getValue().getPlayedAtIso();
            return new javafx.beans.property.SimpleStringProperty(formatPlayedAt(iso));
        });
        opponentCol.setCellValueFactory(new PropertyValueFactory<>("opponent"));
        resultCol.setCellValueFactory(new PropertyValueFactory<>("result"));
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
        durationCol.setCellValueFactory(new PropertyValueFactory<>("durationSeconds"));
        boardSizeCol.setCellValueFactory(new PropertyValueFactory<>("boardSize"));
        difficultyCol.setCellValueFactory(new PropertyValueFactory<>("difficulty"));

        matchesTable.setItems(matches);
    }

    private void loadProfiles() {
        List<PlayerProfile> profiles = PlayerProfileManager.getProfiles();
        profilesList.getItems().clear();
        for (PlayerProfile profile : profiles) {
            profilesList.getItems().add(profile.getPlayerName());
        }
        profilesList.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldV, newV) -> updateProfileView(newV));
        if (!profilesList.getItems().isEmpty()) {
            profilesList.getSelectionModel().select(0);
            updateProfileView(profilesList.getSelectionModel().getSelectedItem());
        }
    }

    private void updateProfileView(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return;
        }
        PlayerProfile profile = PlayerProfileManager.getProfiles().stream()
                .filter(p -> playerName.equals(p.getPlayerName()))
                .findFirst()
                .orElse(null);
        if (profile == null) {
            return;
        }
        playerNameLabel.setText(profile.getPlayerName());
        playerSubtitleLabel.setText("Match History");
        setAvatar(profile.getAvatarId());
        matches.setAll(profile.getMatches());
        updateStats(profile.getMatches());
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
        if (iso == null || iso.isBlank()) {
            return "";
        }
        try {
            return LocalDateTime.parse(iso).format(DISPLAY_TIME);
        } catch (Exception e) {
            return iso;
        }
    }

    private void updateStats(List<MatchRecord> records) {
        int total = records != null ? records.size() : 0;
        int wins = 0;
        int losses = 0;
        if (records != null) {
            for (MatchRecord record : records) {
                if ("Win".equalsIgnoreCase(record.getResult())) {
                    wins++;
                } else if ("Loss".equalsIgnoreCase(record.getResult())) {
                    losses++;
                }
            }
        }
        totalGamesLabel.setText(String.valueOf(total));
        winsLossesLabel.setText(wins + " / " + losses);
        int rate = total == 0 ? 0 : (int) Math.round((wins * 100.0) / total);
        winRateLabel.setText(rate + "%");
    }

    @FXML
    private void onBack() {
        try {
            var url = ResourceUtils.url(getClass(), "/view/home-view.fxml");
            if (url == null) {
                return;
            }
            Parent root = javafx.fxml.FXMLLoader.load(url);
            Scene scene = ((Node) playerNameLabel).getScene();
            root.setOpacity(0);
            scene.setRoot(root);
            SettingsController.applyThemeToRoot(root);

            FadeTransition ft = new FadeTransition(Duration.millis(250), root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
