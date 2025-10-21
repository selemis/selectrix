package com.selectrix.logger;

import com.selectrix.plugin.ActionLogger;

import javax.swing.*;

public class ConsoleLogger implements ActionLogger {
    private final JTextArea consoleArea;

    public ConsoleLogger(JTextArea consoleArea) {
        this.consoleArea = consoleArea;
    }

    @Override
    public void info(String message) {
        SwingUtilities.invokeLater(() -> {
            consoleArea.append("[INFO] " + message + "\n");
            consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
        });
    }

    @Override
    public void error(String message) {
        SwingUtilities.invokeLater(() -> {
            consoleArea.append("[ERROR] " + message + "\n");
            consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
        });
    }

    @Override
    public void error(String message, Throwable throwable) {
        SwingUtilities.invokeLater(() -> {
            consoleArea.append("[ERROR] " + message + "\n");
            if (throwable != null) {
                consoleArea.append("  Exception: " + throwable.getClass().getName() + ": " + throwable.getMessage() + "\n");
            }
            consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
        });
    }
}
