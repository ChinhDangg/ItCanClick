package org.dev;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.dev.Enum.ActionTypes;
import org.dev.Enum.ReadingCondition;
import org.dev.Operation.Action.ActionKeyClick;
import org.dev.Operation.Condition.Condition;
import org.dev.Operation.Condition.PixelCondition;
import org.dev.Operation.Condition.TextCondition;
import org.dev.Operation.Data.ActionData;
import org.dev.Operation.Data.OperationData;
import org.dev.Operation.Data.TaskData;
import org.dev.Operation.Operation;
import org.dev.Operation.OperationController;
import org.dev.Operation.Task.Task;
import org.dev.RunOperation.OperationRunController;
import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Testing extends Application {

    public static void main(String[] args) throws InterruptedException, IOException {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = AppScene.getAppMainScene();

        OperationData opData = getOperationData();

        FXMLLoader operationRunLoader = new FXMLLoader(getClass().getResource("RunOperation/operationRunPane.fxml"));
        Group operationRunGroup = operationRunLoader.load();
        OperationRunController opRunController = operationRunLoader.getController();
        opRunController.runOperation(opData);

        FXMLLoader operationLoader = new FXMLLoader(getClass().getResource("Operation/operationPane.fxml"));
        operationLoader.load();
        OperationController opController = operationLoader.getController();
        opController.loadSavedOperationData(opData);
        AppScene.currentLoadedOperationController = opController;
        AppScene.loadSideMenuHierarchy();

        AppScene.currentLoadedOperationRunController = opRunController;
        AppScene.primaryCenterStackPane.getChildren().add(operationRunGroup);
        AppScene.sideMenuController.loadRunSideHierarchy(opRunController);

        stage.setScene(scene);
        stage.show();
    }




    private OperationData getOperationData() throws IOException {
        ActionData actionData = new ActionData();
        ActionKeyClick actionKeyClick = new ActionKeyClick();
        actionKeyClick.setActionOptions(
                1,
                false,
                1000,
                1000,
                1000,
                ActionTypes.KeyClick,
                ImageIO.read(new File("src/main/resources/org/dev/images/folderIcon.png")),
                ImageIO.read(new File("src/main/resources/org/dev/images/folderIcon.png")),
                new Rectangle(10, 10, 200, 200),
                50
        );
        actionData.setAction(actionKeyClick);
        PixelCondition condition = new PixelCondition(
                ReadingCondition.Pixel,
                ImageIO.read(new File("src/main/resources/org/dev/images/folderIcon.png")),
                new Rectangle(10, 10, 200, 200),
                false,
                false,
                ImageIO.read(new File("src/main/resources/org/dev/images/folderIcon.png")),
                false
        );
        TextCondition textCondition = new TextCondition(
                ReadingCondition.Text,
                ImageIO.read(new File("src/main/resources/org/dev/images/folderIcon.png")),
                new Rectangle(10, 10, 200, 200),
                false,
                false,
                2.0,
                new HashSet<>(List.of("Something", "Something2", "Something3"))
        );
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        conditions.add(textCondition);
        actionData.setEntryConditionList(conditions);

        TaskData taskData = new TaskData();
        Task task = new Task();
        task.setRequired(true);
        task.setPreviousPass(false);
        task.setRepeatNumber(0);
        taskData.setTask(task);
        List<ActionData> actionDataList = new ArrayList<>();
        actionDataList.add(actionData);
        actionDataList.add(actionData);
        taskData.setActionDataList(actionDataList);

        OperationData operationData = new OperationData();
        operationData.setOperation(new Operation());
        List<TaskData> taskDataList = new ArrayList<>();
        taskDataList.add(taskData);
        taskDataList.add(taskData);
        operationData.setTaskDataList(taskDataList);
        return operationData;
    }

}
