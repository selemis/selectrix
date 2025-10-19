package org.example.ui;

import org.example.config.UIConfig;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Comparator;

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
        getColumnModel().getColumn(0).setPreferredWidth(50);  // Checkbox
        getColumnModel().getColumn(0).setMaxWidth(50);
        getColumnModel().getColumn(1).setPreferredWidth(80);  // Type
        getColumnModel().getColumn(2).setPreferredWidth(300); // Name
        getColumnModel().getColumn(3).setPreferredWidth(100); // Size
        getColumnModel().getColumn(4).setPreferredWidth(150); // Date
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
