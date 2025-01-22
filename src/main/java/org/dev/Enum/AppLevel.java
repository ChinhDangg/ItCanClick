package org.dev.Enum;

import lombok.Getter;

@Getter
public enum AppLevel {
    Operation(1),
    TaskGroup(2),
    Task(3),
    Action(4),
    Condition(5);

    private final int order;

    AppLevel(int order) {
        this.order = order;
    }

}
