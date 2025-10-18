package org.example.plugin.impl;

import org.example.plugin.ActionLogger;
import org.example.plugin.FileAction;

import java.io.File;

/**
 * Plugin that moves files to a target directory.
 * TODO: Implement actual move functionality
 */
public class MoveFileAction implements FileAction {

    @Override
    public String getActionName() {
        return "Move Files";
    }

    @Override
    public void execute(File file, ActionLogger logger) throws Exception {
        // TODO: Implement move functionality
        logger.info("  - Move: " + file.getName() + " (not yet implemented)");
    }

    @Override
    public String getDescription() {
        return "Moves selected files to a target directory";
    }
}
