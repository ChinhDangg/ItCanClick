package org.dev.Enum;

public enum ActionTypes {
    MouseClick,
    MouseDoubleClick,
    KeyClick,
    KeyPress,
    KeyPressMouseClick;

    public boolean isKeyAction() {
        return this != MouseClick && this != MouseDoubleClick;
    }
}
