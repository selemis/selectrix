package org.example.ui;

import org.example.config.UIConfig;
import org.example.model.FileTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FileTable extends JTable {
    private UIConfig config;
    private TableRowSorter<TableModel> sorter;

    public FileTable(TableModel model, UIConfig config) {
        super(model);
        this.config = config;
        configureTable();
    }

    private void configureTable() {
        Font uiFont = config.getUIFont();
        setFonts(uiFont);
        configureTableAppearance();
        setColumnWidths();
        customCellRenderer();
        enableSorting();
    }

    private void enableSorting() {
        sorter = new TableRowSorter<>(getModel());
        setRowSorter(sorter);
        configureSortComparators();
    }

    private void configureSortComparators() {
        // Column 0: Checkbox - disable sorting
        sorter.setSortable(0, false);

        // Column 1: Type (Directory/File) - String comparison
        sorter.setComparator(1, Comparator.naturalOrder());

        // Column 2: Name - String comparison
        sorter.setComparator(2, Comparator.naturalOrder());

        // Column 3: Size - Parse file size for proper numeric sorting
        sorter.setComparator(3, new FileSizeComparator());

        // Column 4: Modified Date - String date comparison (format: yyyy-MM-dd HH:mm:ss)
        sorter.setComparator(4, Comparator.naturalOrder());
    }

    private void setFonts(Font uiFont) {
        setFont(uiFont);
        getTableHeader().setFont(uiFont);
    }

    private void configureTableAppearance() {
        setRowHeight(config.getTableRowHeight());
        setFillsViewportHeight(true);
    }

    private void setColumnWidths() {
        // Set initial auto-resize mode
        setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        // Set minimum widths for each column
        getColumnModel().getColumn(0).setMinWidth(60);  // Checkbox (enough for "Select" header)
        getColumnModel().getColumn(0).setMaxWidth(80);  // Allow some flexibility
        getColumnModel().getColumn(1).setMinWidth(80);  // Type
        getColumnModel().getColumn(2).setMinWidth(150); // Name
        getColumnModel().getColumn(3).setMinWidth(80);  // Size
        getColumnModel().getColumn(4).setMinWidth(140); // Date
    }

    /**
     * Auto-resize all columns to fit their content and headers.
     * This should be called after data is loaded.
     */
    public void autoResizeColumns() {
        for (int column = 0; column < getColumnCount(); column++) {
            TableColumn tableColumn = getColumnModel().getColumn(column);
            int preferredWidth = calculateOptimalColumnWidth(column);
            tableColumn.setPreferredWidth(preferredWidth);
        }
    }

    /**
     * Calculate the optimal width for a column based on header and content.
     */
    private int calculateOptimalColumnWidth(int columnIndex) {
        int maxWidth = 0;
        int margin = 10; // Extra margin for padding

        // Check header width
        TableColumn column = getColumnModel().getColumn(columnIndex);
        TableCellRenderer headerRenderer = column.getHeaderRenderer();
        if (headerRenderer == null) {
            headerRenderer = getTableHeader().getDefaultRenderer();
        }

        Object headerValue = column.getHeaderValue();
        Component headerComp = headerRenderer.getTableCellRendererComponent(
                this, headerValue, false, false, 0, columnIndex);
        maxWidth = headerComp.getPreferredSize().width;

        // Check content width (sample first 100 rows for performance)
        int rowsToCheck = Math.min(100, getRowCount());
        for (int row = 0; row < rowsToCheck; row++) {
            TableCellRenderer cellRenderer = getCellRenderer(row, columnIndex);
            Component comp = prepareRenderer(cellRenderer, row, columnIndex);
            maxWidth = Math.max(maxWidth, comp.getPreferredSize().width);
        }

        // Add margin and respect min/max constraints
        maxWidth += margin;

        // Apply column-specific constraints
        if (columnIndex == 0) {
            // Checkbox column - fit header and checkbox, with min/max
            return Math.min(Math.max(maxWidth, 60), 80);
        } else if (columnIndex == 1) {
            // Type column - limited width
            return Math.min(maxWidth, 100);
        } else if (columnIndex == 2) {
            // Name column - allow to grow but have max
            return Math.min(Math.max(maxWidth, 200), 600);
        } else if (columnIndex == 3) {
            // Size column - limited width
            return Math.min(maxWidth, 120);
        } else if (columnIndex == 4) {
            // Date column - fixed reasonable width
            return Math.max(maxWidth, 160);
        }

        return maxWidth;
    }

    // Add custom cell renderer for highlighting checked rows
    private void customCellRenderer() {
        CheckedRowRenderer renderer = new CheckedRowRenderer();
        getColumnModel().getColumn(1).setCellRenderer(renderer);
        getColumnModel().getColumn(2).setCellRenderer(renderer);
        getColumnModel().getColumn(3).setCellRenderer(renderer);
        getColumnModel().getColumn(4).setCellRenderer(renderer);
    }

    public void refreshConfiguration() {
        Font uiFont = config.getUIFont();
        setFonts(uiFont);
        setRowHeight(config.getTableRowHeight());
        invalidate();
        repaint();
    }

    /**
     * Get selected files in the current view order (respecting sorting).
     * This ensures files are processed in the order they appear in the table.
     */
    public List<File> getSelectedFilesInViewOrder() {
        List<File> selectedFiles = new ArrayList<>();
        FileTableModel model = (FileTableModel) getModel();

        // Iterate through rows in view order (as they appear in the sorted table)
        for (int viewRow = 0; viewRow < getRowCount(); viewRow++) {
            // Convert view row to model row
            int modelRow = convertRowIndexToModel(viewRow);

            // Check if this row is selected
            Boolean isSelected = (Boolean) model.getValueAt(modelRow, 0);
            if (isSelected != null && isSelected) {
                // Get the file from the Name column (column 2)
                String fileName = (String) model.getValueAt(modelRow, 2);
                // We need to get the actual File object from the model
                // For now, we'll need to add a method to FileTableModel to get the File by row index
                selectedFiles.add(model.getFileAtRow(modelRow));
            }
        }

        return selectedFiles;
    }

    private class CheckedRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Convert view row to model row to handle sorting
            int modelRow = table.convertRowIndexToModel(row);

            // Check if the row's checkbox is checked
            Boolean checked = (Boolean) table.getModel().getValueAt(modelRow, 0);
            if (checked != null && checked) {
                c.setBackground(config.getHighlightColor());
            } else if (!isSelected) {
                c.setBackground(table.getBackground());
            }

            return c;
        }
    }

    /**
     * Custom comparator for file sizes.
     * Handles formats like "1.5 KB", "2.3 MB", "500 B", etc.
     */
    private static class FileSizeComparator implements Comparator<String> {
        @Override
        public int compare(String size1, String size2) {
            // Empty strings (for directories) should go first
            if (size1.isEmpty() && size2.isEmpty()) return 0;
            if (size1.isEmpty()) return -1;
            if (size2.isEmpty()) return 1;

            long bytes1 = parseFileSize(size1);
            long bytes2 = parseFileSize(size2);

            return Long.compare(bytes1, bytes2);
        }

        private long parseFileSize(String sizeStr) {
            try {
                String[] parts = sizeStr.trim().split("\\s+");
                if (parts.length != 2) return 0;

                double value = Double.parseDouble(parts[0]);
                String unit = parts[1].toUpperCase();

                switch (unit) {
                    case "B":
                        return (long) value;
                    case "KB":
                        return (long) (value * 1024);
                    case "MB":
                        return (long) (value * 1024 * 1024);
                    case "GB":
                        return (long) (value * 1024 * 1024 * 1024);
                    default:
                        return 0;
                }
            } catch (Exception e) {
                return 0;
            }
        }
    }
}
