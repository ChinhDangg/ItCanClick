package org.dev;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.dev.Operation.*;
import org.dev.Operation.Action.Action;
import org.dev.Operation.Action.ActionKeyClick;

public class Testing extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {

        OperationController operationController = new OperationController();
        operationController.getOperation().setOperationName("Some operation name");

        MinimizedTaskController minimizedTaskController = new MinimizedTaskController();
        MinimizedTaskController minimizedTaskController1 = new MinimizedTaskController();
        operationController.getTaskList().add(minimizedTaskController);
        operationController.getTaskList().add(minimizedTaskController1);

        TaskController taskController = new TaskController();
        taskController.getTask().setTaskName("Some task name");
        TaskController taskController1 = new TaskController();
        taskController1.getTask().setTaskName("Some task name 2");
        minimizedTaskController.setTaskController(taskController);
        minimizedTaskController1.setTaskController(taskController1);

        ActionController actionController = new ActionController();
        Action action = new ActionKeyClick();
        action.setActionName("Some action name");
        actionController.setAction(action);
        ActionController actionController1 = new ActionController();
        Action action1 = new ActionKeyClick();
        action1.setActionName("Some action name 2");
        actionController1.setAction(action1);
        taskController.getActionList().add(actionController);
        taskController.getActionList().add(actionController1);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("conditionPaneTesting.fxml"));
        StackPane node = loader.load();
        SideMenuController sideMenuController = loader.getController();
        sideMenuController.loadSideHierarchy(operationController);

        Group group = new Group(node);
        Pane p = new Pane(group);
        Scene scene = new Scene(p);
        stage.setScene(scene);
        stage.show();
    }
}
