package org.dev.Operation.Data;

import lombok.Getter;
import lombok.Setter;
import org.dev.Operation.Action.Action;
import org.dev.Operation.Condition.Condition;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter @Setter
public class ActionData implements AppData {
    @Serial
    private static final long serialVersionUID = 1L;
    private Action action;
    private List<Condition> entryConditionList;
    private List<Condition> exitConditionList;
}
