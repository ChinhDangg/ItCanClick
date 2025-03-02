package org.dev;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

public class Testing1 extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Root Node
        TreeItem<String> rootItem = new TreeItem<>("Root Folder");
        rootItem.setExpanded(true); // Keep root expanded

        // First-level folders
        TreeItem<String> folder1 = new TreeItem<>("Folder 1");
        TreeItem<String> folder2 = new TreeItem<>("Folder 2");

        // Files inside Folder 1
        TreeItem<String> file1 = new TreeItem<>("File 1.txt");
        TreeItem<String> file2 = new TreeItem<>("File 2.txt");

        // Subfolder inside Folder 1
        TreeItem<String> subFolder = new TreeItem<>("Subfolder");
        TreeItem<String> subFile = new TreeItem<>("Subfile.txt");
        subFolder.getChildren().add(subFile);

        // Add items to hierarchy
        folder1.getChildren().addAll(file1, file2, subFolder);
        rootItem.getChildren().addAll(folder1, folder2);

        // Create TreeView
        TreeView<String> treeView = new TreeView<>(rootItem);

        // Scene Setup
        Scene scene = new Scene(treeView, 300, 400);
        primaryStage.setTitle("JavaFX File Explorer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
