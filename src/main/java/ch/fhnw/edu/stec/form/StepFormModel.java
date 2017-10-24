package ch.fhnw.edu.stec.form;

import ch.fhnw.edu.stec.model.InteractionMode;
import ch.fhnw.edu.stec.model.Step;
import io.vavr.control.Option;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

public interface StepFormModel {

    StringProperty titleProperty();

    StringProperty descriptionProperty();

    ObjectProperty<InteractionMode> interactionModeProperty();

    Option<Step> getStepByTag(String tag);

}