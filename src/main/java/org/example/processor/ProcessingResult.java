package org.example.processor;

public class ProcessingResult {
    private final int successCount;
    private final int errorCount;

    public ProcessingResult(int successCount, int errorCount) {
        this.successCount = successCount;
        this.errorCount = errorCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public boolean hasErrors() {
        return errorCount > 0;
    }

    public int getTotalProcessed() {
        return successCount + errorCount;
    }
}
