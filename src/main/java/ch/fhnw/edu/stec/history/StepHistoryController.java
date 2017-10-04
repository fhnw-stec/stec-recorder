package ch.fhnw.edu.stec.history;

import io.vavr.control.Try;

public interface StepHistoryController {

    Try<String> checkoutStep(String tag);

}