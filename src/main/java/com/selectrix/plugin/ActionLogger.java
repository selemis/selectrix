package com.selectrix.plugin;

/**
 * Logger interface for plugins to output messages to the GUI console.
 */
public interface ActionLogger {
    /**
     * Logs an informational message.
     * @param message The message to log
     */
    void info(String message);

    /**
     * Logs an error message.
     * @param message The error message to log
     */
    void error(String message);

    /**
     * Logs an error message with an exception.
     * @param message The error message to log
     * @param throwable The exception that occurred
     */
    void error(String message, Throwable throwable);
}
