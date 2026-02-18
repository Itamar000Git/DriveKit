package com.example.drive_kit.utils;

import java.util.regex.Pattern;

public final class PasswordValidator {

    private PasswordValidator() {}

    public static class Result {
        public final boolean ok;
        public final String error;

        private Result(boolean ok, String error) {
            this.ok = ok;
            this.error = error;
        }

        public static Result ok() { return new Result(true, null); }
        public static Result fail(String msg) { return new Result(false, msg); }
    }

    private static final Pattern UPPER = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWER = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL = Pattern.compile(".*[^A-Za-z0-9].*");

    public static Result validate(String password) {
        if (password == null || password.isEmpty()) return Result.fail("חובה להזין סיסמה");
        if (password.contains(" ")) return Result.fail("סיסמה לא יכולה להכיל רווחים");
        if (password.length() < 8) return Result.fail("סיסמה חייבת להכיל לפחות 8 תווים");
        if (!UPPER.matcher(password).matches()) return Result.fail("חייבת אות גדולה (A-Z)");
        if (!LOWER.matcher(password).matches()) return Result.fail("חייבת אות קטנה (a-z)");
        if (!DIGIT.matcher(password).matches()) return Result.fail("חייבת ספרה (0-9)");
        if (!SPECIAL.matcher(password).matches()) return Result.fail("חייבת תו מיוחד (למשל !@#)");
        return Result.ok();
    }
}
