package com.scm.ui;

import com.scm.models.Supplier;
import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;

public class SupplierDialog extends JDialog {
    private final JTextField nameField;
    private final JTextField contactPersonField;
    private final JTextField emailField;
    private final JTextField phoneField;
    private final JTextField addressField;
    private Supplier supplier;
    private boolean approved;

    public SupplierDialog(Frame owner, String title, Supplier supplier) {
        super(owner, title, true);
        this.supplier = supplier;

        // Create components
        nameField = new JTextField(20);
        contactPersonField = new JTextField(20);
        emailField = new JTextField(20);
        phoneField = new JTextField(20);
        addressField = new JTextField(20);

        // Setup dialog
        setupDialog();
        
        // If editing existing supplier, populate fields
        if (supplier != null) {
            populateFields();
        }
    }

    private void setupDialog() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Add components to panel
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Contact Person:"), gbc);
        gbc.gridx = 1;
        panel.add(contactPersonField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        panel.add(phoneField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        panel.add(addressField, gbc);

        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

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
        nameField.setText(supplier.supplierName());
        contactPersonField.setText(supplier.contactPerson());
        emailField.setText(supplier.email());
        phoneField.setText(supplier.phone());
        addressField.setText(supplier.address());
    }

    private boolean validateInput() {
        if (nameField.getText().trim().isEmpty()) {
            showError("Please enter a supplier name.");
            return false;
        }
        
        if (contactPersonField.getText().trim().isEmpty()) {
            showError("Please enter a contact person.");
            return false;
        }
        
        if (emailField.getText().trim().isEmpty()) {
            showError("Please enter an email address.");
            return false;
        }
        
        if (phoneField.getText().trim().isEmpty()) {
            showError("Please enter a phone number.");
            return false;
        }
        
        if (addressField.getText().trim().isEmpty()) {
            showError("Please enter an address.");
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

    public Supplier getSupplier() {
        if (!approved) return null;
        
        int id = (supplier != null) ? supplier.supplierId() : 0;
        Timestamp createdAt = (supplier != null) ? supplier.createdAt() : new Timestamp(System.currentTimeMillis());
        
        return new Supplier(
            id,
            nameField.getText().trim(),
            contactPersonField.getText().trim(),
            emailField.getText().trim(),
            phoneField.getText().trim(),
            addressField.getText().trim(),
            createdAt
        );
    }

    public boolean isApproved() {
        return approved;
    }
} 