package org.example.config;

import java.awt.*;
import java.io.*;
import java.util.Properties;

public class UIConfig {
    private static UIConfig instance;
    private Properties properties;
    private File propertiesFile;

    // Default values
    private static final String DEFAULT_WINDOW_WIDTH = "1000";
    private static final String DEFAULT_WINDOW_HEIGHT = "600";
    private static final String DEFAULT_WINDOW_TITLE = "File Selector";
    private static final String DEFAULT_HIGHLIGHT_COLOR = "173,216,230";
    private static final String DEFAULT_CONSOLE_BG_COLOR = "240,240,240";
    private static final String DEFAULT_CONSOLE_TEXT_COLOR = "60,60,60";
    private static final String DEFAULT_CONSOLE_BORDER_COLOR = "200,200,200";
    private static final String DEFAULT_CONSOLE_FONT_NAME = "Monospaced";
    private static final String DEFAULT_CONSOLE_FONT_SIZE = "12";
    private static final String DEFAULT_UI_FONT_NAME = "SansSerif";
    private static final String DEFAULT_UI_FONT_SIZE = "12";
    private static final String DEFAULT_TABLE_ROW_HEIGHT = "25";
    private static final String DEFAULT_SPLIT_PANE_RATIO = "0.7";

    private UIConfig() {
        properties = new Properties();
        loadProperties();
    }

    public static UIConfig getInstance() {
        if (instance == null) {
            instance = new UIConfig();
        }
        return instance;
    }

    public void reload() {
        properties.clear();
        loadProperties();
    }

    private void loadProperties() {
        // Try to load from user home directory first
        String userHome = System.getProperty("user.home");
        propertiesFile = new File(userHome, ".fileselector/ui.properties");

        // If it doesn't exist, try to load from resources
        if (!propertiesFile.exists()) {
            loadDefaultProperties();
            // Create the directory and save defaults
            propertiesFile.getParentFile().mkdirs();
            saveProperties();
        } else {
            try (FileInputStream fis = new FileInputStream(propertiesFile)) {
                properties.load(fis);
            } catch (IOException e) {
                System.err.println("Error loading properties file: " + e.getMessage());
                loadDefaultProperties();
            }
        }
    }

    private void loadDefaultProperties() {
        // Try to load from resources first
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("ui.properties")) {
            if (is != null) {
                properties.load(is);
                return;
            }
        } catch (IOException e) {
            // Fall through to use hardcoded defaults
        }

