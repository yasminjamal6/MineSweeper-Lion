package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import main.util.ResourceUtils;

public class MainFx extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Load the home screen FXML
        var fxmlUrl = ResourceUtils.url(MainFx.class, "/view/home-view.fxml");
        if (fxmlUrl == null) {
            return;
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);

        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        String css = ResourceUtils.externalForm(MainFx.class, "/css/home.css");
        if (css != null) {
            scene.getStylesheets().add(css);
        }
        stage.setTitle("Mine Sweeper Lion");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
