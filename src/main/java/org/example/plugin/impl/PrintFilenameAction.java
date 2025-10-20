package org.example.plugin.impl;

import org.example.plugin.ActionLogger;
import org.example.plugin.FileAction;
import org.example.plugin.ProcessingContext;

import java.io.File;

/**
 * Plugin that prints the filename to the console.
 */
public class PrintFilenameAction implements FileAction {

    @Override
    public String getActionName() {
        return "Print Filenames";
    }

    @Override
    public String getDescription() {
        return "Prints the filename to the console";
    }

    @Override
    public void execute(File file, ProcessingContext context, ActionLogger logger) throws Exception {
        logger.info("  - " + file.getName());
    }
}
