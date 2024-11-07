package org.dev.Operation.Task;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
public class Task implements Serializable {
    private String taskName = "Task Name";
    private boolean required;
    private boolean previousPass;
    private int repeatNumber;
}
