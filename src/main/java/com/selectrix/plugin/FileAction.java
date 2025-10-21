package com.selectrix.plugin;

import java.io.File;

/**
 * Interface for file action plugins.
 * Each plugin implements this interface to define a custom action
 * that can be performed on selected files.
 *
 * <p>Plugins have a lifecycle with three phases:
 * <ol>
 *   <li>beforeProcessing() - Initialize state, return context (optional)</li>
 *   <li>execute() - Called once per file (required)</li>
 *   <li>afterProcessing() - Finalize, cleanup (optional)</li>
 * </ol>
 */
public interface FileAction {
    /**
     * Returns the display name of this action that will appear in the UI.
     * @return The name of the action (e.g., "Print Filenames", "Concatenate PDFs")
     */
    String getActionName();

    /**
     * Returns a description of what this action does.
     * @return A brief description of the action
     */
    String getDescription();

    /**
     * Called once before processing begins.
     * Plugin can create and return a context object to carry state between execute() calls.
     *
     * @param logger Logger for outputting messages to the GUI console
     * @return Context object (subclass of ProcessingContext) or null if no context needed
     * @throws Exception if initialization fails (stops processing)
     */
    default ProcessingContext beforeProcessing(ActionLogger logger) throws Exception {
        return null;
    }

    /**
     * Executes the action on the given file.
     * Called once for each selected file.
     *
     * @param file The file to process
     * @param context The context object returned from beforeProcessing() (may be null)
     * @param logger Logger for outputting messages to the GUI console
     * @throws Exception if the action fails for this file
     */
    void execute(File file, ProcessingContext context, ActionLogger logger) throws Exception;

    /**
     * Called once after all files have been processed.
     * Plugin can finalize work, cleanup resources, save results, etc.
     *
     * @param context The context object returned from beforeProcessing() (may be null)
     * @param logger Logger for outputting messages to the GUI console
     * @throws Exception if finalization fails
     */
    default void afterProcessing(ProcessingContext context, ActionLogger logger) throws Exception {
        // Optional - override if needed
    }
}
