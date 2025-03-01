package org.dev.jobManagement;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import lombok.Getter;
import org.dev.RunJob.JobRunController;
import org.dev.SideMenu.LeftMenu.SideMenuController;

@Getter
public class JobRunStructure {
    private final JobRunController displayParentController;
    private final JobRunController parentController;
    private final JobRunController currentController;

    private final Node sideHBoxLabel;
    private final VBox sideContent = new VBox();

    public JobRunStructure(JobRunController displayParentController, JobRunController parentController, JobRunController currentController,
                           String name) {
        this.displayParentController = displayParentController;
        this.parentController = parentController;
        this.currentController = currentController;
        sideHBoxLabel = SideMenuController.getNewSideHBoxLabel(name, this);
    }

    public void addToSideContent(Node hBoxLabel, Node content) {
        sideContent.getChildren().addAll(hBoxLabel, content);
    }
}
