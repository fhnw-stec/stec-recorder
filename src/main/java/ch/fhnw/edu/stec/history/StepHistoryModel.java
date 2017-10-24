package ch.fhnw.edu.stec.history;

import ch.fhnw.edu.stec.model.InteractionMode;
import ch.fhnw.edu.stec.model.Step;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;

public interface StepHistoryModel {

    ObservableList<Step> getSteps();

    ObjectProperty<InteractionMode> interactionModeProperty();

    default boolean isBeingEdited(Step step) {
        return (interactionModeProperty().get() instanceof InteractionMode.Edit) &&
                interactionModeProperty().get().getTag().equals(step.getTag());
    }

}
