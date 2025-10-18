package org.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Main extends JFrame {
    private JTable fileTable;
    private FileTableModel tableModel;
    private JComboBox<String> actionComboBox;

    public Main() {
        setTitle("File Selector");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        // Create menu bar
        JMenuBar menuBar = new JMenuBar();

        // Create File menu
        JMenu fileMenu = new JMenu("File");

        // Create Open Folder menu item
        JMenuItem openFolderItem = new JMenuItem("Open Folder");
        openFolderItem.addActionListener(e -> openFolder());

        fileMenu.add(openFolderItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        // Create buttons
        JButton selectAllButton = new JButton("Select All");
        selectAllButton.addActionListener(e -> selectAll());

        JButton deselectAllButton = new JButton("Deselect All");
        deselectAllButton.addActionListener(e -> deselectAll());

        JButton processFilesButton = new JButton("Process Files");
        processFilesButton.addActionListener(e -> processFiles());

        // Create action combo box
        String[] actions = {"Print Filenames", "Copy Files", "Move Files", "Delete Files"};
        actionComboBox = new JComboBox<>(actions);

        buttonPanel.add(selectAllButton);
        buttonPanel.add(deselectAllButton);
        buttonPanel.add(new JLabel("Action:"));
        buttonPanel.add(actionComboBox);
        buttonPanel.add(processFilesButton);

        add(buttonPanel, BorderLayout.NORTH);

        // Create table model and table
        tableModel = new FileTableModel();
        fileTable = new JTable(tableModel);

        // Configure table
        fileTable.setRowHeight(25);
        fileTable.setFillsViewportHeight(true);

        // Set column widths
        fileTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // Checkbox
        fileTable.getColumnModel().getColumn(0).setMaxWidth(50);
        fileTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Type
        fileTable.getColumnModel().getColumn(2).setPreferredWidth(300); // Name
        fileTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Size
        fileTable.getColumnModel().getColumn(4).setPreferredWidth(150); // Date

        // Add custom cell renderer for highlighting checked rows
        CheckedRowRenderer renderer = new CheckedRowRenderer();
        fileTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
        fileTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
        fileTable.getColumnModel().getColumn(3).setCellRenderer(renderer);
        fileTable.getColumnModel().getColumn(4).setCellRenderer(renderer);

        // Add scroll pane
        JScrollPane scrollPane = new JScrollPane(fileTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void openFolder() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = fileChooser.getSelectedFile();
            loadFolderContents(selectedFolder);
        }
    }

    private void loadFolderContents(File folder) {
        tableModel.clear();

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                tableModel.addFile(file);
            }
        }
    }

    private void selectAll() {
        tableModel.setAllSelected(true);
    }

    private void deselectAll() {
        tableModel.setAllSelected(false);
    }

    private void processFiles() {
        List<File> selectedFiles = tableModel.getSelectedFiles();

        if (selectedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No files selected!",
                "Warning",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedAction = (String) actionComboBox.getSelectedItem();

        // Perform action on each selected file
        System.out.println("Processing " + selectedFiles.size() + " file(s) with action: " + selectedAction);
        for (File file : selectedFiles) {
            performAction(file, selectedAction);
        }
        System.out.println("Processing complete.");
    }

    private void performAction(File file, String action) {
        // This is where the action is performed on each file
        // Different actions based on the combo box selection
        switch (action) {
            case "Print Filenames":
                printFilename(file);
                break;
            case "Copy Files":
                copyFile(file);
                break;
            case "Move Files":
                moveFile(file);
                break;
            case "Delete Files":
                deleteFile(file);
                break;
            default:
                System.out.println("Unknown action: " + action);
        }
    }

    private void printFilename(File file) {
        System.out.println("  - " + file.getName());
    }

    private void copyFile(File file) {
        // TODO: Implement copy functionality
        System.out.println("  - Copy: " + file.getName() + " (not yet implemented)");
    }

    private void moveFile(File file) {
        // TODO: Implement move functionality
        System.out.println("  - Move: " + file.getName() + " (not yet implemented)");
    }

    private void deleteFile(File file) {
        // TODO: Implement delete functionality
        System.out.println("  - Delete: " + file.getName() + " (not yet implemented)");
    }

    // Custom table model for file data
    private static class FileTableModel extends AbstractTableModel {
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

    // Inner class to represent a file row with checkbox state
    private static class FileRow {
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

    // Custom cell renderer to highlight checked rows
    private class CheckedRowRenderer extends DefaultTableCellRenderer {
        private final Color HIGHLIGHT_COLOR = new Color(173, 216, 230); // Light blue

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Check if the row's checkbox is checked
            Boolean checked = (Boolean) tableModel.getValueAt(row, 0);
            if (checked != null && checked) {
                c.setBackground(HIGHLIGHT_COLOR);
            } else if (!isSelected) {
                c.setBackground(table.getBackground());
            }

            return c;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}