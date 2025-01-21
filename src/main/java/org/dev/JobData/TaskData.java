package org.dev.JobData;

import lombok.Getter;
import lombok.Setter;
import org.dev.Job.Task.Task;

import java.io.Serial;
import java.util.List;

@Getter @Setter
public class TaskData implements JobData {
    @Serial
    private static final long serialVersionUID = 1L;
    private Task task;
    private List<ActionData> actionDataList;
}
