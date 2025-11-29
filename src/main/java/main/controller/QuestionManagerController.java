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
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;





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
        // Set up table columns (כבר יש אצלך)
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

        // 🔹 טעינה מה־CSV לרשימה
        questionTable.setItems(FXCollections.observableArrayList(questionBank.getAllQuestions()));
        System.out.println("✅ Loaded " + questionBank.getAllQuestions().size() + " questions from CSV.");

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
                    private final HBox pane = new HBox(5, editBtn, deleteBtn);

                    {
                        editBtn.setOnAction(e -> {
                            Question oldQ = getTableView().getItems().get(getIndex());
                            Question updatedQ = openQuestionDialog(oldQ);
                            if (updatedQ != null) {
                                // עדכון בבנק (כולל CSV)
                                questionBank.updateQuestion(oldQ, updatedQ);

                                // עדכון בטבלה
                                getTableView().getItems().set(getIndex(), updatedQ);
                                getTableView().refresh();

                                System.out.println("✏ Updated question: " + updatedQ.getText());
                            }
                        });

                        deleteBtn.setOnAction(e -> {
                            Question q = getTableView().getItems().get(getIndex());

                            Alert confirm = new Alert(
                                    Alert.AlertType.CONFIRMATION,
                                    "Delete this question?\n\n" + q.getText(),
                                    ButtonType.YES, ButtonType.NO
                            );

                            Optional<ButtonType> res = confirm.showAndWait();
                            if (res.isPresent() && res.get() == ButtonType.YES) {
                                // מחיקה ממודל השאלות + CSV
                                questionBank.removeQuestion(q);

                                // מחיקה מהטבלה
                                getTableView().getItems().remove(q);
                                getTableView().refresh();

                                System.out.println("🗑 Deleted question: " + q.getText());
                            }
                        });

                        editBtn.getStyleClass().add("edit-btn");
                        deleteBtn.getStyleClass().add("delete-btn");
                    }

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
        Question newQ = openQuestionDialog(null);
        if (newQ != null) {
            // מוסיפים לבנק השאלות (וזה שומר ל־CSV)
            questionBank.addQuestion(newQ);

            // מוסיפים גם לטבלה
            questionTable.getItems().add(newQ);
            questionTable.refresh();

            System.out.println("✅ Added new question: " + newQ.getText());
        }
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
    private Question openQuestionDialog(Question original) {
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle(original == null ? "Add Question" : "Edit Question");
        dialog.setHeaderText(null); // נשתמש ב-header שלנו מה-UI

        DialogPane dialogPane = dialog.getDialogPane();

        // נטען את ה-CSS של העמוד שאליו כבר מחובר
        dialogPane.getStylesheets().add(
                getClass().getResource("/css/question-manager.css").toExternalForm()
        );
        // נוסיף סטייל לדיאלוג עצמו (רקע גרדיאנט, עיגול קצוות וכו')
        dialogPane.getStyleClass().add("qd-dialog-pane");

        ButtonType okButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // ===== בניית ה-UI של החלון =====

        // Header למעלה
        HBox header = new HBox(10);
        header.getStyleClass().add("qd-header");

        StackPane iconWrapper = new StackPane();
        iconWrapper.getStyleClass().add("qd-header-icon");
        Label iconLabel = new Label(original == null ? "➕" : "✏");
        iconLabel.getStyleClass().add("qd-header-icon-emoji");
        iconWrapper.getChildren().add(iconLabel);

        VBox headerTexts = new VBox(2);
        Label titleLbl = new Label(original == null ? "Add Question" : "Edit Question");
        titleLbl.getStyleClass().add("qd-header-title");
        Label subLbl = new Label("Fill in the trivia question details");
        subLbl.getStyleClass().add("qd-header-subtitle");
        headerTexts.getChildren().addAll(titleLbl, subLbl);

        header.getChildren().addAll(iconWrapper, headerTexts);

        // טופס השדות (GridPane)
        GridPane grid = new GridPane();
        grid.getStyleClass().add("qd-form-grid");
        grid.setPadding(new Insets(10, 0, 0, 0));

        TextArea txtQuestion = new TextArea();
        txtQuestion.setPrefRowCount(3);
        txtQuestion.getStyleClass().addAll("qd-textarea");

        TextField txtA = new TextField();
        TextField txtB = new TextField();
        TextField txtC = new TextField();
        TextField txtD = new TextField();
        txtA.getStyleClass().add("qd-textfield");
        txtB.getStyleClass().add("qd-textfield");
        txtC.getStyleClass().add("qd-textfield");
        txtD.getStyleClass().add("qd-textfield");

        ChoiceBox<Integer> cbCorrect = new ChoiceBox<>();
        cbCorrect.getItems().addAll(1, 2, 3, 4);
        cbCorrect.getStyleClass().add("qd-choicebox");

        ChoiceBox<QuestionLevel> cbLevel = new ChoiceBox<>();
        cbLevel.getItems().addAll(QuestionLevel.values());
        cbLevel.getStyleClass().add("qd-choicebox");

        // לייבלים
        Label lblQ = new Label("Question:");
        Label lblA = new Label("Answer A:");
        Label lblB = new Label("Answer B:");
        Label lblC = new Label("Answer C:");
        Label lblD = new Label("Answer D:");
        Label lblCorrect = new Label("Correct answer (1-4):");
        Label lblLevel = new Label("Difficulty:");

        lblQ.getStyleClass().add("qd-label");
        lblA.getStyleClass().add("qd-label");
        lblB.getStyleClass().add("qd-label");
        lblC.getStyleClass().add("qd-label");
        lblD.getStyleClass().add("qd-label");
        lblCorrect.getStyleClass().add("qd-label");
        lblLevel.getStyleClass().add("qd-label");

        // הוספה לגריד
        int row = 0;
        grid.add(lblQ, 0, row);
        grid.add(txtQuestion, 1, row++, 2, 1);

        grid.add(lblA, 0, row);
        grid.add(txtA, 1, row++);

        grid.add(lblB, 0, row);
        grid.add(txtB, 1, row++);

        grid.add(lblC, 0, row);
        grid.add(txtC, 1, row++);

        grid.add(lblD, 0, row);
        grid.add(txtD, 1, row++);

        grid.add(lblCorrect, 0, row);
        grid.add(cbCorrect, 1, row++);

        grid.add(lblLevel, 0, row);
        grid.add(cbLevel, 1, row);

        // כרטיס פנימי
        VBox card = new VBox(14);
        card.getStyleClass().add("qd-card");
        card.getChildren().addAll(grid);

        // root של הדיאלוג
        VBox root = new VBox(16);
        root.getStyleClass().add("qd-root");
        root.getChildren().addAll(header, card);

        dialogPane.setContent(root);

        // ===== ערכי ברירת מחדל (אם זה עריכה) =====
        if (original != null) {
            txtQuestion.setText(original.getText());
            String[] opts = original.getOptions();
            txtA.setText(opts[0]);
            txtB.setText(opts[1]);
            txtC.setText(opts[2]);
            txtD.setText(opts[3]);
            cbCorrect.setValue(original.getCorrectIndex() + 1);
            cbLevel.setValue(original.getLevel());
        } else {
            cbCorrect.setValue(1);
            cbLevel.setValue(QuestionLevel.EASY);
        }

        // ===== סטייל לכפתורים (Save / Cancel) =====
        // חייב לקרוא אחרי setContent ואחרי הוספת הכפתורים
        dialogPane.lookupButton(okButtonType).getStyleClass().add("gh-btn-primary");
        dialogPane.lookupButton(ButtonType.CANCEL).getStyleClass().add("gh-btn-secondary");

        // ===== המרה של תוצאת הדיאלוג לאובייקט Question =====
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                if (txtQuestion.getText().isBlank() ||
                        txtA.getText().isBlank() ||
                        txtB.getText().isBlank() ||
                        txtC.getText().isBlank() ||
                        txtD.getText().isBlank() ||
                        cbCorrect.getValue() == null ||
                        cbLevel.getValue() == null) {

                    Alert alert = new Alert(Alert.AlertType.ERROR,
                            "Please fill all fields", ButtonType.OK);
                    alert.showAndWait();
                    return null;
                }

                String[] opts = new String[4];
                opts[0] = txtA.getText().trim();
                opts[1] = txtB.getText().trim();
                opts[2] = txtC.getText().trim();
                opts[3] = txtD.getText().trim();

                int correctIndex = cbCorrect.getValue() - 1; // 1–4 -> 0–3

                return new Question(
                        txtQuestion.getText().trim(),
                        opts,
                        correctIndex,
                        cbLevel.getValue()
                );
            }
            return null;
        });

        Optional<Question> result = dialog.showAndWait();
        return result.orElse(null);
    }



}
