package main.controller;

import javafx.fxml.FXML;

public class HomeController {

    @FXML
    private void onStartAdventure() {
        System.out.println("Start Adventure clicked");
        // TODO: navigation to setup screen
    }

    @FXML
    private void onHistory() {
        System.out.println("History clicked");
    }

    @FXML
    private void onManageQuestions() {
        System.out.println("Question Manager clicked");
    }
}
