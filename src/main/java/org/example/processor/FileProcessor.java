package org.example.processor;

import org.example.plugin.ActionLogger;
import org.example.plugin.FileAction;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class FileProcessor {

    /**
     * Process files with the given action in a background thread.
     *
     * @param files The list of files to process
     * @param action The action to execute on each file
     * @param logger The logger for output
     * @param onComplete Callback executed on UI thread when processing completes
     */
    public void processFilesAsync(List<File> files, FileAction action, ActionLogger logger,
                                   Consumer<ProcessingResult> onComplete) {
        new Thread(() -> {
            ProcessingResult result = processFiles(files, action, logger);
            onComplete.accept(result);
        }).start();
    }

    /**
     * Process files synchronously with the given action.
     *
     * @param files The list of files to process
     * @param action The action to execute on each file
     * @param logger The logger for output
     * @return ProcessingResult containing success and error counts
     */
    public ProcessingResult processFiles(List<File> files, FileAction action, ActionLogger logger) {
        logger.info("Processing " + files.size() + " file(s) with action: " + action.getActionName());

        int successCount = 0;
        int errorCount = 0;

        for (File file : files) {
            try {
                action.execute(file, logger);
                successCount++;
            } catch (Exception e) {
                errorCount++;
                logger.error("Error processing " + file.getName() + ": " + e.getMessage(), e);
            }
        }

        logger.info("Processing complete. Success: " + successCount + ", Errors: " + errorCount);

        return new ProcessingResult(successCount, errorCount);
    }
}
