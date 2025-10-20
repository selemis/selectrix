package org.example.processor;

import org.example.plugin.ActionLogger;
import org.example.plugin.FileAction;
import org.example.plugin.ProcessingContext;

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
        ProcessingContext context = null;

        try {
            // Phase 1: Before processing
            context = action.beforeProcessing(logger);

            // Phase 2: Process each file
            for (File file : files) {
                try {
                    action.execute(file, context, logger);
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    logger.error("Error processing " + file.getName() + ": " + e.getMessage(), e);
                }
            }

            // Phase 3: After processing
            action.afterProcessing(context, logger);

        } catch (Exception e) {
            // Error in before/after processing
            logger.error("Error in processing lifecycle: " + e.getMessage(), e);
            errorCount++;
        }

        logger.info("Processing complete. Success: " + successCount + ", Errors: " + errorCount);

        return new ProcessingResult(successCount, errorCount);
    }
}
