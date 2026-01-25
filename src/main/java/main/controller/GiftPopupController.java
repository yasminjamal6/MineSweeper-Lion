package main.controller;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GiftPopupController {

    @FXML private Label titleLabel;
    @FXML private Label bigIconLabel;
    @FXML private Label rewardTitle;
    @FXML private Label rewardDesc;

    @FXML
    private void initialize() {
        // Nice entrance: pop + fade (animation is applied after setData is called)
        var root = titleLabel.getScene() == null ? null : titleLabel.getScene().getRoot();
    }

    public void setData(String title, String bigIcon, String rewardTitleText, String desc) {
        titleLabel.setText(title);
        bigIconLabel.setText(bigIcon);
        rewardTitle.setText(rewardTitleText);
        rewardDesc.setText(desc);

        // Animate after data has been set
        var card = titleLabel.getParent().getParent(); // VBox card
        card.setOpacity(0);
        card.setScaleX(0.96);
        card.setScaleY(0.96);

        FadeTransition ft = new FadeTransition(Duration.millis(180), card);
        ft.setFromValue(0);
        ft.setToValue(1);

        ScaleTransition st = new ScaleTransition(Duration.millis(220), card);
        st.setFromX(0.96); st.setFromY(0.96);
        st.setToX(1.0);   st.setToY(1.0);

        ft.play();
        st.play();
    }

    /**
     * Closes the popup window.
     */
    @FXML
    private void onClose() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
}
