package org.dev.Operation.Data;

import lombok.Getter;
import lombok.Setter;
import org.dev.Operation.Condition.Condition;
import java.io.Serial;

@Getter @Setter
public class ConditionData implements AppData {
    @Serial
    private static final long serialVersionUID = 1L;
    private Condition condition;
}
