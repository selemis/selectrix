package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class Main extends JFrame {
    private DefaultListModel<FileCheckBox> listModel;
    private JList<FileCheckBox> fileList;

    public Main() {
        setTitle("File Selector");
        setSize(800, 600);
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
        openFolderItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFolder();
            }
        });

        fileMenu.add(openFolderItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Create list model and list for displaying files
        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);
        fileList.setCellRenderer(new CheckBoxListRenderer());
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add mouse listener to toggle checkboxes
        fileList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int index = fileList.locationToIndex(evt.getPoint());
                if (index != -1) {
                    FileCheckBox item = listModel.getElementAt(index);
                    item.setSelected(!item.isSelected());
                    fileList.repaint();
                }
            }
        });

        // Add scroll pane
        JScrollPane scrollPane = new JScrollPane(fileList);
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
        listModel.clear();

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                listModel.addElement(new FileCheckBox(file));
            }
        }
    }

    // Inner class to represent a file with checkbox state
    private static class FileCheckBox {
        private final File file;
        private boolean selected;

        public FileCheckBox(File file) {
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

        @Override
        public String toString() {
            return file.getName();
        }
    }

    // Custom cell renderer to display checkboxes
    private static class CheckBoxListRenderer extends JCheckBox implements ListCellRenderer<FileCheckBox> {
        @Override
        public Component getListCellRendererComponent(JList<? extends FileCheckBox> list,
                                                      FileCheckBox value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            setEnabled(list.isEnabled());
            setSelected(value.isSelected());
            setFont(list.getFont());
            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

            String displayText = value.getFile().getName();
            if (value.getFile().isDirectory()) {
                displayText = "[DIR] " + displayText;
            }
            setText(displayText);

            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Main().setVisible(true);
            }
        });
    }
}