package org.dev.JobData;

import lombok.Getter;
import lombok.Setter;
import org.dev.Job.Task.TaskGroup;
import java.io.Serial;
import java.util.List;

@Getter @Setter
public class TaskGroupData implements JobData {
    @Serial
    private static final long serialVersionUID = 1L;
    private TaskGroup taskGroup;
    private List<TaskData> taskDataList;
}
