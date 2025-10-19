package org.example;

import org.example.config.UIConfig;
import org.example.plugin.ActionLogger;
import org.example.plugin.FileAction;
import org.example.plugin.PluginLoader;
import org.example.ui.PropertiesEditorDialog;

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
    private JComboBox<FileAction> actionComboBox;
    private final List<FileAction> availableActions;
    private JTextArea consoleArea;
    private JButton processFilesButton;
    private UIConfig config;
    private JSplitPane splitPane;
    private JMenuBar menuBar;
    private JButton selectAllButton;
    private JButton deselectAllButton;
    private List<JLabel> labels;

    public Main() {
        // Load UI configuration
        config = UIConfig.getInstance();

        setTitle(config.getWindowTitle());
        setSize(config.getWindowWidth(), config.getWindowHeight());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Load plugins
        availableActions = PluginLoader.loadPlugins();
        System.out.println("Loaded " + availableActions.size() + " plugin(s)");

        initComponents();
    }

    private void initComponents() {
        labels = new ArrayList<>();

        // Create menu bar
        menuBar = new JMenuBar();
        Font uiFont = config.getUIFont();
        menuBar.setFont(uiFont);

        // Create File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(uiFont);

        // Create Open Folder menu item
        JMenuItem openFolderItem = new JMenuItem("Open Folder");
        openFolderItem.setFont(uiFont);
        openFolderItem.addActionListener(e -> openFolder());

        fileMenu.add(openFolderItem);
        menuBar.add(fileMenu);

        // Create Settings menu
        JMenu settingsMenu = new JMenu("Settings");
        settingsMenu.setFont(uiFont);

        // Create UI Properties menu item
        JMenuItem uiPropertiesItem = new JMenuItem("UI Properties...");
        uiPropertiesItem.setFont(uiFont);
        uiPropertiesItem.addActionListener(e -> openPropertiesEditor());

        settingsMenu.add(uiPropertiesItem);
        menuBar.add(settingsMenu);

        setJMenuBar(menuBar);

        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        // Create buttons
        selectAllButton = new JButton("Select All");
        selectAllButton.setFont(uiFont);
        selectAllButton.addActionListener(e -> selectAll());

        deselectAllButton = new JButton("Deselect All");
        deselectAllButton.setFont(uiFont);
        deselectAllButton.addActionListener(e -> deselectAll());

        processFilesButton = new JButton("Process Files");
        processFilesButton.setFont(uiFont);
        processFilesButton.addActionListener(e -> processFiles());

        // Create action combo box with loaded plugins
        actionComboBox = new JComboBox<>();
        actionComboBox.setFont(uiFont);
        for (FileAction action : availableActions) {
            actionComboBox.addItem(action);
        }
        // Custom renderer to display action names
        actionComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof FileAction) {
                    setText(((FileAction) value).getActionName());
                }
                setFont(uiFont);
                return this;
            }
        });

        JLabel actionLabel = new JLabel("Action:");
        actionLabel.setFont(uiFont);
        labels.add(actionLabel);

        buttonPanel.add(selectAllButton);
        buttonPanel.add(deselectAllButton);
        buttonPanel.add(actionLabel);
        buttonPanel.add(actionComboBox);
        buttonPanel.add(processFilesButton);

        add(buttonPanel, BorderLayout.NORTH);

        // Create table model and table
        tableModel = new FileTableModel();
        fileTable = new JTable(tableModel);
        fileTable.setFont(uiFont);
        fileTable.getTableHeader().setFont(uiFont);

        // Configure table
        fileTable.setRowHeight(config.getTableRowHeight());
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

        // Add scroll pane for table
        JScrollPane tableScrollPane = new JScrollPane(fileTable);

        // Create console area
        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setFont(config.getConsoleFont());
        consoleArea.setBackground(config.getConsoleBgColor());
        consoleArea.setForeground(config.getConsoleTextColor());
        consoleArea.setBorder(BorderFactory.createLineBorder(config.getConsoleBorderColor()));
        JScrollPane consoleScrollPane = new JScrollPane(consoleArea);
        consoleScrollPane.setPreferredSize(new Dimension(0, 150));

        // Create split pane with table on top and console on bottom
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, consoleScrollPane);
        splitPane.setResizeWeight(config.getSplitPaneRatio());
        splitPane.setOneTouchExpandable(true);

        add(splitPane, BorderLayout.CENTER);
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

    private void openPropertiesEditor() {
        PropertiesEditorDialog dialog = new PropertiesEditorDialog(this);
        dialog.setVisible(true);

        // If changes were made, refresh the UI
        if (dialog.isChangesMade()) {
            refreshUI();
        }
    }

    private void refreshUI() {
        // Reload configuration from file
        config.reload();

        Font uiFont = config.getUIFont();

        // Update window properties
        setTitle(config.getWindowTitle());
        setSize(config.getWindowWidth(), config.getWindowHeight());

        // Update menu bar and menus
        menuBar.setFont(uiFont);
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu menu = menuBar.getMenu(i);
            menu.setFont(uiFont);
            for (int j = 0; j < menu.getItemCount(); j++) {
                JMenuItem item = menu.getItem(j);
                if (item != null) {
                    item.setFont(uiFont);
                }
            }
        }

        // Update buttons
        selectAllButton.setFont(uiFont);
        deselectAllButton.setFont(uiFont);
        processFilesButton.setFont(uiFont);

        // Update labels
        for (JLabel label : labels) {
            label.setFont(uiFont);
        }

        // Update combo box
        actionComboBox.setFont(uiFont);

        // Update table
        fileTable.setFont(uiFont);
        fileTable.getTableHeader().setFont(uiFont);
        fileTable.setRowHeight(config.getTableRowHeight());
        fileTable.invalidate();
        fileTable.repaint();

        // Update console area
        consoleArea.setFont(config.getConsoleFont());
        consoleArea.setBackground(config.getConsoleBgColor());
        consoleArea.setForeground(config.getConsoleTextColor());
        consoleArea.setBorder(BorderFactory.createLineBorder(config.getConsoleBorderColor()));

        // Force console area to recalculate layout with new font
        consoleArea.invalidate();
        consoleArea.getParent().revalidate();

        // Update split pane ratio
        splitPane.setResizeWeight(config.getSplitPaneRatio());
        splitPane.setDividerLocation(config.getSplitPaneRatio());

        // Force repaint of the entire frame
        repaint();
        revalidate();
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

        FileAction selectedAction = (FileAction) actionComboBox.getSelectedItem();
        if (selectedAction == null) {
            JOptionPane.showMessageDialog(this,
                "No action selected!",
                "Warning",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Clear console before processing
        consoleArea.setText("");

        // Create logger for plugins
        ActionLogger logger = new ConsoleLogger();

        // Disable the process button during execution
        processFilesButton.setEnabled(false);

        // Run processing in background thread
        final FileAction action = selectedAction;
        new Thread(() -> {
            logger.info("Processing " + selectedFiles.size() + " file(s) with action: " + action.getActionName());
            int successCount = 0;
            int errorCount = 0;

            for (File file : selectedFiles) {
                try {
                    action.execute(file, logger);
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    logger.error("Error processing " + file.getName() + ": " + e.getMessage(), e);
                }
            }

            logger.info("Processing complete. Success: " + successCount + ", Errors: " + errorCount);

            final int finalSuccessCount = successCount;
            final int finalErrorCount = errorCount;

            // Re-enable the process button on the UI thread
            SwingUtilities.invokeLater(() -> {
                processFilesButton.setEnabled(true);

                if (finalErrorCount > 0) {
                    JOptionPane.showMessageDialog(Main.this,
                        "Processing completed with errors.\nSuccess: " + finalSuccessCount + "\nErrors: " + finalErrorCount,
                        "Processing Result",
                        JOptionPane.WARNING_MESSAGE);
                }
            });
        }).start();
    }

    // Logger implementation that writes to the console area
    private class ConsoleLogger implements ActionLogger {
        @Override
        public void info(String message) {
            SwingUtilities.invokeLater(() -> {
                consoleArea.append("[INFO] " + message + "\n");
                consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
            });
        }

        @Override
        public void error(String message) {
            SwingUtilities.invokeLater(() -> {
                consoleArea.append("[ERROR] " + message + "\n");
                consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
            });
        }

        @Override
        public void error(String message, Throwable throwable) {
            SwingUtilities.invokeLater(() -> {
                consoleArea.append("[ERROR] " + message + "\n");
                if (throwable != null) {
                    consoleArea.append("  Exception: " + throwable.getClass().getName() + ": " + throwable.getMessage() + "\n");
                }
                consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
            });
        }
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
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Check if the row's checkbox is checked
            Boolean checked = (Boolean) tableModel.getValueAt(row, 0);
            if (checked != null && checked) {
                c.setBackground(config.getHighlightColor());
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