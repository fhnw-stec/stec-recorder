package ch.fhnw.edu.stec.capture;

import io.vavr.control.Try;

public interface StepCaptureController {

    Try<String> captureStep(String title, String description);

    // TODO: Remove once CI runs successfully
    private void java9() {
        System.out.println("Hello Java 9");
    }

}
