module org.dev {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires static lombok;
    requires tess4j;
    requires com.github.kwhat.jnativehook;
    requires jdk.unsupported.desktop;
    requires jdk.compiler;

    exports org.dev;
    exports org.dev.Enum;
    exports org.dev.Operation.Condition;
    exports org.dev.Operation.Data;
    exports org.dev.Menu;

    opens org.dev to javafx.fxml;
    opens org.dev.Menu to javafx.fxml;

    exports org.dev.Operation;
    opens org.dev.Operation to javafx.fxml;
    exports org.dev.Operation.Action;
    opens org.dev.Operation.Action to javafx.fxml;
    exports org.dev.Operation.Task;
    opens org.dev.Operation.Task to javafx.fxml;
    exports org.dev.RunOperation;
    opens org.dev.RunOperation to javafx.fxml;
    opens org.dev.Enum to javafx.fxml;
    exports org.dev.SideMenu;
    opens org.dev.SideMenu to javafx.fxml;
}
