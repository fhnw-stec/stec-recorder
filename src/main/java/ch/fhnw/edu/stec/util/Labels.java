package ch.fhnw.edu.stec.util;

public interface Labels {

    String PROJECT_SECTION_TITLE = "Project Directory";
    String STEP_FORM_SECTION_TITLE = "Active Step";
    String STEP_HISTORY_SECTION_TITLE = "Step History";

    String DIR_CHOOSER_BUTTON_LABEL = "...";
    String INIT_BUTTON_LABEL = "Initialize";

    String INVALID_PROJECT_DIR_LABEL = "Not a directory";
    String UNINITIALIZED_PROJECT_DIR_LABEL = "Not initialized";
    String READY_PROJECT_DIR_LABEL = "Project directory ready";

    String STEP_TITLE_PROMPT = "Step Title";
    String STEP_DESCRIPTION_PROMPT = "Step Description (AsciiDoc)";
    String CAPTURE_BUTTON_LABEL = "Capture";
    String PREVIEW = "Preview";

    String COMMIT_MSG_SUFFIX = "\n\nCaptured with https://github.com/fhnw-stec/stec-recorder";
    String CAPTURE_COMMIT_MSG_TEMPLATE = "Create Step '%s'" + COMMIT_MSG_SUFFIX;
    String EDIT_COMMIT_MSG_TEMPLATE = "Edit Step '%s'" + COMMIT_MSG_SUFFIX;
    String INITIAL_STATUS_COMMIT_MSG = "Initial status" + COMMIT_MSG_SUFFIX;

    String REFRESH_BUTTON_TOOLTIP = "Refresh";

    String CAPTURE_SUCCESSFUL = "Capture successful";
    String CAPTURE_FAILED = "Capture failed";

    String SAVE_SUCCESSFUL = "Save successful";
    String SAVE_FAILED = "Save failed";

    String CHECKOUT_SUCCESSFUL = "Checkout successful";
    String CHECKOUT_FAILED = "Checkout failed";

    String DELETE_STEP_SUCCESSFUL = "Delete successful";
    String DELETE_STEP_FAILED = "Delete failed";

    String ENTERING_CAPTURE_MODE_SUCCESSFUL = "Ready for a next step";
    String ENTERING_CAPTURE_MODE_FAILED = "Entering capture mode failed";

    String SAVE_BUTTON_LABEL = "Save";
    String RESET_BUTTON_LABEL = "Reset";

    String LOADING_STEP_FILE_CHANGES_FAILED = "Loading step file changes failed";

}
