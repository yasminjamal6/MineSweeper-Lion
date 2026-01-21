package main.controller;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;
import main.util.ResourceUtils;
import model.PlayerProfile;
import model.ProfileStore;
import model.Session;
import model.ShopAvatarItem;
import model.ShopCatalog;

public class ProfileController {

    @FXML private Label playerNameLabel;
    @FXML private Label coinsLabel;
    @FXML private Label selectedAvatarLabel;
    @FXML private FlowPane avatarsFlow;

    private PlayerProfile profile;

    // ================= INIT =================
    @FXML
    private void initialize() {
        String playerName = Session.getActivePlayerName();
        profile = ProfileStore.loadOrCreate(playerName);
        refreshUI();
    }

    // ================= UI =================
    private void refreshUI() {
        playerNameLabel.setText("Player: " + profile.getPlayerName());
        coinsLabel.setText("Coins: " + profile.getCoins());
        selectedAvatarLabel.setText("Selected Avatar: " + profile.getSelectedAvatarId());
        renderOwnedAvatars();
    }

    private void renderOwnedAvatars() {
        avatarsFlow.getChildren().clear();

        for (String avatarId : profile.getOwnedAvatars()) {
            avatarsFlow.getChildren().add(createAvatarCard(avatarId));
        }
    }

    private Button createAvatarCard(String avatarId) {
        Button btn = new Button();
        btn.setPrefSize(90, 90);
        btn.getStyleClass().add("pf-avatar-card");

        ShopAvatarItem item = ShopCatalog.findAvatarById(avatarId);

        if (item != null) {
            ImageView iv = loadImage(item.getImagePath());
            if (iv != null) {
                btn.setGraphic(iv);
            } else {
                btn.setText(item.getName());
            }
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
                refreshUI();
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

    // ================= NAV =================
    @FXML
    private void onOpenShop(ActionEvent event) {
        switchScene(event, "/view/shopView.fxml");
    }

    @FXML
    private void onBackHome(ActionEvent event) {
        switchScene(event, "/view/home-view.fxml");
    }

    private void switchScene(ActionEvent event, String fxml) {
        try {
            Parent root = FXMLLoader.load(ResourceUtils.url(getClass(), fxml));
            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);

            FadeTransition ft = new FadeTransition(Duration.millis(250), root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
