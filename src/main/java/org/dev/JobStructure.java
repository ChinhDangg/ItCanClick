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
    private SideMenuLabelController labelController;
    private final VBox sideContent = new VBox();
    private final List<JobStructure> subJobStructures;

    public JobStructure(JobDataController displayParentController, JobDataController parentController, JobDataController currentController, String name) {
        subJobStructures = new ArrayList<>();
        this.displayParentController = displayParentController;
        this.parentController = parentController;
        this.currentController = currentController;
        if (name != null)
            labelController = SideMenuController.getNewSideHBoxLabelController(name, this);
    }

    public String getName() {
        return labelController.getName();
    }

    public void changeName(String name) {
        labelController.changeLabelName(name);
    }

    public Node getHBoxLabel() {
        if (labelController == null)
            return null;
        return labelController.getHBoxLabel();
    }

    public int getSubStructureSize() {
        return subJobStructures.size();
    }

    public int getSubStructureIndex(JobDataController jobDataController) {
        int count = 0;
        for (JobStructure subJobStructure : subJobStructures) {
            if (subJobStructure.getCurrentController() == jobDataController)
                return count;
            count++;
        }
        return -1;
    }

    public void addSubJobStructure(JobStructure structure) {
        subJobStructures.add(structure);
        addToSideContent(structure.getHBoxLabel(), structure.getSideContent());
    }

    public void addSubJobStructure(int index, JobStructure structure) {
        subJobStructures.add(index, structure);
        addToSideContent(structure.getHBoxLabel(), structure.getSideContent());
    }

    public int removeSubJobStructure(JobDataController jobDataController) {
        int removeIndex = getSubStructureIndex(jobDataController);
        subJobStructures.remove(removeIndex);
        removeFromSideContent(removeIndex);
        return removeIndex;
    }

    public void updateSubJobStructure(JobDataController jobDataController, int changeIndex) {
        int selectedIndex = getSubStructureIndex(jobDataController);
        subJobStructures.add(changeIndex, subJobStructures.remove(selectedIndex));
        updateSideContent(selectedIndex, changeIndex);
    }

    private void addToSideContent(Node sideHBoxLabel, VBox newSideContent) {
        if (sideHBoxLabel == null)
            return;
        sideContent.getChildren().add(new VBox(sideHBoxLabel, newSideContent));
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
