package ch.fhnw.edu.stec.capture;

import ch.fhnw.edu.stec.notification.NotificationController;
import ch.fhnw.edu.stec.util.AsciidoctorRenderer;
import ch.fhnw.edu.stec.util.Labels;
import io.vavr.control.Try;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;

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

        SplitPane editor = createDescriptionEditor(descriptionField);

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

        VBox.setVgrow(editor, Priority.ALWAYS);

        getChildren().addAll(titleTextField, editor, captureButton);

    }

    private static SplitPane createDescriptionEditor(TextArea descriptionField) {
        WebView preview = new WebView();

        // Creating the renderer is expensive -> must happen outside of listener
        AsciidoctorRenderer renderer = new AsciidoctorRenderer();
        descriptionField.setOnKeyReleased(e -> {
            String html = renderer.renderToHtml(descriptionField.getText());
            preview.getEngine().loadContent(html);
        });

        Label previewOverlayLabel = new Label(Labels.PREVIEW);
        previewOverlayLabel.setTextFill(Color.LIGHTGRAY);
        HBox previewOverlay = new HBox(previewOverlayLabel);
        previewOverlay.setPadding(new Insets(5));
        previewOverlay.setAlignment(Pos.BOTTOM_RIGHT);

        SplitPane editor = new SplitPane(descriptionField, new StackPane(preview, previewOverlay));
        editor.setOrientation(Orientation.HORIZONTAL);
        return editor;
    }

}