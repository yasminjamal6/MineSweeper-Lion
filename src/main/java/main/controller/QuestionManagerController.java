package main.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import model.Question;
import model.QuestionLevel;
import model.QuestionBank;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;


public class QuestionManagerController {

    @FXML private TableView<Question> questionTable;
    @FXML private TableColumn<Question, String> colQuestion;
    @FXML private TableColumn<Question, String> colCorrect;
    @FXML private TableColumn<Question, QuestionLevel> colDifficulty;
    @FXML private TableColumn<Question, String> colAnswers;
    @FXML private TableColumn<Question, Void> colActions;
    @FXML private HBox headerRow;     // top header row
    @FXML private StackPane tableCard; // wrapper around the table card


    private final QuestionBank questionBank = new QuestionBank();

    @FXML
    private void initialize() {
        // Set up table columns
        colQuestion.setCellValueFactory(new PropertyValueFactory<>("text"));
        colDifficulty.setCellValueFactory(new PropertyValueFactory<>("level"));

        colCorrect.setCellValueFactory(q -> new ReadOnlyStringWrapper(
                q.getValue().getOptions()[q.getValue().getCorrectIndex()]
        ));

        colAnswers.setCellValueFactory(q -> {
            String[] opts = q.getValue().getOptions();
            return new ReadOnlyStringWrapper(
                    String.format("A: %s | B: %s | C: %s | D: %s",
                            opts[0], opts[1], opts[2], opts[3])
            );
        });

        addActionButtons();

        // Load all questions from CSV automatically
        questionTable.getItems().setAll(questionBank.getAllQuestions());
        System.out.println("✅ Loaded " + questionBank.getAllQuestions().size() + " questions from CSV.");
        // Play entrance animations for header + table card
        playEntranceAnimations();
    }

    /**
     * Adds edit and delete buttons to the 'Actions' column.
     */
    private void addActionButtons() {
        colActions.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Question, Void> call(final TableColumn<Question, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("✎");
                    private final Button deleteBtn = new Button("🗑");

                    {
                        editBtn.setOnAction(e -> {
                            Question q = getTableView().getItems().get(getIndex());
                            System.out.println("Edit question: " + q.getText());
                        });

                        deleteBtn.setOnAction(e -> {
                            Question q = getTableView().getItems().get(getIndex());
                            questionTable.getItems().remove(q);
                            System.out.println("Deleted question: " + q.getText());
                        });

                        editBtn.getStyleClass().add("edit-btn");
                        deleteBtn.getStyleClass().add("delete-btn");
                    }

                    private final HBox pane = new HBox(5, editBtn, deleteBtn);

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
            }
        });
    }

    /**
     * Handles going back to home screen.
     */
    @FXML
    private void onBack(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/home-view.fxml"));
            Parent root = loader.load();
            ((Node)e.getSource()).getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Temporary placeholder for add question button.
     */
    @FXML
    private void onAddQuestion(ActionEvent e) {
        System.out.println(">> Add question clicked (to be implemented later)");
    }
    /**
     * Smooth entrance animations for the header and the table card.
     */
    private void playEntranceAnimations() {
        if (headerRow == null || tableCard == null) {
            return;
        }

        // Initial state: invisible and slightly shifted
        headerRow.setOpacity(0);
        tableCard.setOpacity(0);

        headerRow.setTranslateY(-30);
        tableCard.setTranslateY(40);

        // Header: fade + move down
        FadeTransition headerFade = new FadeTransition(Duration.millis(500), headerRow);
        headerFade.setFromValue(0);
        headerFade.setToValue(1);

        TranslateTransition headerMove = new TranslateTransition(Duration.millis(500), headerRow);
        headerMove.setFromY(-30);
        headerMove.setToY(0);
        headerMove.setInterpolator(Interpolator.EASE_OUT);

        // Table card: fade + move up (with slight delay)
        FadeTransition tableFade = new FadeTransition(Duration.millis(600), tableCard);
        tableFade.setFromValue(0);
        tableFade.setToValue(1);
        tableFade.setDelay(Duration.millis(200));

        TranslateTransition tableMove = new TranslateTransition(Duration.millis(600), tableCard);
        tableMove.setFromY(40);
        tableMove.setToY(0);
        tableMove.setInterpolator(Interpolator.EASE_OUT);
        tableMove.setDelay(Duration.millis(200));

        ParallelTransition all = new ParallelTransition(
                headerFade, headerMove,
                tableFade, tableMove
        );

        all.play();
    }

}
