package main.controller;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.util.ResourceUtils;
import model.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ShopController {

    public enum StoreSource {
        HOME,
        PROFILE
    }

    // ----- FXML -----
    @FXML private Label lblCoins;
    @FXML private Label lblPlayerName;

    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> sortChoice;

    @FXML private TilePane tileItems;

    @FXML private Label lblSelectedName;
    @FXML private Label lblSelectedPrice;
    @FXML private Label lblSelectedStatus;

    @FXML private Button btnBuy;
    @FXML private Button btnEquip;

    @FXML private Label lblMessage;

    // ----- DATA -----
    private final ShopService shopService = new ShopService();
    private PlayerProfile profile;
    private ShopAvatarItem selectedItem;
    private StoreSource storeSource = StoreSource.HOME;

    @FXML
    private void initialize() {
        // If there is a root in FXML, theme can be applied there as well.
        // Here the theme is applied after loading the new root, so no fx:id is required.


        String name = Session.getActivePlayerName();
        if (name == null || name.isBlank()) {
            name = GameSetupController.selectedPlayerAName;
        }
        if (name == null || name.isBlank()) {
            name = "Player";
        }
        profile = ProfileStore.loadOrCreate(name);



        // UI init
        if (sortChoice != null) {
            sortChoice.getItems().setAll("Default", "Price: Low → High", "Price: High → Low", "Name: A → Z");
            sortChoice.setValue("Default");
            sortChoice.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> refreshGrid());
        }

        if (searchField != null) {
            searchField.textProperty().addListener((obs, o, n) -> refreshGrid());
        }

        refreshTopBar();
        resetPreview();
        refreshGrid();
    }

    // ---------------- UI HELPERS ----------------

    private void refreshTopBar() {
        if (profile == null) return;
        if (lblCoins != null) lblCoins.setText(String.valueOf(profile.getCoins()));
        if (lblPlayerName != null) lblPlayerName.setText(profile.getPlayerName());
    }

    private void resetPreview() {
        selectedItem = null;

        if (lblSelectedName != null) lblSelectedName.setText("Select an item");
        if (lblSelectedPrice != null) lblSelectedPrice.setText("");
        if (lblSelectedStatus != null) lblSelectedStatus.setText("");

        if (btnBuy != null) btnBuy.setDisable(true);
        if (btnEquip != null) btnEquip.setDisable(true);

        if (lblMessage != null) lblMessage.setText("");
    }

    private void refreshGrid() {
        if (tileItems == null) return;

        tileItems.getChildren().clear();

        List<ShopAvatarItem> items = shopService.getAllAvatars();

        // Search
        String q = (searchField == null || searchField.getText() == null) ? "" : searchField.getText().trim().toLowerCase();
        if (!q.isEmpty()) {
            items = items.stream()
                    .filter(it -> it.getName().toLowerCase().contains(q) || it.getId().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }

        // Sort
        String sort = (sortChoice == null) ? "Default" : sortChoice.getValue();
        if ("Price: Low → High".equals(sort)) {
            items = items.stream().sorted(Comparator.comparingInt(ShopAvatarItem::getPrice)).collect(Collectors.toList());
        } else if ("Price: High → Low".equals(sort)) {
            items = items.stream().sorted((a,b) -> Integer.compare(b.getPrice(), a.getPrice())).collect(Collectors.toList());
        } else if ("Name: A → Z".equals(sort)) {
            items = items.stream().sorted(Comparator.comparing(ShopAvatarItem::getName, String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());
        }

        for (ShopAvatarItem item : items) {
            tileItems.getChildren().add(createCard(item));
        }

        // Keep the preview in sync if an item was already selected.
        if (selectedItem != null) {
            setSelectedItem(selectedItem);
        }
    }

    private VBox createCard(ShopAvatarItem item) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("shop-item-card");

        // Image (optional)
        ImageView iv = buildAvatarImage(item);
        if (iv != null) {
            iv.getStyleClass().add("shop-item-image");
            card.getChildren().add(iv);
        }

        Label name = new Label(item.getName());
        name.getStyleClass().add("shop-item-name");

        Label price = new Label(item.getPrice() == 0 ? "Free" : ("💰 " + item.getPrice()));
        price.getStyleClass().add("shop-item-price");

        Label status = new Label();
        status.getStyleClass().add("shop-item-status");

        boolean owned = profile != null && profile.ownsAvatar(item.getId());
        boolean equipped = profile != null &&
                item.getId() != null &&
                item.getId().equalsIgnoreCase(profile.getSelectedAvatarId());

        if (equipped) status.setText("⭐ Equipped");
        else if (owned) status.setText("✅ Owned");
        else status.setText("🛒 Locked");

        card.getChildren().addAll(name, price, status);

        card.setOnMouseClicked(e -> setSelectedItem(item));
        return card;
    }

    private ImageView buildAvatarImage(ShopAvatarItem item) {
        try (var is = ResourceUtils.stream(getClass(), item.getImagePath())) {
            if (is == null) return null;
            Image img = new Image(is);
            ImageView iv = new ImageView(img);
            iv.setFitWidth(90);
            iv.setFitHeight(90);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            return iv;
        } catch (Exception e) {
            return null;
        }
    }

    private void setSelectedItem(ShopAvatarItem item) {
        selectedItem = item;
        if (lblMessage != null) lblMessage.setText("");

        if (item == null) {
            resetPreview();
            return;
        }

        if (lblSelectedName != null) lblSelectedName.setText(item.getName());
        if (lblSelectedPrice != null) lblSelectedPrice.setText(item.getPrice() == 0 ? "Free" : ("💰 Price: " + item.getPrice()));

        boolean owned = profile != null && profile.ownsAvatar(item.getId());
        boolean equipped = profile != null &&
                item.getId() != null &&
                item.getId().equalsIgnoreCase(profile.getSelectedAvatarId());

        if (lblSelectedStatus != null) {
            if (equipped) lblSelectedStatus.setText("⭐ Currently Equipped");
            else if (owned) lblSelectedStatus.setText("✅ Owned (You can equip it)");
            else lblSelectedStatus.setText("🛒 Not owned (Buy to unlock)");
        }

        if (btnBuy != null) btnBuy.setDisable(owned);
        if (btnEquip != null) btnEquip.setDisable(!owned || equipped);
    }

    // ---------------- ACTIONS ----------------

    @FXML
    private void onBuy(ActionEvent event) {
        if (profile == null || selectedItem == null) return;

        PurchaseResult result = shopService.purchaseAvatar(profile, selectedItem.getId());

        if (result == PurchaseResult.SUCCESS) {
            ProfileStore.save(profile);

            // Keep the in-memory manager in sync so ProfileController updates immediately.
            PlayerProfileManager.upsertProfileData(profile);
        }

        if (lblMessage != null) {
            switch (result) {
                case SUCCESS -> lblMessage.setText("✅ Purchased!");
                case ALREADY_OWNED -> lblMessage.setText("You already own this item.");
                case NOT_ENOUGH_COINS -> lblMessage.setText("Not enough coins.");
                case ITEM_NOT_FOUND -> lblMessage.setText("Item not found.");
            }
        }

        refreshTopBar();
        refreshGrid();
        setSelectedItem(selectedItem);

    }

    @FXML
    private void onEquip(ActionEvent event) {
        if (profile == null || selectedItem == null) return;

        boolean ok = profile != null && profile.selectAvatar(selectedItem.getId());
        if (ok) {
            ProfileStore.save(profile);
        }
        if (lblMessage != null) lblMessage.setText(ok ? "⭐ Equipped!" : "Can't equip this item.");

        refreshGrid();
        setSelectedItem(selectedItem);
    }

    @FXML
    private void onBack(ActionEvent event) {
        if (storeSource == StoreSource.PROFILE) {
            switchSceneWithFade(event, "/view/profile-view.fxml");
        } else {
            switchSceneWithFade(event, "/view/home-view.fxml");
        }
    }

    public void setStoreSource(StoreSource storeSource) {
        if (storeSource != null) {
            this.storeSource = storeSource;
        }
    }

    // Helper aligned with GameSetupController's transition style.
    private void switchSceneWithFade(ActionEvent event, String fxmlPath) {
        try {
            var url = ResourceUtils.url(getClass(), fxmlPath);
            if (url == null) return;

            Parent newRoot = new FXMLLoader(url).load();

            // Apply theme and language settings like other screens.
            SettingsController.applyThemeToRoot(newRoot);
            SettingsController.refreshLanguageOnAllWindows();

            Scene scene = ((Node) event.getSource()).getScene();
            newRoot.setOpacity(0);
            scene.setRoot(newRoot);

            FadeTransition ft = new FadeTransition(Duration.millis(250), newRoot);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

            Stage stage = (Stage) scene.getWindow();
            stage.sizeToScene();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
