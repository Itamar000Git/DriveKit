package com.example.drive_kit.Model;
public class NotificationItem {

    public enum Type { INSURANCE, TEST }
    public enum Stage { NONE, D28, D14, D7, D1, EXPIRED }

    private final Type type;
    private final Stage stage;
    private final String message;

    public NotificationItem(Type type, Stage stage, String message) {
        this.type = type;
        this.stage = stage;
        this.message = message;
    }

    public Type getType() { return type; }
    public Stage getStage() { return stage; }
    public String getMessage() { return message; }
}
