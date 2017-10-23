package ch.fhnw.edu.stec.history;

import ch.fhnw.edu.stec.model.Step;
import io.vavr.control.Try;

public interface StepHistoryController {

    Try<String> switchToEditMode(Step step);

    Try<String> switchToCaptureMode();

    Try<String> deleteStep(Step step);

}