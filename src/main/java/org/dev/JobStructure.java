package org.dev;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import lombok.AccessLevel;
import lombok.Getter;
import org.dev.JobController.JobDataController;
import org.dev.SideMenu.LeftMenu.SideMenuController;
import org.dev.SideMenu.LeftMenu.SideMenuLabelController;

import java.util.ArrayList;
import java.util.List;

@Getter
public class JobStructure {

    private final JobDataController displayParentController;
    private final JobDataController parentController;
    private final JobDataController currentController;

    @Getter(AccessLevel.NONE)
    private final SideMenuLabelController labelController;
    private final VBox sideContent = new VBox();
    private final List<JobStructure> subJobStructures;

    public JobStructure(JobDataController displayParentController, JobDataController parentController,
                        JobDataController currentController, String name) {
        subJobStructures = new ArrayList<>();
        this.displayParentController = displayParentController;
        this.parentController = parentController;
        this.currentController = currentController;
        labelController = SideMenuController.getNewSideHBoxLabelController(name, this);
    }

    public void changeName(String name) {
        labelController.changeLabelName(name);
    }

    public Node getHBoxLabel() {
        return labelController.getHBoxLabel();
    }

    public void addSubJobStructure(JobStructure structure) {
        subJobStructures.add(structure);
        addToSideContent(structure.getHBoxLabel(), structure.getSideContent());
    }

    public void removeSubJobStructure(JobStructure structure) {
        int removeIndex = subJobStructures.indexOf(structure);
        subJobStructures.remove(structure);
        removeFromSideContent(removeIndex);
    }

    public void updateSubJobStructure(JobStructure structure, int changeIndex) {
        int selectedIndex = subJobStructures.indexOf(structure);
        subJobStructures.remove(structure);
        subJobStructures.add(changeIndex, structure);
        updateSideContent(selectedIndex, changeIndex);
    }

    private void addToSideContent(Node sideHBoxLabel, VBox sideContent) {
        sideContent.getChildren().add(new VBox(sideHBoxLabel, sideContent));
    }

    private void removeFromSideContent(int index) {
        sideContent.getChildren().remove(index);
    }

    private void updateSideContent(int selectedIndex, int changeIndex) {
        Node temp = sideContent.getChildren().get(selectedIndex);
        sideContent.getChildren().remove(selectedIndex);
        sideContent.getChildren().add(changeIndex, temp);
    }

}
