package org.dev.Job.Task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dev.Job.MainJob;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class Task implements Serializable, MainJob {
    @Serial
    private static final long serialVersionUID = 1L;
    private String taskName = "Task Name";
    private boolean required;
    private boolean previousPass;
    private int repeatNumber;

    @Override
    public Task clone() {
        return new Task(taskName, required, previousPass, repeatNumber);
    }
}
