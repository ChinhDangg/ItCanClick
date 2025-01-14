package org.dev.Operation.Data;

import lombok.Getter;
import lombok.Setter;
import org.dev.Operation.Task.Task;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter @Setter
public class TaskData implements AppData {
    @Serial
    private static final long serialVersionUID = 1L;
    private Task task;
    private List<ActionData> actionDataList;
}
