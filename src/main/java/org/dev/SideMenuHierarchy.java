package org.dev;

import javafx.scene.control.Label;
import lombok.Getter;
import org.dev.Operation.MainJobController;
import java.util.ArrayList;
import java.util.List;

@Getter
public class SideMenuHierarchy {

    private final Label controllerLabelName;
    private final MainJobController controller;
    private final List<SideMenuHierarchy> subHierarchies;

    public SideMenuHierarchy(Label name, MainJobController controller) {
        controllerLabelName = name;
        this.controller = controller;
        subHierarchies = new ArrayList<>();
    }

    public void addSubHierarchy(SideMenuHierarchy hierarchy) {
        subHierarchies.add(hierarchy);
    }
    public void removeSubHierarchy(SideMenuHierarchy hierarchy) {
        subHierarchies.remove(hierarchy);
    }
    public void adjustSubHierarchy(int oldIndex, int newIndex) {
        subHierarchies.add(newIndex, subHierarchies.remove(oldIndex));
    }

    public void changeName(String newName) {
        controllerLabelName.setText(newName);
    }
}
