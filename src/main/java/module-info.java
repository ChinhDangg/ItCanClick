module org.dev {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires static lombok;
    requires tess4j;
    requires com.github.kwhat.jnativehook;
    requires jdk.compiler;
    requires org.slf4j;
    requires org.bytedeco.opencv;

    exports org.dev;
    opens org.dev;
    exports org.dev.Enum;
    opens org.dev.Enum;
    exports org.dev.Job;
    opens org.dev.Job;
    exports org.dev.Job.Action;
    opens org.dev.Job.Action;
    exports org.dev.Job.Task;
    opens org.dev.Job.Task;
    exports org.dev.Job.Condition;
    opens org.dev.Job.Condition;
    exports org.dev.JobController;
    opens org.dev.JobController;
    exports org.dev.Menu;
    opens org.dev.Menu;
    exports org.dev.RunJob;
    opens org.dev.RunJob;
    exports org.dev.SideMenu;
    opens org.dev.SideMenu;
    exports org.dev.SideMenu.LeftMenu;
    opens org.dev.SideMenu.LeftMenu;
    exports org.dev.SideMenu.TopMenu;
    opens org.dev.SideMenu.TopMenu;
    exports org.dev.jobManagement;
    opens org.dev.jobManagement;

}
