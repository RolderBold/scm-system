package com.scm.ui;

import com.scm.dao.SupplierDAO;
import com.scm.models.Supplier;
import javax.swing.*;
import java.awt.*;

public class SuppliersFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTable suppliersTable;
    private SupplierDAO supplierDAO;
    private SupplierTableModel tableModel;

    public SuppliersFrame() {
        setTitle("Suppliers Management");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        supplierDAO = new SupplierDAO();

        // Initialize table
        tableModel = new SupplierTableModel(supplierDAO.getAllSuppliers());
        suppliersTable = new JTable(tableModel);
        suppliersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suppliersTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        suppliersTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        suppliersTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        suppliersTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        suppliersTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        suppliersTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        suppliersTable.getColumnModel().getColumn(5).setPreferredWidth(250);

        JScrollPane scrollPane = new JScrollPane(suppliersTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create button panel with modern styling
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton addButton = createStyledButton("Add Supplier", new Color(46, 204, 113));
        JButton editButton = createStyledButton("Edit Supplier", new Color(52, 152, 219));
        JButton deleteButton = createStyledButton("Delete Supplier", new Color(231, 76, 60));
        JButton refreshButton = createStyledButton("Refresh", new Color(155, 89, 182));

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action listeners
        addButton.addActionListener(e -> addSupplier());
        editButton.addActionListener(e -> editSupplier());
        deleteButton.addActionListener(e -> deleteSupplier());
        refreshButton.addActionListener(e -> refreshTableData());

        refreshTableData();
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

    private void refreshTableData() {
        tableModel.setSuppliers(supplierDAO.getAllSuppliers());
    }

    private void addSupplier() {
        SupplierDialog dialog = new SupplierDialog(this, "Add Supplier", null);
        dialog.setVisible(true);
        
        if (dialog.isApproved()) {
            Supplier supplier = dialog.getSupplier();
            if (supplier != null && supplierDAO.addSupplier(supplier)) {
                refreshTableData();
                JOptionPane.showMessageDialog(this, "Supplier added successfully!");
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error adding supplier.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editSupplier() {
        int selectedRow = suppliersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a supplier to edit.");
            return;
        }

        Supplier supplier = tableModel.getSupplierAt(selectedRow);
        SupplierDialog dialog = new SupplierDialog(this, "Edit Supplier", supplier);
        dialog.setVisible(true);
        
        if (dialog.isApproved()) {
            supplier = dialog.getSupplier();
            if (supplier != null && supplierDAO.updateSupplier(supplier)) {
                refreshTableData();
                JOptionPane.showMessageDialog(this, "Supplier updated successfully!");
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error updating supplier.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSupplier() {
        int selectedRow = suppliersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a supplier to delete.");
            return;
        }

        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this supplier?",
            "Delete Supplier",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (result == JOptionPane.YES_OPTION) {
            Supplier supplier = tableModel.getSupplierAt(selectedRow);
            if (supplierDAO.deleteSupplier(supplier.supplierId())) {
                refreshTableData();
                JOptionPane.showMessageDialog(this, "Supplier deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error deleting supplier. The supplier may have associated orders or inventory items.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}