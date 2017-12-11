package ch.fhnw.edu.stec.history;

import ch.fhnw.edu.stec.model.Step;
import ch.fhnw.ima.memento.MementoModel;
import ch.fhnw.ima.memento.MementoRef;
import io.vavr.control.Option;
import javafx.beans.property.ObjectProperty;

public interface StepHistoryModel {

    MementoModel<Step> getMementoModel();

    ObjectProperty<Option<MementoRef>> getMementoSelectionModel();

}
