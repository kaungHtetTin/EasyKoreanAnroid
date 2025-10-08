package com.calamus.easykorean.app;

import java.util.regex.Pattern;

public class FileNameSanitizer {

    /**
     * Removes characters that cannot be used in Android filenames
     * @param input The original filename string
     * @return Sanitized filename with invalid characters removed
     */
    public static String sanitizeFileName(String input) {
        if (input == null || input.isEmpty()) {
            return "unnamed";
        }

        // Pattern for characters not allowed in Android filenames
        // Includes: \ / : * ? " < > | and control characters
        Pattern invalidChars = Pattern.compile("[\\\\/:*?\"<>|\\x00-\\x1F]");

        // Remove invalid characters
        String sanitized = invalidChars.matcher(input).replaceAll("");

        // Also remove trailing periods and spaces (Windows restriction that affects Android)
        sanitized = sanitized.replaceAll("\\.+$", "").replaceAll("\\s+$", "");

        // If the result is empty after sanitization, return a default name
        if (sanitized.isEmpty()) {
            return "unnamed";
        }

        return sanitized;
    }

    // Alternative implementation with explicit character replacement
    public static String sanitizeFileNameAlternative(String input) {
        if (input == null || input.isEmpty()) {
            return "unnamed";
        }

        StringBuilder sb = new StringBuilder();

        for (char c : input.toCharArray()) {
            // Allow letters, digits, spaces, and common safe characters
            if (Character.isLetterOrDigit(c) ||
                    c == ' ' || c == '-' || c == '_' || c == '.' ||
                    c == '(' || c == ')' || c == '[' || c == ']' ||
                    c == '!' || c == '@' || c == '#' || c == '$' ||
                    c == '%' || c == '&' || c == '+' || c == ',' ||
                    c == ';' || c == '=' || c == '\'' || c == '~') {
                sb.append(c);
            }
            // Explicitly block invalid characters
            else if (c != '\\' && c != '/' && c != ':' && c != '*' &&
                    c != '?' && c != '"' && c != '<' && c != '>' &&
                    c != '|' && c >= 32) { // Allow printable characters (>= space)
                sb.append(c);
            }
            // All other characters (control characters, etc.) are skipped
        }

        String result = sb.toString();

        // Remove trailing periods and spaces
        result = result.replaceAll("\\.+$", "").replaceAll("\\s+$", "");

        if (result.isEmpty()) {
            return "unnamed";
        }

        return result;
    }
}