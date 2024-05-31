module org.dev {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires static lombok;
    requires tess4j;
    requires com.github.kwhat.jnativehook;

    exports org.dev;
    exports org.dev.Enum;
    exports org.dev.Task.Condition;
    exports org.dev.Menu;

    opens org.dev to javafx.fxml;
    opens org.dev.Menu to javafx.fxml;
    exports org.dev.Task;
    opens org.dev.Task to javafx.fxml;
    exports org.dev.Task.Action;
    opens org.dev.Task.Action to javafx.fxml;
}
