package ch.fhnw.edu.stec;

import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.migrationsupport.rules.ExternalResourceSupport;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ExternalResourceSupport.class)
class StecControllerTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    void chooseDirectory() throws IOException {
        StecModel model = new StecModel();
        StecController controller = new StecController(model);

        File initialDir = tmpFolder.newFolder("initial");
        model.gigDirectoryProperty().set(initialDir);

        controller.chooseDirectory(null);
        assertEquals(initialDir, model.gigDirectoryProperty().get(), "no change if null");

        controller.chooseDirectory(tmpFolder.newFile());
        assertEquals(initialDir, model.gigDirectoryProperty().get(), "no change if not a directory");

        File newDir = tmpFolder.newFolder("new");
        controller.chooseDirectory(newDir);
        assertEquals(newDir, model.gigDirectoryProperty().get());
    }

}