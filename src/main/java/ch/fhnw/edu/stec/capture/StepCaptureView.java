package ch.fhnw.edu.stec.capture;

import ch.fhnw.edu.stec.notification.NotificationController;
import io.vavr.control.Try;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import static ch.fhnw.edu.stec.util.Labels.*;

public final class StepCaptureView extends VBox {

    public StepCaptureView(StepCaptureController captureController, NotificationController notificationController) {

        setSpacing(5);
        setPadding(new Insets(5));

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

        captureButton.setOnAction(e -> {
            Try<String> result = captureController.captureStep(titleTextField.getText(), descriptionField.getText());

            result.onSuccess(msg -> {
                notificationController.notifyInfo(msg);
                titleTextField.setText("");
                descriptionField.setText("");
            });

            result.onFailure(t -> notificationController.notifyError("Capturing step failed", t));

        });

        BooleanBinding isInputComplete = titleTextField.textProperty().isEmpty().or(descriptionField.textProperty().isEmpty());
        captureButton.disableProperty().bind(isInputComplete);

        VBox.setVgrow(descriptionField, Priority.ALWAYS);

        getChildren().addAll(titleTextField, descriptionField, captureButton);

    }

}