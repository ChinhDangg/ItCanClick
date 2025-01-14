package org.dev.Enum;

import lombok.Getter;

@Getter
public enum AppLevel {
    Operation(1),
    Task(2),
    Action(3),
    Condition(4);

    private final int order;

    AppLevel(int order) {
        this.order = order;
    }

}
