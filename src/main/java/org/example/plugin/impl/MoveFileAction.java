package org.example.plugin.impl;

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
    public void execute(File file) throws Exception {
        // TODO: Implement move functionality
        System.out.println("  - Move: " + file.getName() + " (not yet implemented)");
    }

    @Override
    public String getDescription() {
        return "Moves selected files to a target directory";
    }
}