        // Use hardcoded defaults
        properties.setProperty("window.width", DEFAULT_WINDOW_WIDTH);
        properties.setProperty("window.height", DEFAULT_WINDOW_HEIGHT);
        properties.setProperty("window.title", DEFAULT_WINDOW_TITLE);
        properties.setProperty("highlight.color", DEFAULT_HIGHLIGHT_COLOR);
        properties.setProperty("console.background.color", DEFAULT_CONSOLE_BG_COLOR);
        properties.setProperty("console.text.color", DEFAULT_CONSOLE_TEXT_COLOR);
        properties.setProperty("console.border.color", DEFAULT_CONSOLE_BORDER_COLOR);
        properties.setProperty("console.font.name", DEFAULT_CONSOLE_FONT_NAME);
        properties.setProperty("console.font.size", DEFAULT_CONSOLE_FONT_SIZE);
        properties.setProperty("ui.font.name", DEFAULT_UI_FONT_NAME);
        properties.setProperty("ui.font.size", DEFAULT_UI_FONT_SIZE);
        properties.setProperty("table.row.height", DEFAULT_TABLE_ROW_HEIGHT);
        properties.setProperty("split.pane.ratio", DEFAULT_SPLIT_PANE_RATIO);
    }

    public void saveProperties() {
        try {
            propertiesFile.getParentFile().mkdirs();

            // Try to copy the formatted template from resources
            InputStream templateStream = getClass().getClassLoader().getResourceAsStream("ui.properties");
            if (templateStream != null) {
                // Copy the template file to user directory
                try (FileOutputStream fos = new FileOutputStream(propertiesFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = templateStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }
                templateStream.close();
            } else {
                // Fallback: write formatted properties manually
                writeFormattedProperties();
            }
        } catch (IOException e) {
            System.err.println("Error saving properties file: " + e.getMessage());
        }
    }

    private void writeFormattedProperties() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(propertiesFile))) {
            writer.write("# File Selector UI Configuration\n");
            writer.write("# This file contains customizable UI settings for the File Selector application\n");
            writer.write("\n");
            writer.write("# Window settings\n");
            writer.write("window.width=" + properties.getProperty("window.width", DEFAULT_WINDOW_WIDTH) + "\n");
            writer.write("window.height=" + properties.getProperty("window.height", DEFAULT_WINDOW_HEIGHT) + "\n");
            writer.write("window.title=" + properties.getProperty("window.title", DEFAULT_WINDOW_TITLE) + "\n");
            writer.write("\n");
            writer.write("# Colors (RGB format: red,green,blue where each value is 0-255)\n");
            writer.write("# Highlight color for selected rows in the file table\n");
            writer.write("highlight.color=" + properties.getProperty("highlight.color", DEFAULT_HIGHLIGHT_COLOR) + "\n");
            writer.write("\n");
            writer.write("# Console area colors\n");
            writer.write("console.background.color=" + properties.getProperty("console.background.color", DEFAULT_CONSOLE_BG_COLOR) + "\n");
            writer.write("console.text.color=" + properties.getProperty("console.text.color", DEFAULT_CONSOLE_TEXT_COLOR) + "\n");
            writer.write("console.border.color=" + properties.getProperty("console.border.color", DEFAULT_CONSOLE_BORDER_COLOR) + "\n");
            writer.write("\n");
            writer.write("# Font settings\n");
            writer.write("# Available fonts: Monospaced, SansSerif, Serif, Dialog, DialogInput\n");
            writer.write("\n");
            writer.write("# Console font (for log output area)\n");
            writer.write("console.font.name=" + properties.getProperty("console.font.name", DEFAULT_CONSOLE_FONT_NAME) + "\n");
            writer.write("console.font.size=" + properties.getProperty("console.font.size", DEFAULT_CONSOLE_FONT_SIZE) + "\n");
            writer.write("\n");
            writer.write("# UI font (for menus, buttons, table, and other UI elements)\n");
            writer.write("ui.font.name=" + properties.getProperty("ui.font.name", DEFAULT_UI_FONT_NAME) + "\n");
            writer.write("ui.font.size=" + properties.getProperty("ui.font.size", DEFAULT_UI_FONT_SIZE) + "\n");
            writer.write("\n");
            writer.write("# Table settings\n");
            writer.write("# Row height in pixels\n");
            writer.write("table.row.height=" + properties.getProperty("table.row.height", DEFAULT_TABLE_ROW_HEIGHT) + "\n");
            writer.write("\n");
            writer.write("# Split pane settings\n");
            writer.write("# Ratio between table and console (0.0-1.0, where 0.7 means 70% for table, 30% for console)\n");
            writer.write("split.pane.ratio=" + properties.getProperty("split.pane.ratio", DEFAULT_SPLIT_PANE_RATIO) + "\n");
        }
    }

    // Getters
    public int getWindowWidth() {
        return Integer.parseInt(properties.getProperty("window.width", DEFAULT_WINDOW_WIDTH));
    }

    public int getWindowHeight() {
        return Integer.parseInt(properties.getProperty("window.height", DEFAULT_WINDOW_HEIGHT));
    }

    public String getWindowTitle() {
        return properties.getProperty("window.title", DEFAULT_WINDOW_TITLE);
    }

    public Color getHighlightColor() {
        return parseColor(properties.getProperty("highlight.color", DEFAULT_HIGHLIGHT_COLOR));
    }

    public Color getConsoleBgColor() {
        return parseColor(properties.getProperty("console.background.color", DEFAULT_CONSOLE_BG_COLOR));
    }

    public Color getConsoleTextColor() {
        return parseColor(properties.getProperty("console.text.color", DEFAULT_CONSOLE_TEXT_COLOR));
    }

    public Color getConsoleBorderColor() {
        return parseColor(properties.getProperty("console.border.color", DEFAULT_CONSOLE_BORDER_COLOR));
    }

    public Font getConsoleFont() {
        String fontName = properties.getProperty("console.font.name", DEFAULT_CONSOLE_FONT_NAME);
        int fontSize = Integer.parseInt(properties.getProperty("console.font.size", DEFAULT_CONSOLE_FONT_SIZE));
        return new Font(fontName, Font.PLAIN, fontSize);
    }

    public Font getUIFont() {
        String fontName = properties.getProperty("ui.font.name", DEFAULT_UI_FONT_NAME);
        int fontSize = Integer.parseInt(properties.getProperty("ui.font.size", DEFAULT_UI_FONT_SIZE));
        return new Font(fontName, Font.PLAIN, fontSize);
    }

    public int getTableRowHeight() {
        return Integer.parseInt(properties.getProperty("table.row.height", DEFAULT_TABLE_ROW_HEIGHT));
    }

    public double getSplitPaneRatio() {
        return Double.parseDouble(properties.getProperty("split.pane.ratio", DEFAULT_SPLIT_PANE_RATIO));
    }

    // Setters
    public void setWindowWidth(int width) {
        properties.setProperty("window.width", String.valueOf(width));
    }

    public void setWindowHeight(int height) {
        properties.setProperty("window.height", String.valueOf(height));
    }

    public void setWindowTitle(String title) {
        properties.setProperty("window.title", title);
    }

    public void setHighlightColor(Color color) {
        properties.setProperty("highlight.color", colorToString(color));
    }

    public void setConsoleBgColor(Color color) {
        properties.setProperty("console.background.color", colorToString(color));
    }

    public void setConsoleTextColor(Color color) {
        properties.setProperty("console.text.color", colorToString(color));
    }

    public void setConsoleBorderColor(Color color) {
        properties.setProperty("console.border.color", colorToString(color));
    }

    public void setConsoleFont(String fontName, int fontSize) {
        properties.setProperty("console.font.name", fontName);
        properties.setProperty("console.font.size", String.valueOf(fontSize));
    }

    public void setUIFont(String fontName, int fontSize) {
        properties.setProperty("ui.font.name", fontName);
        properties.setProperty("ui.font.size", String.valueOf(fontSize));
    }

    public void setTableRowHeight(int height) {
        properties.setProperty("table.row.height", String.valueOf(height));
    }

    public void setSplitPaneRatio(double ratio) {
        properties.setProperty("split.pane.ratio", String.valueOf(ratio));
    }

    // Utility methods
    private Color parseColor(String colorStr) {
        try {
            String[] rgb = colorStr.split(",");
            if (rgb.length == 3) {
                return new Color(
                    Integer.parseInt(rgb[0].trim()),
                    Integer.parseInt(rgb[1].trim()),
                    Integer.parseInt(rgb[2].trim())
                );
            }
        } catch (Exception e) {
            System.err.println("Error parsing color: " + colorStr);
        }
        return Color.WHITE;
    }

    private String colorToString(Color color) {
        return color.getRed() + "," + color.getGreen() + "," + color.getBlue();
    }

    public File getPropertiesFile() {
        return propertiesFile;
    }

    public Properties getProperties() {
        return properties;
    }
}
