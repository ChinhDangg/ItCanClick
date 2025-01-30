package org.dev.JobController;

import javafx.scene.Node;
import org.dev.Enum.AppLevel;

public interface MainJobController {

    Node getParentNode();

    AppLevel getAppLevel();

    void takeToDisplay();
}
