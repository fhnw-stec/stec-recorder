package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.model.GigDir;
import ch.fhnw.edu.stec.model.Step;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class StecModel {

    private final ObjectProperty<GigDir> gigDir = new SimpleObjectProperty<>();
    private final ObservableList<Step> steps = FXCollections.observableArrayList();

    ObjectProperty<GigDir> gigDirProperty() {
        return gigDir;
    }

    ObservableList<Step> steps() {
        return steps;
    }

}