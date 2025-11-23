package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainFx extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Load the home screen FXML
        FXMLLoader fxmlLoader = new FXMLLoader(
                MainFx.class.getResource("/view/home-view.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        scene.getStylesheets().add(
                MainFx.class.getResource("/css/home.css").toExternalForm()
        );
        stage.setTitle("Mine Sweeper Lion");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
