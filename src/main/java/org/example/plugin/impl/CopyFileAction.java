package org.example.plugin.impl;

import org.example.plugin.ActionLogger;
import org.example.plugin.FileAction;

import java.io.File;

/**
 * Plugin that copies files to a target directory.
 * TODO: Implement actual copy functionality
 */
public class CopyFileAction implements FileAction {

    @Override
    public String getActionName() {
        return "Copy Files";
    }

    @Override
    public void execute(File file, ActionLogger logger) throws Exception {
        // TODO: Implement copy functionality
        logger.info("  - Copy: " + file.getName() + " (not yet implemented)");
    }

    @Override
    public String getDescription() {
        return "Copies selected files to a target directory";
    }
}
