package org.example.plugin.impl;

import org.example.plugin.FileAction;

import java.io.File;

/**
 * Plugin that deletes files.
 * TODO: Implement actual delete functionality with confirmation
 */
public class DeleteFileAction implements FileAction {

    @Override
    public String getActionName() {
        return "Delete Files";
    }

    @Override
    public void execute(File file) throws Exception {
        // TODO: Implement delete functionality with confirmation dialog
        System.out.println("  - Delete: " + file.getName() + " (not yet implemented)");
    }

    @Override
    public String getDescription() {
        return "Deletes selected files permanently";
    }
}
