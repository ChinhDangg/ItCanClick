package org.dev.Operation.Task;

import com.sun.tools.javac.Main;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dev.Operation.MainJob;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Task implements MainJob, Serializable {
    private String taskName = "Task Name";
    private boolean required;
    private boolean previousPass;
    private int repeatNumber;

    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public Task getDeepCopied() {
        return new Task(taskName, required, previousPass, repeatNumber);
    }
}
