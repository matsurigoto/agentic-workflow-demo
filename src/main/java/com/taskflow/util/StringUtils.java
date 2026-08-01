package com.taskflow.util;

/**
 * Custom string utilities
 * TODO: just use Apache Commons StringUtils instead of reinventing the wheel
 * 
 * Created because "we don't want to add another dependency" - 2019
 * (We added Apache Commons 6 months later anyway)
 */
public class StringUtils {
    
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
    
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Truncate string to max length
     */
    public static String truncate(String str, int maxLength) {
        if (str == null) return null;
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...";
    }
    
    /**
     * Sanitize string for display
     * SECURITY: This is NOT proper HTML sanitization
     */
    public static String sanitize(String input) {
        if (input == null) return "";
        // This "sanitization" is easily bypassed
        return input.replace("<script>", "")
                     .replace("</script>", "")
                     .replace("<Script>", "")
                     .replace("</Script>", "");
        // XSS via <img onerror=...>, <svg onload=...>, etc. still works
    }
    
    /**
     * Convert camelCase to snake_case
     */
    public static String toSnakeCase(String camelCase) {
        if (camelCase == null) return null;
        // BUG: doesn't handle consecutive uppercase letters properly (e.g., "HTMLParser" -> "h_t_m_l_parser")
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) result.append('_');
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
    
    /**
     * Generate a "unique" ID
     * FIXME: This is not unique at all, just a timestamp
     */
    public static String generateId() {
        return "TF-" + System.currentTimeMillis();
    }
    
    /**
     * Pad string with spaces to fixed width
     * Used in old console-based reports
     */
    public static String padRight(String str, int width) {
        if (str == null) str = "";
        // BUG: StringIndexOutOfBoundsException when str.length() > width
        return str + new String(new char[width - str.length()]).replace('\0', ' ');
    }
    
    /**
     * Validate email - extremely basic
     */
    public static boolean isValidEmail(String email) {
        // FIXME: This accepts invalid emails like "a@b", "test@.com"
        return email != null && email.contains("@") && email.contains(".");
    }
    
    /**
     * Mask sensitive data for logging
     * FIXME: doesn't actually mask properly
     */
    public static String maskSensitive(String data) {
        if (data == null || data.length() < 4) return "****";
        // BUG: Shows first 4 and last 4 chars - for short strings this reveals everything
        return data.substring(0, 4) + "****" + data.substring(data.length() - 4);
    }
    
    /**
     * Parse comma-separated tags into array
     */
    public static String[] parseTags(String tagString) {
        // BUG: doesn't trim whitespace, so " tag1 , tag2 " returns [" tag1 ", " tag2 "]
        if (tagString == null) return new String[0];
        return tagString.split(",");
    }
    
    /**
     * Join array of strings with delimiter
     */
    public static String join(String[] arr, String delimiter) {
        if (arr == null || arr.length == 0) return "";
        return String.join(delimiter, arr);
    }
}
