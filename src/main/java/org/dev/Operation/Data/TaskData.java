package org.dev.Operation.Data;

import lombok.Getter;
import lombok.Setter;
import org.dev.Operation.Task.Task;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class TaskData implements Serializable {
    private Task task;
    private List<ActionData> actionDataList;
}
