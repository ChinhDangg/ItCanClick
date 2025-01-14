package org.dev.Operation.Data;

import lombok.Getter;
import lombok.Setter;
import org.dev.Operation.Operation;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter @Setter
public class OperationData implements AppData {
    @Serial
    private static final long serialVersionUID = 1L;
    private Operation operation;
    private List<TaskData> taskDataList;
}
