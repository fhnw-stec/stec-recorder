package ch.fhnw.edu.stec.status;

import ch.fhnw.edu.stec.model.ProjectDir;
import ch.fhnw.edu.stec.model.Step;
import io.vavr.control.Option;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;

public interface StatusBarModel {

    ObjectProperty<ProjectDir> projectDirProperty();
    ObservableList<Node> getStatusBarLeftItems();
    ObservableList<Node> getStatusBarRightItems();
    Option<Step> getStepByTag(String tag);

}
