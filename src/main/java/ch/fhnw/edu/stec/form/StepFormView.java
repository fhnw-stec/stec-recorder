package ch.fhnw.edu.stec.form;

import ch.fhnw.edu.stec.model.InteractionMode;
import ch.fhnw.edu.stec.model.Step;
import ch.fhnw.edu.stec.notification.NotificationController;
import ch.fhnw.edu.stec.util.AsciidoctorRenderer;
import ch.fhnw.edu.stec.util.Glyphs;
import ch.fhnw.edu.stec.util.Labels;
import io.vavr.control.Option;
import io.vavr.control.Try;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import org.controlsfx.glyphfont.FontAwesome;

import static ch.fhnw.edu.stec.util.Labels.*;

public final class StepFormView extends VBox {

    private static final int BUTTON_PANE_HEIGHT = 200;

    public StepFormView(StepFormModel model,
                        StepFormController formController,
                        NotificationController notificationController) {

        setSpacing(5);
        setPadding(new Insets(5));

        TextField titleTextField = new TextField();
        titleTextField.setEditable(true);
        titleTextField.setPromptText(STEP_TITLE_PROMPT);
        titleTextField.textProperty().bindBidirectional(model.titleProperty());

        TextArea descriptionField = new TextArea();
        descriptionField.setEditable(true);
        descriptionField.setWrapText(true);
        descriptionField.setPrefRowCount(3);
        descriptionField.setPromptText(STEP_DESCRIPTION_PROMPT);
        descriptionField.textProperty().bindBidirectional(model.descriptionProperty());

        SplitPane editor = createDescriptionEditor(descriptionField);

        Node buttonPane = createButtonPane(model, formController, notificationController, titleTextField, descriptionField);

        VBox.setVgrow(editor, Priority.ALWAYS);

        getChildren().addAll(titleTextField, editor, buttonPane);

    }

    private static SplitPane createDescriptionEditor(TextArea descriptionField) {
        WebView preview = new WebView();

        // Creating the renderer is expensive -> must happen outside of listener
        AsciidoctorRenderer renderer = new AsciidoctorRenderer();
        descriptionField.textProperty().addListener(cl -> {
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

    private Pane createButtonPane(StepFormModel model, StepFormController captureController, NotificationController notificationController, TextField titleTextField, TextArea descriptionField) {
        Button captureButton = createCaptureButton(captureController, notificationController, titleTextField, descriptionField);

        HBox pane = new HBox(captureButton);
        pane.setPrefHeight(BUTTON_PANE_HEIGHT);
        HBox.setHgrow(captureButton, Priority.ALWAYS);

        Pane editButtonPane = createEditButtonButtonPane(model, captureController, notificationController, titleTextField, descriptionField);
        HBox.setHgrow(editButtonPane, Priority.ALWAYS);

        model.interactionModeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue instanceof InteractionMode.Capture) {
                pane.getChildren().setAll(captureButton);
            } else {
                pane.getChildren().setAll(editButtonPane);
            }
        });

        return pane;
    }

    private Pane createEditButtonButtonPane(StepFormModel model, StepFormController formController, NotificationController notificationController, TextField titleTextField, TextArea descriptionField) {
        Button saveButton = new Button(Labels.SAVE_BUTTON_LABEL, Glyphs.FONT_AWESOME.create(FontAwesome.Glyph.SAVE));
        saveButton.setOnAction(e -> {
            String tag = model.interactionModeProperty().get().getTag();
            Try<String> result = formController.saveStep(tag, titleTextField.getText(), descriptionField.getText());
            result.onSuccess(notificationController::notifyInfo);
            result.onFailure(t -> notificationController.notifyError(Labels.SAVE_FAILED, t));
        });

        Button resetButton = new Button(Labels.RESET_BUTTON_LABEL, Glyphs.FONT_AWESOME.create(FontAwesome.Glyph.UNDO));
        resetButton.setOnAction(e -> {
            String tag = model.interactionModeProperty().get().getTag();
            Option<Step> stepOption = model.getStepByTag(tag);
            stepOption.forEach(step -> {
                titleTextField.textProperty().setValue(step.getTitle());
                descriptionField.textProperty().setValue(step.getDescription());
            });
        });

        HBox box = new HBox(5, resetButton, saveButton);
        box.setAlignment(Pos.TOP_RIGHT);
        return box;
    }

    private Button createCaptureButton(StepFormController formController, NotificationController notificationController, TextField titleTextField, TextArea descriptionField) {
        Button captureButton = new Button(CAPTURE_BUTTON_LABEL, Glyphs.FONT_AWESOME.create(FontAwesome.Glyph.CAMERA));
        captureButton.setMaxHeight(Double.MAX_VALUE);
        captureButton.setMaxWidth(Double.MAX_VALUE);

        captureButton.setOnAction(e -> {
            Try<String> result = formController.captureStep(titleTextField.getText(), descriptionField.getText());
            result.onSuccess(notificationController::notifyInfo);
            result.onFailure(t -> notificationController.notifyError(Labels.CAPTURE_FAILED, t));
        });

        BooleanBinding isInputComplete = titleTextField.textProperty().isEmpty().or(descriptionField.textProperty().isEmpty());
        captureButton.disableProperty().bind(isInputComplete);

        return captureButton;
    }

}