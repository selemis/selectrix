package org.example.ui;

import org.example.config.UIConfig;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;

public class FileTable extends JTable {
    private UIConfig config;

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

            // Check if the row's checkbox is checked
            Boolean checked = (Boolean) table.getModel().getValueAt(row, 0);
            if (checked != null && checked) {
                c.setBackground(config.getHighlightColor());
            } else if (!isSelected) {
                c.setBackground(table.getBackground());
            }

            return c;
        }
    }
}
