package com.scm.ui;

import com.scm.dao.InventoryDAO;
import com.scm.models.Inventory;
import com.scm.ui.InventoryTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class InventoryFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final InventoryTableModel tableModel;
    private final JTable table;
    private final InventoryDAO inventoryDAO;

    public InventoryFrame() {
        super("Inventory Management System");
        this.inventoryDAO = new InventoryDAO();
        this.tableModel = new InventoryTableModel(inventoryDAO.getAllInventory());
        this.table = new JTable(tableModel);

        setupUI();
        setupWindowListener();
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        // Setup table
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // Product Name
        table.getColumnModel().getColumn(2).setPreferredWidth(200); // Description
        table.getColumnModel().getColumn(3).setPreferredWidth(150); // Supplier
        table.getColumnModel().getColumn(4).setPreferredWidth(80);  // Quantity
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // Reorder Level
        table.getColumnModel().getColumn(6).setPreferredWidth(100); // Unit Price
        
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Create button panel with modern styling
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton addButton = createStyledButton("Add Item", new Color(46, 204, 113));
        JButton editButton = createStyledButton("Edit Item", new Color(52, 152, 219));
        JButton deleteButton = createStyledButton("Delete Item", new Color(231, 76, 60));
        JButton refreshButton = createStyledButton("Refresh", new Color(155, 89, 182));

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action listeners
        addButton.addActionListener(this::addItem);
        editButton.addActionListener(this::editItem);
        deleteButton.addActionListener(this::deleteItem);
        refreshButton.addActionListener(e -> refreshTable());

        // Set frame properties
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        refreshTable();
    }
    
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return button;
    }

    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Cleanup resources
                dispose();
            }
        });
    }

    private void addItem(ActionEvent e) {
        var dialog = new InventoryItemDialog(this, "Add New Item", null);
        dialog.setVisible(true);

        if (dialog.isApproved()) {
            var newItem = dialog.getInventoryItem();
            if (newItem != null) {
                try {
                    if (inventoryDAO.addInventoryItem(newItem)) {
                        refreshTable();
                        JOptionPane.showMessageDialog(this,
                            "Item added successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        throw new Exception("Failed to add inventory item");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                        String.format("Error adding item: %s", ex.getMessage()),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void editItem(ActionEvent e) {
        var selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an item to edit.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        var item = tableModel.getInventoryAt(selectedRow);
        var dialog = new InventoryItemDialog(this, "Edit Item", item);
        dialog.setVisible(true);

        if (dialog.isApproved()) {
            var updatedItem = dialog.getInventoryItem();
            if (updatedItem != null) {
                try {
                    if (inventoryDAO.updateInventoryItem(updatedItem)) {
                        refreshTable();
                        JOptionPane.showMessageDialog(this,
                            "Item updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        throw new Exception("Failed to update inventory item");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                        String.format("Error updating item: %s", ex.getMessage()),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void deleteItem(ActionEvent e) {
        var selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an item to delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        var item = tableModel.getInventoryAt(selectedRow);
        var response = JOptionPane.showConfirmDialog(this,
            String.format("Are you sure you want to delete '%s'?", item.getProductName()),
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            try {
                if (inventoryDAO.deleteInventoryItem(item.getProductId())) {
                    refreshTable();
                    JOptionPane.showMessageDialog(this,
                        "Item deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    throw new Exception("Failed to delete inventory item");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    String.format("Error deleting item: %s", ex.getMessage()),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshTable() {
        tableModel.setInventory(inventoryDAO.getAllInventory());
        tableModel.fireTableDataChanged();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new InventoryFrame().setVisible(true);
        });
    }
}