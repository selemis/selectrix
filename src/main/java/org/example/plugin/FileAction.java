package org.example.plugin;

import java.io.File;

/**
 * Interface for file action plugins.
 * Each plugin implements this interface to define a custom action
 * that can be performed on selected files.
 */
public interface FileAction {
    /**
     * Returns the display name of this action that will appear in the UI.
     * @return The name of the action (e.g., "Print Filenames", "Copy Files")
     */
    String getActionName();

    /**
     * Executes the action on the given file.
     * @param file The file to process
     * @param logger Logger for outputting messages to the GUI console
     * @throws Exception if the action fails
     */
    void execute(File file, ActionLogger logger) throws Exception;

    /**
     * Returns a description of what this action does.
     * @return A brief description of the action
     */
    String getDescription();
}
