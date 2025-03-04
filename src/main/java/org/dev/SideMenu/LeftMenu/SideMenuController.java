package org.dev.SideMenu.LeftMenu;

import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import org.dev.AppScene;
import org.dev.Enum.CurrentTab;
import org.dev.Enum.LogLevel;
import org.dev.JobController.JobDataController;
import org.dev.JobController.MainJobController;
import org.dev.RunJob.JobRunController;
import org.dev.jobManagement.JobRunStructure;
import org.dev.jobManagement.JobStructure;

import java.net.URL;
import java.util.ResourceBundle;

/**
 operationHBox
 operationSideContentVBox
    VBox
    taskLabelHBox
    taskSideContentVBox
        actionLabelHBox
        actionLabelHBox
        actionLabelHBox
    VBox
    taskLabelHBox
    taskSideContentVBox
        actionLabelHBox
        actionLabelHBox
 */

public class SideMenuController implements Initializable {
    @FXML
    private StackPane parentNode;
    @FXML
    private ScrollPane sideMenuMainScrollPane;
    @FXML
    private VBox mainSideHierarchyVBox;
    @FXML
    private VBox runSideHierarchyVBox, sideHierarchyVBox;
    @FXML
    private Group newOperationGroupButton;

    private final String className = this.getClass().getSimpleName();
    public static RightClickMenuController rightClickMenuController;
    private double currentScale = 1.0;
    private static Node currentHighlightedLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setScaleHierarchyVBox();
        rightClickMenuController = loadRightClickMenu();
        newOperationGroupButton.setOnMouseClicked(this::getNewOperationPane);
    }

    private Node getParentNode() {
        return parentNode;
    }

    // ------------------------------------------------------
    private void setScaleHierarchyVBox() {
        if (currentScale != AppScene.currentGlobalScale) {
            currentScale = AppScene.currentGlobalScale;
            mainSideHierarchyVBox.getTransforms().add(new Scale(currentScale, currentScale, 0, 0));
            parentNode.setPrefWidth(parentNode.getPrefWidth() * currentScale);
        }
        AppScene.addLog(LogLevel.TRACE, className, "Scaled side menu: " + currentScale);
    }

    private void getNewOperationPane(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on new operation button");
        AppScene.loadEmptyOperation();
        AppScene.loadSideMenuHierarchy();
    }

    public void showSideMenuContent(CurrentTab whatTab) {
        if (whatTab == CurrentTab.Operation) {
            sideHierarchyVBox.setVisible(true);
            sideHierarchyVBox.setManaged(true);
            runSideHierarchyVBox.setVisible(false);
            runSideHierarchyVBox.setManaged(false);
        }
        else {
            runSideHierarchyVBox.setVisible(true);
            runSideHierarchyVBox.setManaged(true);
            sideHierarchyVBox.setVisible(false);
            sideHierarchyVBox.setManaged(false);
        }
        AppScene.addLog(LogLevel.DEBUG, className, "Showing tab: " + whatTab.name());
    }

    public void toggleSideMenuHierarchy() {
        Node parentNode = getParentNode();
        boolean newSet = !parentNode.isVisible();
        parentNode.setVisible(newSet);
        parentNode.setManaged(newSet);
        AppScene.addLog(LogLevel.DEBUG, className, "Showed Side menu: " + newSet);
    }

    // ------------------------------------------------------
    public static SideMenuLabelController getNewSideHBoxLabelController(String name, JobStructure jobStructure) {
        SideMenuLabelController controller = getSideMenuLabelController();
        if (controller == null)
            return null;
        Node hBoxLabel = controller.createHBoxLabel(name, jobStructure.getSideContent(), jobStructure.getCurrentController().getAppLevel());
        hBoxLabel.setOnMouseClicked(event -> doubleClickAndRightClick(event, jobStructure, rightClickMenuController));
        return controller;
    }

    public static SideMenuLabelController getNewRunSideHBoxController(String name, VBox sideContent, JobRunController<Object> jobRunController) {
        SideMenuLabelController controller = getSideMenuLabelController();
        if (controller == null)
            return null;
        Node hBoxLabel = controller.createHBoxLabel(name, sideContent, jobRunController.getAppLevel());
        hBoxLabel.setOnMouseClicked(event -> doubleClick(event, jobRunController));
        return controller;
    }

    private static SideMenuLabelController getSideMenuLabelController() {
        try {
            FXMLLoader sideMenuLabelLoader = new FXMLLoader(SideMenuController.class.getResource("sideMenuLabel.fxml"));
            sideMenuLabelLoader.load();
            return sideMenuLabelLoader.getController();
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, SideMenuController.class.getSimpleName(), "Error loading side menu label: " + e.getMessage());
            return null;
        }
    }

    private static void doubleClick(MouseEvent event, MainJobController jobController) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            jobController.takeToDisplay();
            highlightLabel((Node) event.getSource());
        }
    }

    private static void doubleClickAndRightClick(MouseEvent event, JobStructure jobStructure,
                                                 RightClickMenuController rightClickMenuController) {
        JobDataController currentController = jobStructure.getCurrentController();
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            currentController.takeToDisplay();
            highlightLabel((Node) event.getSource());
        }
        else if (event.getButton() == MouseButton.SECONDARY) {
            rightClickMenuController.showRightMenu(event, jobStructure);
        }
    }

    private static void highlightLabel(Node clickedNode) {
        if (currentHighlightedLabel != null)
            currentHighlightedLabel.pseudoClassStateChanged(PseudoClass.getPseudoClass("highlighted"), false);
        currentHighlightedLabel = clickedNode;
        currentHighlightedLabel.pseudoClassStateChanged(PseudoClass.getPseudoClass("highlighted"), true);
    }

    // ------------------------------------------------------
    public void loadSideHierarchy(JobStructure jobStructure) {
        newOperationGroupButton.setVisible(false);
        ObservableList<Node> sideHierarchyChildren = sideHierarchyVBox.getChildren();
        sideHierarchyChildren.clear();
        sideHierarchyChildren.add(jobStructure.getHBoxLabel());
        sideHierarchyChildren.add(jobStructure.getSideContent());
        AppScene.addLog(LogLevel.DEBUG, className, "Loaded Operation side hierarchy");
    }

    public void loadRunSideHierarchy(JobRunStructure jobRunStructure) {
        ObservableList<Node> runSideHierarchyChildren = runSideHierarchyVBox.getChildren();
        runSideHierarchyChildren.clear();
        runSideHierarchyChildren.add(jobRunStructure.getSideHBoxLabel());
        runSideHierarchyChildren.add(jobRunStructure.getSideContent());
        AppScene.addLog(LogLevel.DEBUG, className, "Loaded run side hierarchy");
    }


    private RightClickMenuController loadRightClickMenu() {
        try {
            FXMLLoader rightClickMenuLoader = new FXMLLoader(SideMenuController.class.getResource("rightClickMenuPane.fxml"));
            rightClickMenuLoader.load();
            return rightClickMenuLoader.getController();
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading right click menu: " + e.getMessage());
            return null;
        }
    }
}
