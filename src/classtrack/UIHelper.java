package classtrack;

/**
 * UIHelper - Utility class for console UI enhancements
 * Provides ANSI color codes, box drawing, and formatted messages
 */
public class UIHelper {

    // ANSI Color Codes
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // Bright/Bold Colors
    public static final String BRIGHT_BLACK = "\u001B[90m";
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_BLUE = "\u001B[94m";
    public static final String BRIGHT_MAGENTA = "\u001B[95m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_WHITE = "\u001B[97m";

    // Text Styles
    public static final String BOLD = "\u001B[1m";
    public static final String UNDERLINE = "\u001B[4m";

    // Dimensions
    private static final int MENU_WIDTH = 60; // Banner width
    private static final int INNER_WIDTH = MENU_WIDTH - 2; // Subtract borders

    /**
     * Prints a success message in green
     */
    public static void printSuccess(String message) {
        System.out.println(GREEN + "[OK] " + message + RESET);
    }

    /**
     * Prints an error message in red
     */
    public static void printError(String message) {
        System.out.println(RED + "[ERROR] " + message + RESET);
    }

    /**
     * Prints an info message in cyan
     */
    public static void printInfo(String message) {
        System.out.println(CYAN + "[INFO] " + message + RESET);
    }

    /**
     * Prints a warning message in yellow
     */
    public static void printWarning(String message) {
        System.out.println(YELLOW + "[WARN] " + message + RESET);
    }

    /**
     * Prints a colorful banner with gradient effect
     */
    public static void printBanner() {
        String border = "═".repeat(INNER_WIDTH);

        System.out.println(BRIGHT_CYAN + "╔" + border + "╗");
        System.out.println("║" + " ".repeat(INNER_WIDTH) + "║");

        System.out.println("║" + centerText(
                BOLD + BRIGHT_MAGENTA + "ClassTrack" + RESET + BRIGHT_WHITE + " - Student Attendance Management",
                INNER_WIDTH) + BRIGHT_CYAN + "║");
        System.out.println("║" + centerText(BRIGHT_YELLOW + "System v1.0" + RESET, INNER_WIDTH) + BRIGHT_CYAN + "║");

        System.out.println("║" + " ".repeat(INNER_WIDTH) + "║");
        System.out.println("║" + centerText(BRIGHT_GREEN + "Developed using Core Java & MySQL" + RESET, INNER_WIDTH)
                + BRIGHT_CYAN + "║");
        System.out.println("║" + " ".repeat(INNER_WIDTH) + "║");
        System.out.println("╚" + border + "╝" + RESET);
        System.out.println();
    }

    /**
     * Prints a section header with colored box
     */
    public static void printSectionHeader(String title, String color) {
        String separator = "═".repeat(42);
        System.out.println("\n" + color + separator + RESET);
        System.out.println(color + BOLD + centerTextSimple(title, 42) + RESET);
        System.out.println(color + separator + RESET);
    }

    /**
     * Centers text within a given width (accounting for ANSI codes in length
     * calculation is hard, so explicit padding is safer)
     * This method assumes text contains ANSI codes but centers based on visible
     * length?
     * Correcting centering with ANSI codes is tricky.
     * Workaround: Split the text into parts, center the visible part, then wrap
     * with ANSI.
     * In printBanner above, I applied ANSI inside.
     * Let's use strict manual padding for now to avoid complexity.
     */
    private static String centerText(String text, int width) {
        // Strip ANSI codes to calculate visible length
        String visibleText = text.replaceAll("\u001B\\[[;\\d]*m", "");
        int padding = (width - visibleText.length()) / 2;
        int extra = (width - visibleText.length()) % 2;
        return " ".repeat(Math.max(0, padding)) + text + " ".repeat(Math.max(0, padding + extra));
    }

    private static String centerTextSimple(String text, int width) {
        int padding = (width - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text;
    }

    /**
     * Prints a colored menu with icons
     */
    public static void printMenu() {
        int width = 58; // Menu width (matches banner inner width)
        String border = "═".repeat(width);

        System.out.println("\n" + BRIGHT_CYAN + "╔" + border + "╗");

        // Title
        String title = "MAIN MENU";
        int titlePad = (width - title.length()) / 2;
        System.out.println("║" + " ".repeat(titlePad) + BOLD + BRIGHT_WHITE + title + RESET + BRIGHT_CYAN
                + " ".repeat(width - titlePad - title.length()) + "║");

        System.out.println("╠" + border + "╣" + RESET);

        printMenuItem("1. - Add Student", BRIGHT_GREEN, CYAN, width);
        printMenuItem("2. - Update Student", BRIGHT_GREEN, CYAN, width);
        printMenuItem("3. - Delete Student", BRIGHT_GREEN, CYAN, width);
        printMenuItem("4. - Search Student", BRIGHT_GREEN, BLUE, width);
        printMenuItem("5. - Mark Student Attendance (Single)", BRIGHT_GREEN, GREEN, width);
        printMenuItem("6. - Mark Class Attendance (Batch)", BRIGHT_GREEN, CYAN, width);
        printMenuItem("7. - View Attendance (Single)", BRIGHT_GREEN, YELLOW, width);
        printMenuItem("8. - View Attendance Summary (All)", BRIGHT_GREEN, CYAN, width);
        printMenuItem("9. - Show Defaulters (Below 75%)", BRIGHT_GREEN, RED, width);
        printMenuItem("10. - Logout", BRIGHT_GREEN, MAGENTA, width);
        printMenuItem("11. - Exit", BRIGHT_GREEN, BRIGHT_RED, width);

        System.out.println(BRIGHT_CYAN + "╚" + border + "╝" + RESET);
    }

    private static void printMenuItem(String text, String numColor, String textColor, int width) {
        // text format: "1. - Add Student"
        // We want to color "1." differently from "- Add Student"
        String[] parts = text.split(" - ", 2);
        String number = parts[0];
        String label = " - " + parts[1];

        int visibleLength = number.length() + label.length();
        int padding = width - visibleLength - 2; // -2 for left margin

        System.out.print(BRIGHT_CYAN + "║ " + numColor + number + textColor + label);
        System.out.println(" ".repeat(Math.max(0, padding)) + BRIGHT_CYAN + " ║");
    }

    /**
     * Prints a table header
     */
    public static void printTableHeader(String... columns) {
        System.out.println(BRIGHT_CYAN + "┌" + "─".repeat(70) + "┐" + RESET);
        System.out.print(BRIGHT_CYAN + "│ " + BOLD + BRIGHT_WHITE);
        for (String col : columns) {
            System.out.print(String.format("%-17s", col));
        }
        System.out.println(RESET + BRIGHT_CYAN + " │" + RESET);
        System.out.println(BRIGHT_CYAN + "├" + "─".repeat(70) + "┤" + RESET);
    }

    /**
     * Prints a table row
     */
    public static void printTableRow(String... values) {
        System.out.print(BRIGHT_CYAN + "│ " + WHITE);
        for (String val : values) {
            System.out.print(String.format("%-17s", val));
        }
        System.out.println(RESET + BRIGHT_CYAN + " │" + RESET);
    }

    /**
     * Prints a table footer
     */
    public static void printTableFooter() {
        System.out.println(BRIGHT_CYAN + "└" + "─".repeat(70) + "┘" + RESET);
    }

    /**
     * Returns color based on percentage
     */
    public static String getPercentageColor(double percentage) {
        if (percentage >= 75)
            return GREEN;
        else if (percentage >= 60)
            return YELLOW;
        else
            return RED;
    }

    /**
     * Prints a colored percentage
     */
    public static void printPercentage(double percentage) {
        String color = getPercentageColor(percentage);
        System.out.print(color + String.format("%.2f%%", percentage) + RESET);
    }
}
