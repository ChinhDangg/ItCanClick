package org.dev.Task;

import lombok.Getter;
import lombok.Setter;
import org.dev.Task.Action.Action;
import org.dev.Task.Condition.Condition;

import java.util.List;

@Getter
@Setter
public class Task {
    private Action action;
    private List<Condition> entryConditions;
    private List<Condition> exitCondition;
}
