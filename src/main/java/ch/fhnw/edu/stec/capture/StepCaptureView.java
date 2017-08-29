package ch.fhnw.edu.stec.capture;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import static ch.fhnw.edu.stec.util.Labels.*;

public final class StepCaptureView extends HBox{

    public StepCaptureView(StepCaptureController controller) {

        setSpacing(5);
        setPadding(new Insets(5));

        TextField tagNameField = new TextField();
        tagNameField.setEditable(true);
        tagNameField.setPromptText(STEP_NAME_PROMPT);

        TextField descriptionField = new TextField();
        descriptionField.setEditable(true);
        descriptionField.setPromptText(STEP_DESCRIPTION_PROMPT);

        Button captureButton = new Button(STEP_CAPTURE_BUTTON_LABEL);
        captureButton.setOnAction(e -> {
            controller.captureStep(tagNameField.getText(), descriptionField.getText());
        });

        HBox.setHgrow(tagNameField, Priority.ALWAYS);
        HBox.setHgrow(descriptionField, Priority.ALWAYS);
        getChildren().addAll(tagNameField, descriptionField, captureButton);


    }

}
