package ch.fhnw.edu.stec.capture;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import static ch.fhnw.edu.stec.util.Labels.*;

public final class StepCaptureView extends VBox {

    public StepCaptureView(StepCaptureController controller) {

        setSpacing(5);
        setPadding(new Insets(5, 0, 5, 5));

        TextField titleTextField = new TextField();
        titleTextField.setEditable(true);
        titleTextField.setPromptText(STEP_TITLE_PROMPT);

        TextArea descriptionField = new TextArea();
        descriptionField.setEditable(true);
        descriptionField.setWrapText(true);
        descriptionField.setPrefRowCount(3);
        descriptionField.setPromptText(STEP_DESCRIPTION_PROMPT);

        Button captureButton = new Button(STEP_CAPTURE_BUTTON_LABEL);
        captureButton.setMaxWidth(Double.MAX_VALUE);

        // TODO: User notification & form reset
        captureButton.setOnAction(e -> controller.captureStep(titleTextField.getText(), descriptionField.getText()));

        VBox.setVgrow(descriptionField, Priority.ALWAYS);
        VBox.setVgrow(captureButton, Priority.ALWAYS);

        getChildren().addAll(titleTextField, descriptionField, captureButton);

    }

}