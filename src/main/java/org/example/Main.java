package org.example;

import org.example.config.UIConfig;
import org.example.logger.ConsoleLogger;
import org.example.model.FileTableModel;
import org.example.plugin.ActionLogger;
import org.example.plugin.FileAction;
import org.example.plugin.PluginLoader;
import org.example.processor.FileProcessor;
import org.example.ui.FileTable;
import org.example.ui.PropertiesEditorDialog;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main extends JFrame {
    private FileTable fileTable;
    private FileTableModel tableModel;
    private JComboBox<FileAction> actionComboBox;
    private List<FileAction> availableActions;
    private JTextArea consoleArea;
    private JButton processFilesButton;
    private UIConfig config;
    private JSplitPane splitPane;
    private JMenuBar menuBar;
    private JButton selectAllButton;
    private JButton deselectAllButton;
    private JButton selectFilesButton;
    private JButton selectDirsButton;
    private List<JLabel> labels;
    private FileProcessor fileProcessor;

    public Main() {
        loadUIConfiguration();
        loadPlugins();
        fileProcessor = new FileProcessor();
        initComponents();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }

    private void loadUIConfiguration() {
        config = UIConfig.getInstance();
        updateWindowProperties();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void loadPlugins() {
        availableActions = PluginLoader.loadPlugins();
        System.out.println("Loaded " + availableActions.size() + " plugin(s)");
    }

    private void initComponents() {
        labels = new ArrayList<>();
        Font uiFont = config.getUIFont();
        createMenuBar(uiFont);
        setJMenuBar(menuBar);
        add(createButtonPanel(uiFont), BorderLayout.NORTH);
        createFileTable();
        createSplitPlane(new JScrollPane(fileTable), createConsolePane());
        add(splitPane, BorderLayout.CENTER);
    }

    private void createMenuBar(Font uiFont) {
        menuBar = new JMenuBar();
        menuBar.setFont(uiFont);
        menuBar.add(createFileMenu(uiFont));
        menuBar.add(createSettingsMenu(uiFont));
    }

    private JPanel createButtonPanel(Font uiFont) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        createButtons(uiFont);
        createActionCombo(uiFont);

        buttonPanel.add(selectAllButton);
        buttonPanel.add(deselectAllButton);
        buttonPanel.add(selectFilesButton);
        buttonPanel.add(selectDirsButton);
        buttonPanel.add(createActionLabel(uiFont));
        buttonPanel.add(actionComboBox);
        buttonPanel.add(processFilesButton);
        return buttonPanel;
    }

    private void createFileTable() {
        tableModel = new FileTableModel();
        fileTable = new FileTable(tableModel, config);
    }

    // Create split pane with table on top and console on bottom
    private void createSplitPlane(JScrollPane tableScrollPane, JScrollPane consoleScrollPane) {
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, consoleScrollPane);
        splitPane.setResizeWeight(config.getSplitPaneRatio());
        splitPane.setOneTouchExpandable(true);
    }

    private JScrollPane createConsolePane() {
        createConsoleArea();
        JScrollPane consoleScrollPane = new JScrollPane(consoleArea);
        consoleScrollPane.setPreferredSize(new Dimension(0, 150));
        return consoleScrollPane;
    }

    private JMenu createFileMenu(Font uiFont) {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(uiFont);
        fileMenu.add(createOpenFolderMenuItem(uiFont));
        return fileMenu;
    }

    private JMenu createSettingsMenu(Font uiFont) {
        JMenu settingsMenu = new JMenu("Settings");
        settingsMenu.setFont(uiFont);
        settingsMenu.add(createIOPropertiesMenuItem(uiFont));
        return settingsMenu;
    }

    private void createButtons(Font uiFont) {
        selectAllButton = new JButton("Select All");
        selectAllButton.setFont(uiFont);
        selectAllButton.addActionListener(e -> selectAll());

        deselectAllButton = new JButton("Deselect All");
        deselectAllButton.setFont(uiFont);
        deselectAllButton.addActionListener(e -> deselectAll());

        selectFilesButton = new JButton("Select Files");
        selectFilesButton.setFont(uiFont);
        selectFilesButton.addActionListener(e -> selectFiles());

        selectDirsButton = new JButton("Select Dirs");
        selectDirsButton.setFont(uiFont);
        selectDirsButton.addActionListener(e -> selectDirs());

        processFilesButton = new JButton("Process Files");
        processFilesButton.setFont(uiFont);
        processFilesButton.addActionListener(e -> processFiles());
    }

    private void createActionCombo(Font uiFont) {
        actionComboBox = new JComboBox<>();
        updateComboBox(uiFont);
        for (FileAction action : availableActions) {
            actionComboBox.addItem(action);
        }
        actionComboBox.setRenderer(customerRenderForActionNames(uiFont));
    }

    private JLabel createActionLabel(Font uiFont) {
        JLabel actionLabel = new JLabel("Action:");
        actionLabel.setFont(uiFont);
        labels.add(actionLabel);
        return actionLabel;
    }

    private JMenuItem createOpenFolderMenuItem(Font uiFont) {
        JMenuItem openFolderItem = new JMenuItem("Open Folder");
        openFolderItem.setFont(uiFont);
        openFolderItem.addActionListener(e -> openFolder());
        return openFolderItem;
    }

    private JMenuItem createIOPropertiesMenuItem(Font uiFont) {
        JMenuItem uiPropertiesItem = new JMenuItem("UI Properties...");
        uiPropertiesItem.setFont(uiFont);
        uiPropertiesItem.addActionListener(e -> openPropertiesEditor());
        return uiPropertiesItem;
    }

    private DefaultListCellRenderer customerRenderForActionNames(Font uiFont) {
        return new DefaultListCellRenderer() {
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
        };
    }

    private void openFolder() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // Apply UI font to file chooser
        applyFontToFileChooser(fileChooser, config.getUIFont());

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = fileChooser.getSelectedFile();
            loadFolderContents(selectedFolder);
        }
    }

    /**
     * Recursively apply font to all components in the file chooser.
     */
    private void applyFontToFileChooser(JFileChooser fileChooser, Font font) {
        // Set font on the file chooser itself
        fileChooser.setFont(font);

        // Recursively apply to all child components
        applyFontToComponent(fileChooser, font);
    }

    /**
     * Recursively apply font to a component and all its children.
     */
    private void applyFontToComponent(Component component, Font font) {
        component.setFont(font);

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                applyFontToComponent(child, font);
            }
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

    private void loadFolderContents(File folder) {
        tableModel.clear();

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                tableModel.addFile(file);
            }
        }

        // Auto-resize columns to fit content after loading files
        SwingUtilities.invokeLater(() -> fileTable.autoResizeColumns());
    }

    private void refreshUI() {
        reloadConfiguration();
        Font uiFont = config.getUIFont();
        updateWindowProperties();
        updateMenus(uiFont);
        updateButtons(uiFont);
        updateLabels(uiFont);
        updateComboBox(uiFont);
        updateTable();
        updateConsoleArea();
        updateSplitPaneRatio();
        repaintTheFrame();
    }

    // Reload configuration from file
    private void reloadConfiguration() {
        config.reload();
    }

    private void updateWindowProperties() {
        setTitle(config.getWindowTitle());
        setSize(config.getWindowWidth(), config.getWindowHeight());
    }

    // Update menu bar and menus
    private void updateMenus(Font uiFont) {
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
    }

    private void updateButtons(Font uiFont) {
        selectAllButton.setFont(uiFont);
        deselectAllButton.setFont(uiFont);
        processFilesButton.setFont(uiFont);
    }

    private void updateLabels(Font uiFont) {
        for (JLabel label : labels) {
            label.setFont(uiFont);
        }
    }

    private void updateComboBox(Font uiFont) {
        actionComboBox.setFont(uiFont);
    }

    private void updateTable() {
        fileTable.refreshConfiguration();
    }

    private void updateConsoleArea() {
        consoleArea.setFont(config.getConsoleFont());
        consoleArea.setBackground(config.getConsoleBgColor());
        consoleArea.setForeground(config.getConsoleTextColor());
        consoleArea.setBorder(BorderFactory.createLineBorder(config.getConsoleBorderColor()));
        consoleArea.invalidate();
        consoleArea.getParent().revalidate();
    }

    private void updateSplitPaneRatio() {
        splitPane.setResizeWeight(config.getSplitPaneRatio());
        splitPane.setDividerLocation(config.getSplitPaneRatio());
    }

    // Force repaint of the entire frame
    private void repaintTheFrame() {
        repaint();
        revalidate();
    }

    private void createConsoleArea() {
        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setFont(config.getConsoleFont());
        consoleArea.setBackground(config.getConsoleBgColor());
        consoleArea.setForeground(config.getConsoleTextColor());
        consoleArea.setBorder(BorderFactory.createLineBorder(config.getConsoleBorderColor()));
    }

    private void selectAll() {
        tableModel.setAllSelected(true);
    }

    private void deselectAll() {
        tableModel.setAllSelected(false);
    }

    private void selectFiles() {
        tableModel.setFilesSelected(true);
    }

    private void selectDirs() {
        tableModel.setDirectoriesSelected(true);
    }

    private void processFiles() {
        // Get selected files in the current view order (respecting table sorting)
        List<File> selectedFiles = fileTable.getSelectedFilesInViewOrder();
        if (validateSelectedFiles(selectedFiles)) return;

        FileAction selectedAction = (FileAction) actionComboBox.getSelectedItem();
        if (validateSelectedAction(selectedAction)) return;

        clearConsole();
        ActionLogger logger = loggerForPlugins();
        disableProcessButton();
        processFilesAsync(selectedFiles, selectedAction, logger);
    }

    private boolean validateSelectedFiles(List<File> selectedFiles) {
        if (selectedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No files selected!",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return true;
        }
        return false;
    }

    private boolean validateSelectedAction(FileAction selectedAction) {
        if (selectedAction == null) {
            JOptionPane.showMessageDialog(this,
                    "No action selected!",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return true;
        }
        return false;
    }

    // Clear console before processing
    private void clearConsole() {
        consoleArea.setText("");
    }

    // Create logger for plugins
    private ActionLogger loggerForPlugins() {
        return new ConsoleLogger(consoleArea);
    }

    // Disable the process button during execution
    private void disableProcessButton() {
        processFilesButton.setEnabled(false);
    }

    private void processFilesAsync(List<File> selectedFiles, FileAction selectedAction, ActionLogger logger) {
        fileProcessor.processFilesAsync(selectedFiles, selectedAction, logger, result -> {
            // Re-enable the process button on the UI thread
            SwingUtilities.invokeLater(() -> {
                processFilesButton.setEnabled(true);

                if (result.hasErrors()) {
                    JOptionPane.showMessageDialog(Main.this,
                            "Processing completed with errors.\nSuccess: " + result.getSuccessCount() +
                                    "\nErrors: " + result.getErrorCount(),
                            "Processing Result",
                            JOptionPane.WARNING_MESSAGE);
                }
            });
        });
    }

}