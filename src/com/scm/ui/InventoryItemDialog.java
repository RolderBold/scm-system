package com.scm.ui;

import com.scm.dao.SupplierDAO;
import com.scm.models.Inventory;
import com.scm.models.Supplier;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class InventoryItemDialog extends JDialog {
    private final JTextField nameField;
    private final JTextArea descriptionArea;
    private final JComboBox<String> supplierComboBox;
    private final JSpinner quantitySpinner;
    private final JSpinner reorderLevelSpinner;
    private final JTextField unitPriceField;
    private final SupplierDAO supplierDAO;
    private Inventory item;
    private boolean approved;
    private final Map<String, Integer> supplierMap = new HashMap<>();

    public InventoryItemDialog(Frame owner, String title, Inventory item) {
        super(owner, title, true);
        this.item = item;
        this.supplierDAO = new SupplierDAO();

        // Create components
        nameField = new JTextField(20);
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        
        supplierComboBox = new JComboBox<>();
        
        var quantityModel = new SpinnerNumberModel(0, 0, 10000, 1);
        quantitySpinner = new JSpinner(quantityModel);
        
        var reorderModel = new SpinnerNumberModel(10, 1, 1000, 1);
        reorderLevelSpinner = new JSpinner(reorderModel);
        
        unitPriceField = new JTextField(20);

        // Setup dialog
        setupDialog();
        
        // If editing existing item, populate fields
        if (item != null) {
            populateFields();
        }
    }

    private void setupDialog() {
        var panel = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Populate supplier combo box
        var suppliers = supplierDAO.getAllSuppliers();
        for (var supplier : suppliers) {
            String displayText = String.format("%s (ID: %d)", supplier.supplierName(), supplier.supplierId());
            supplierComboBox.addItem(displayText);
            supplierMap.put(displayText, supplier.supplierId());
        }

        // Add components to panel
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Product Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(descriptionArea), gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Supplier:"), gbc);
        gbc.gridx = 1;
        panel.add(supplierComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        panel.add(quantitySpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Reorder Level:"), gbc);
        gbc.gridx = 1;
        panel.add(reorderLevelSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Unit Price:"), gbc);
        gbc.gridx = 1;
        panel.add(unitPriceField, gbc);

        // Add buttons
        var buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        var okButton = new JButton("OK");
        var cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
            if (validateInput()) {
                approved = true;
                dispose();
            }
        });

        cancelButton.addActionListener(e -> {
            approved = false;
            dispose();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        // Add panels to dialog
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(getOwner());
    }

    private void populateFields() {
        nameField.setText(item.getProductName());
        descriptionArea.setText(item.getDescription());
        
        // Set supplier
        var supplier = supplierDAO.getSupplierById(item.getSupplierId());
        if (supplier != null) {
            var supplierItem = String.format("%s (ID: %d)", supplier.supplierName(), supplier.supplierId());
            supplierComboBox.setSelectedItem(supplierItem);
        }
        
        quantitySpinner.setValue(item.getQuantity());
        reorderLevelSpinner.setValue(item.getReorderLevel());
        unitPriceField.setText(item.getUnitPrice().toString());
    }

    private boolean validateInput() {
        if (nameField.getText().trim().isEmpty()) {
            showError("Please enter a product name.");
            return false;
        }
        
        if (descriptionArea.getText().trim().isEmpty()) {
            showError("Please enter a description.");
            return false;
        }
        
        try {
            var price = Double.parseDouble(unitPriceField.getText());
            if (price <= 0) {
                showError("Unit price must be greater than zero.");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid unit price.");
            return false;
        }
        
        return true;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "Input Error",
            JOptionPane.ERROR_MESSAGE);
    }

    public Inventory getInventoryItem() {
        if (!approved) return null;
        
        try {
            if (item == null) {
                item = new Inventory();
            }
            
            item.setProductName(nameField.getText().trim());
            item.setDescription(descriptionArea.getText().trim());
            
            var supplierSelection = (String) supplierComboBox.getSelectedItem();
            if (supplierSelection != null) {
                Integer supplierId = supplierMap.get(supplierSelection);
                if (supplierId != null) {
                    item.setSupplierId(supplierId);
                }
            }
            
            item.setQuantity((Integer) quantitySpinner.getValue());
            item.setReorderLevel((Integer) reorderLevelSpinner.getValue());
            item.setUnitPrice(new BigDecimal(unitPriceField.getText()));
            
            return item;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                String.format("Error creating inventory item: %s", e.getMessage()),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public boolean isApproved() {
        return approved;
    }
}