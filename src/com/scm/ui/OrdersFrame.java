package com.scm.ui;

import com.scm.dao.OrderDAO;
import com.scm.dao.SupplierDAO;
import com.scm.models.Order;

import javax.swing.*;
import java.awt.*;

public class OrdersFrame extends JFrame {
    private final JTable ordersTable;
    private final OrderDAO orderDAO;
    private final OrderTableModel tableModel;
    private final SupplierDAO supplierDAO;
    private static final String[] ORDER_STATUSES = {"Pending", "Processing", "Completed", "Cancelled"};

    public OrdersFrame() {
        super("Orders Management");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        this.orderDAO = new OrderDAO();
        this.supplierDAO = new SupplierDAO();
        this.tableModel = new OrderTableModel(orderDAO.getAllOrders());
        this.ordersTable = new JTable(tableModel);

        setupUI();
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        // Initialize table
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ordersTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        ordersTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        ordersTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Date
        ordersTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Supplier
        ordersTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Total Amount
        ordersTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Status
        ordersTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Expected Delivery

        var scrollPane = new JScrollPane(ordersTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create button panel with modern styling
        var buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        var createButton = createStyledButton("Create Order", new Color(46, 204, 113));
        var viewDetailsButton = createStyledButton("View Details", new Color(52, 152, 219));
        var deleteButton = createStyledButton("Delete Order", new Color(231, 76, 60));
        var refreshButton = createStyledButton("Refresh", new Color(155, 89, 182));

        buttonPanel.add(createButton);
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action listeners
        createButton.addActionListener(e -> createOrder());
        viewDetailsButton.addActionListener(e -> viewOrderDetails());
        deleteButton.addActionListener(e -> deleteOrder());
        refreshButton.addActionListener(e -> refreshTable());

        refreshTable();
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        var button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return button;
    }

    private void refreshTable() {
        try {
            var orders = orderDAO.getAllOrders();
            if (orders != null) {
                tableModel.setOrders(orders);
            } else {
                throw new Exception("Failed to fetch orders");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                String.format("Error refreshing orders: %s", ex.getMessage()),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createOrder() {
        var dialog = new OrderDialog(this, "Create Order", null);
        dialog.setVisible(true);
        
        if (dialog.isApproved()) {
            var order = dialog.getOrder();
            var orderDetails = dialog.getOrderDetails();
            
            if (order != null && orderDetails != null && !orderDetails.isEmpty()) {
                try {
                    // Try to create order with details
                    if (orderDAO.createOrder(order, orderDetails)) {
                        refreshTable();
                        JOptionPane.showMessageDialog(this,
                            "Order created successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        // Fallback to simple order creation if createOrder with details fails
                        if (orderDAO.addOrder(order)) {
                            refreshTable();
                            JOptionPane.showMessageDialog(this,
                                "Order created successfully (without details)!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            throw new Exception("Failed to create order");
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                        String.format("Error creating order: %s", ex.getMessage()),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void viewOrderDetails() {
        var selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an order to view details.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        var order = tableModel.getOrderAt(selectedRow);
        var dialog = new OrderDialog(this, "Order Details", order);
        dialog.setVisible(true);
        
        if (dialog.isApproved()) {
            var updatedOrder = dialog.getOrder();
            if (updatedOrder != null) {
                try {
                    if (orderDAO.updateOrderStatus(updatedOrder.getOrderId(), updatedOrder.getStatus())) {
                        refreshTable();
                        JOptionPane.showMessageDialog(this,
                            "Order updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        throw new Exception("Failed to update order");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                        String.format("Error updating order: %s", ex.getMessage()),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void deleteOrder() {
        var selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an order to delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        var order = tableModel.getOrderAt(selectedRow);
        
        // Confirm deletion
        int response = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this order? This action cannot be undone.",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (response == JOptionPane.YES_OPTION) {
            try {
                if (orderDAO.deleteOrder(order.getOrderId())) {
                    refreshTable();
                    JOptionPane.showMessageDialog(this,
                        "Order deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    throw new Exception("Failed to delete order");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    String.format("Error deleting order: %s", ex.getMessage()),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}