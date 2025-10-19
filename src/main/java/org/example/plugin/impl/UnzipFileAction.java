package org.example.plugin.impl;

import org.example.plugin.ActionLogger;
import org.example.plugin.FileAction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Plugin that unzips ZIP files to a directory with the same name as the ZIP file.
 */
public class UnzipFileAction implements FileAction {

    @Override
    public String getActionName() {
        return "Unzip Files";
    }

    @Override
    public void execute(File file, ActionLogger logger) throws Exception {
        // Check if the file is a ZIP file
        if (!file.getName().toLowerCase().endsWith(".zip")) {
            logger.error("File is not a ZIP file: " + file.getName());
            return;
        }

        if (!file.exists() || !file.isFile()) {
            logger.error("File does not exist or is not a regular file: " + file.getName());
            return;
        }

        // Determine the output directory (same name as ZIP file without .zip extension)
        String zipFileName = file.getName();
        String dirName = zipFileName.substring(0, zipFileName.length() - 4);
        File outputDir = new File(file.getParent(), dirName);

        // Create output directory if it doesn't exist
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                logger.error("Failed to create output directory: " + outputDir.getAbsolutePath());
                return;
            }
        }

        logger.info("Unzipping " + file.getName() + " to " + outputDir.getName() + "/");

        int extractedCount = 0;
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry zipEntry;
            byte[] buffer = new byte[8192];

            while ((zipEntry = zis.getNextEntry()) != null) {
                File newFile = newFile(outputDir, zipEntry);

                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory: " + newFile);
                    }
                } else {
                    // Create parent directories if needed
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory: " + parent);
                    }

                    // Extract file
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                    extractedCount++;
                }
                zis.closeEntry();
            }
        }

        logger.info("Successfully extracted " + extractedCount + " file(s) from " + file.getName());
    }

    @Override
    public String getDescription() {
        return "Unzips ZIP files to a directory with the same name";
    }

    /**
     * Creates a new File object for a zip entry, preventing Zip Slip vulnerability.
     *
     * @param destinationDir The destination directory
     * @param zipEntry The zip entry
     * @return A safe File object
     * @throws IOException if the entry is outside the target directory
     */
    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
