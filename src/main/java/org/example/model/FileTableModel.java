package org.example.model;

import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Select", "Type", "Name", "Size", "Modified Date"};
    private final List<FileRow> rows = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Boolean.class;
            case 1:
            case 2:
            case 3:
            case 4:
                return String.class;
            default:
                return Object.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0; // Only checkbox column is editable
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        FileRow row = rows.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return row.isSelected();
            case 1:
                return row.getFile().isDirectory() ? "Directory" : "File";
            case 2:
                return row.getFile().getName();
            case 3:
                return row.getFile().isDirectory() ? "" : formatFileSize(row.getFile().length());
            case 4:
                return dateFormat.format(new Date(row.getFile().lastModified()));
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            rows.get(rowIndex).setSelected((Boolean) aValue);
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    public void addFile(File file) {
        rows.add(new FileRow(file));
        fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
    }

    public void clear() {
        rows.clear();
        fireTableDataChanged();
    }

    public void setAllSelected(boolean selected) {
        for (FileRow row : rows) {
            row.setSelected(selected);
        }
        fireTableDataChanged();
    }

    public List<File> getSelectedFiles() {
        List<File> selectedFiles = new ArrayList<>();
        for (FileRow row : rows) {
            if (row.isSelected()) {
                selectedFiles.add(row.getFile());
            }
        }
        return selectedFiles;
    }

    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
}
