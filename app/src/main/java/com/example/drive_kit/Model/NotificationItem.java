package com.example.drive_kit.Model;
/**
 * Represents a notification item in the system.
 *
 * A notification describes an upcoming or overdue event related to a car,
 * such as insurance expiration, annual test, or treatment reminder.
 *
 * Each notification includes:
 * - Type (what kind of event)
 * - Stage (how close or overdue it is)
 * - Message (text shown to the user)
 */
public class NotificationItem {

    /**
     * Type of notification.
     */
    public enum Type { INSURANCE, TEST ,TREATMENT_10K}

    /**
     * Stage of the notification (timing relative to event).
     */
    public enum Stage { NONE, D28, D14, D7, D1, EXPIRED ,M6, M7, M8, EXPIRED_TREAT}
    private final Type type; // Type of notification (insurance/test/treatment)
    private final Stage stage; // Stage of notification (timing or expiration status)
    private final String message; // Message displayed to the user

    /**
     * Constructs a NotificationItem.
     *
     * @param type type of notification
     * @param stage stage of notification
     * @param message user-facing message
     */
    public NotificationItem(Type type, Stage stage, String message) {
        this.type = type;
        this.stage = stage;
        this.message = message;
    }
    /** @return notification type */
    public Type getType() { return type; }

    /** @return notification stage */
    public Stage getStage() { return stage; }

    /** @return message text */
    public String getMessage() { return message; }
}
