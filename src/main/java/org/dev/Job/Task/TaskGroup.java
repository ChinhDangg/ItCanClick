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
@NoArgsConstructor
@AllArgsConstructor
public class TaskGroup implements Serializable, MainJob {
    @Serial
    private static final long serialVersionUID = 1L;
    private String taskGroupName = "Task Group Name";
    private boolean required;
    private boolean disabled;

    @Override
    public TaskGroup cloneData() {
        return new TaskGroup(taskGroupName, required, disabled);
    }
}
