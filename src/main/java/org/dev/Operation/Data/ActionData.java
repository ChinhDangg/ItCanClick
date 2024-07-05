package org.dev.Operation.Data;

import lombok.Getter;
import lombok.Setter;
import org.dev.Operation.Action.Action;
import org.dev.Operation.Condition.Condition;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class ActionData implements Serializable {
    private Action action;
    private List<Condition> entryCondition;
    private List<Condition> exitCondition;
}
