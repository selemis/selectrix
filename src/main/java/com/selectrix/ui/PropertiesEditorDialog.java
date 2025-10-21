package com.selectrix.ui;

import com.selectrix.config.UIConfig;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class PropertiesEditorDialog extends JDialog {
    private UIConfig config;
    private boolean changesMade = false;

    public PropertiesEditorDialog(Frame parent) {
        super(parent, "Edit UI Properties", true);
        config = UIConfig.getInstance();

        setSize(700, 500);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Get UI font for all components
        Font uiFont = config.getUIFont();

        // Create info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        JLabel infoLabel = new JLabel("<html><b>Edit UI Configuration</b><br/>" +
                "Properties file location: " + config.getPropertiesFile().getAbsolutePath() + "</html>");
        infoLabel.setFont(uiFont);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.add(infoLabel, BorderLayout.NORTH);
        add(infoPanel, BorderLayout.NORTH);

        // Create editor panel with text area
        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JLabel editorLabel = new JLabel("Edit properties below:");
        editorLabel.setFont(uiFont);
        editorPanel.add(editorLabel, BorderLayout.NORTH);

        // Create text area for editing properties
        JTextArea textArea = new JTextArea();
        textArea.setFont(config.getConsoleFont()); // Use console font for monospaced text
        textArea.setTabSize(4);

        // Load current properties into text area
        StringBuilder content = new StringBuilder();
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader(config.getPropertiesFile()));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            // If file doesn't exist, generate default content
            content.append("# File Selector UI Configuration\n");
            content.append("# This file contains customizable UI settings for the File Selector application\n\n");
            content.append("# Window settings\n");
            content.append("window.width=").append(config.getWindowWidth()).append("\n");
            content.append("window.height=").append(config.getWindowHeight()).append("\n");
            content.append("window.title=").append(config.getWindowTitle()).append("\n\n");
            content.append("# Colors (RGB format: red,green,blue where each value is 0-255)\n");
            content.append("highlight.color=").append(colorToString(config.getHighlightColor())).append("\n");
            content.append("console.background.color=").append(colorToString(config.getConsoleBgColor())).append("\n");
            content.append("console.text.color=").append(colorToString(config.getConsoleTextColor())).append("\n");
            content.append("console.border.color=").append(colorToString(config.getConsoleBorderColor())).append("\n\n");
            content.append("# Font settings\n");
            content.append("font.name=").append(config.getConsoleFont().getName()).append("\n");
            content.append("font.size=").append(config.getConsoleFont().getSize()).append("\n\n");
            content.append("# Table settings\n");
            content.append("table.row.height=").append(config.getTableRowHeight()).append("\n\n");
            content.append("# Split pane settings\n");
            content.append("split.pane.ratio=").append(config.getSplitPaneRatio()).append("\n");
        }

        textArea.setText(content.toString());
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        editorPanel.add(scrollPane, BorderLayout.CENTER);

        add(editorPanel, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JButton saveButton = new JButton("Save");
        saveButton.setFont(uiFont);
        saveButton.addActionListener(e -> {
            try {
                // Save the text content to the properties file
                java.io.FileWriter writer = new java.io.FileWriter(config.getPropertiesFile());
                writer.write(textArea.getText());
                writer.close();

                // Mark that changes were made
                changesMade = true;

                JOptionPane.showMessageDialog(this,
                    "Properties saved successfully!\nChanges will be applied immediately.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

                dispose();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error saving properties: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(uiFont);
        cancelButton.addActionListener(e -> dispose());

        JButton openFileButton = new JButton("Open in External Editor");
        openFileButton.setFont(uiFont);
        openFileButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(config.getPropertiesFile());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "Could not open file in external editor: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(openFileButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private String colorToString(Color color) {
        return color.getRed() + "," + color.getGreen() + "," + color.getBlue();
    }

    public boolean isChangesMade() {
        return changesMade;
    }
}
