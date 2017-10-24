package ch.fhnw.edu.stec.form;

import io.vavr.control.Try;

public interface StepFormController {

    Try<String> captureStep(String title, String description);

    Try<String> saveStep(String tag, String title, String description);
}
