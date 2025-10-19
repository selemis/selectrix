package org.example.model;

import java.io.File;

public class FileRow {
    private final File file;
    private boolean selected;

    public FileRow(File file) {
        this.file = file;
        this.selected = false;
    }

    public File getFile() {
        return file;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
