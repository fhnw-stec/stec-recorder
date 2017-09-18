package ch.fhnw.edu.stec.capture;

import io.vavr.control.Try;

public interface StepCaptureController {

    Try<String> captureStep(String title, String description);

}
