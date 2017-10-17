package ch.fhnw.edu.stec.history;

import ch.fhnw.edu.stec.model.CaptureMode;
import ch.fhnw.edu.stec.model.Step;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;

public interface StepHistoryModel {

    ObservableList<Step> getSteps();

    ObjectProperty<CaptureMode> captureModeProperty();

    default boolean isBeingEdited(Step step) {
        return (step != null) &&
                (captureModeProperty().get() instanceof CaptureMode.Edit) &&
                ((CaptureMode.Edit) captureModeProperty().get()).getTag().equals(step.getTag());
    }

}
