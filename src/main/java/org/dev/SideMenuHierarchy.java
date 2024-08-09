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
    private List<SideMenuHierarchy> subHierarchies;

    public SideMenuHierarchy(Label name, MainJobController controller) {
        controllerLabelName = name;
        this.controller = controller;
    }

    public void addSubHierarchy(SideMenuHierarchy hierarchy) {
        if (subHierarchies == null)
            subHierarchies = new ArrayList<>();
        subHierarchies.add(hierarchy);
    }
    public void removeSubHierarchy(SideMenuHierarchy hierarchy) {
        subHierarchies.remove(hierarchy);
    }
    public void adjustSubHierarchy(int oldIndex, int newIndex) {
        subHierarchies.add(newIndex, subHierarchies.remove(oldIndex));
    }
}
