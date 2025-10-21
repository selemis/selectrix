package com.selectrix.plugin.impl;

import com.selectrix.plugin.ActionLogger;
import com.selectrix.plugin.FileAction;
import com.selectrix.plugin.ProcessingContext;

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
