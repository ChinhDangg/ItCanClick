package org.dev.jobManagement;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import lombok.AccessLevel;
import lombok.Getter;
import org.dev.RunJob.JobRunController;
import org.dev.SideMenu.LeftMenu.SideMenuController;
import org.dev.SideMenu.LeftMenu.SideMenuLabelController;

import java.util.ArrayList;
import java.util.List;

@Getter
public class JobRunStructure {
    private final JobRunController displayParentController;
    private final JobRunController parentController;
    private final JobRunController currentController;

    @Getter(AccessLevel.NONE)
    private SideMenuLabelController labelController;
    private final VBox sideContent = new VBox();
    private final List<JobRunStructure> subJobRunStructures;

    public JobRunStructure(JobRunController displayParentController, JobRunController parentController, JobRunController currentController,
                           String name) {
        subJobRunStructures = new ArrayList<>();
        this.displayParentController = displayParentController;
        this.parentController = parentController;
        this.currentController = currentController;
        labelController = SideMenuController.getNewRunSideHBoxController(name, sideContent, currentController);
    }

    public void collapseContent() {
        labelController.collapseContent();
    }

    public Node getSideHBoxLabel() {
        return labelController.getHBoxLabel();
    }

    public void addSubJobRunStructure(JobRunStructure subJobRunStructure) {
        subJobRunStructures.add(subJobRunStructure);
        addToSideContent(subJobRunStructure.getSideHBoxLabel(), subJobRunStructure.getSideContent());
    }

    private void addToSideContent(Node hBoxLabel, Node content) {
        Platform.runLater(() -> sideContent.getChildren().addAll(hBoxLabel, content));
    }

    public void removeAllSubJobRunStructure() {
        subJobRunStructures.clear();
        Platform.runLater(() -> sideContent.getChildren().clear());
    }
}
