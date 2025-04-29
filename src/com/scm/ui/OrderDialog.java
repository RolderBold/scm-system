package com.scm.ui;

import com.scm.dao.InventoryDAO;
import com.scm.dao.OrderDAO;
import com.scm.dao.SupplierDAO;
import com.scm.models.Inventory;
import com.scm.models.Order;
import com.scm.models.OrderDetail;
import com.scm.models.Supplier;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;
import java.util.Vector;

public class OrderDialog extends JDialog {
    private static final String[] ORDER_STATUSES = {"Pending", "Processing", "Completed", "Cancelled"};
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    private final JDateChooser orderDateChooser;
    private final JComboBox<String> supplierComboBox;
    private final JLabel totalAmountLabel;
    private final JComboBox<String> statusComboBox;
    private final JDateChooser expectedDeliveryChooser;
    private final SupplierDAO supplierDAO;
    private final InventoryDAO inventoryDAO;
    private final OrderDAO orderDAO;
    private Order order;
    private final List<OrderDetail> orderDetails = new ArrayList<>();
    private boolean approved;
    private final Map<String, Integer> supplierMap = new HashMap<>();
    
    private JTable inventoryTable;
    private JTable selectedItemsTable;
    private DefaultTableModel selectedItemsModel;
    private List<Inventory> availableItems = new ArrayList<>();
    private BigDecimal orderTotal = BigDecimal.ZERO;
    
    private final String[] INVENTORY_COLUMNS = {"ID", "Product Name", "Description", "Available", "Unit Price"};
    private final String[] ORDER_ITEM_COLUMNS = {"ID", "Product Name", "Quantity", "Unit Price", "Subtotal"};

    public OrderDialog(Frame owner, String title, Order order) {
        super(owner, title, true);
        this.order = order;
        this.supplierDAO = new SupplierDAO();
        this.inventoryDAO = new InventoryDAO();
        this.orderDAO = new OrderDAO();
        
        // Create components
        orderDateChooser = new JDateChooser();
        orderDateChooser.setDateFormatString("yyyy-MM-dd");
        
        supplierComboBox = new JComboBox<>();
        totalAmountLabel = new JLabel("₹0.00");
        totalAmountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        statusComboBox = new JComboBox<>(ORDER_STATUSES);
        
        expectedDeliveryChooser = new JDateChooser();
        expectedDeliveryChooser.setDateFormatString("yyyy-MM-dd");

        // Set default dates
        if (order == null) {
            orderDateChooser.setDate(new Date());
            expectedDeliveryChooser.setDate(new Date());
            calculateExpectedDeliveryDate();
        }

        // Load all inventory items
        availableItems = inventoryDAO.getAllInventory();

        // Setup dialog
        setupDialog();
        
        // If editing existing order, populate fields
        if (order != null) {
            populateFields();
        } else {
            // For new orders, load all inventory initially
            loadAllInventory();
        }
    }
    
    private void loadAllInventory() {
        // Clear the inventory table
        DefaultTableModel model = (DefaultTableModel) inventoryTable.getModel();
        model.setRowCount(0);
        
        // Load all inventory items
        for (Inventory item : availableItems) {
            model.addRow(new Object[]{
                item.getProductId(),
                item.getProductName(),
                item.getDescription(),
                item.getQuantity(),
                String.format("₹%.2f", item.getUnitPrice())
            });
        }
    }

    private void loadInventoryForSupplier() {
        String selectedSupplier = (String) supplierComboBox.getSelectedItem();
        if (selectedSupplier == null) {
            // If no supplier selected, load all inventory
            loadAllInventory();
            return;
        }
        
        Integer supplierId = supplierMap.get(selectedSupplier);
        if (supplierId == null) return;
        
        // Clear the inventory table
        DefaultTableModel model = (DefaultTableModel) inventoryTable.getModel();
        model.setRowCount(0);
        
        boolean hasItems = false;
        
        // Load inventory items for this supplier
        for (Inventory item : availableItems) {
            if (item.getSupplierId() == supplierId) {
                model.addRow(new Object[]{
                    item.getProductId(),
                    item.getProductName(),
                    item.getDescription(),
                    item.getQuantity(),
                    String.format("₹%.2f", item.getUnitPrice())
                });
                hasItems = true;
            }
        }
        
        if (!hasItems) {
            JOptionPane.showMessageDialog(this,
                "No inventory items found for the selected supplier.",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void setupDialog() {
        setSize(900, 700);
        setLayout(new BorderLayout());
        
        // Set date change listener for order date
        orderDateChooser.addPropertyChangeListener("date", e -> {
            if (orderDateChooser.getDate() != null && expectedDeliveryChooser.getDate() == null) {
                calculateExpectedDeliveryDate();
            }
        });
        
        // Create top panel with order details
        JPanel orderDetailsPanel = createOrderDetailsPanel();
        add(orderDetailsPanel, BorderLayout.NORTH);
        
        // Create center panel with inventory selection
        JPanel inventorySelectionPanel = createInventorySelectionPanel();
        add(inventorySelectionPanel, BorderLayout.CENTER);
        
        // Create button panel
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
        add(buttonPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(getOwner());
    }
    
    private JPanel createOrderDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Order Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Populate supplier combo box
        var suppliers = supplierDAO.getAllSuppliers();
        for (var supplier : suppliers) {
            String displayText = String.format("%s (ID: %d)", supplier.supplierName(), supplier.supplierId());
            supplierComboBox.addItem(displayText);
            supplierMap.put(displayText, supplier.supplierId());
        }
        
        supplierComboBox.addActionListener(e -> loadInventoryForSupplier());

        // Add components to panel
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Order Date:"), gbc);
        gbc.gridx = 1;
        panel.add(orderDateChooser, gbc);
        
        gbc.gridx = 2;
        panel.add(new JLabel("Expected Delivery:"), gbc);
        gbc.gridx = 3;
        panel.add(expectedDeliveryChooser, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Supplier:"), gbc);
        gbc.gridx = 1;
        panel.add(supplierComboBox, gbc);
        
        gbc.gridx = 2;
        panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 3;
        panel.add(statusComboBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Total Amount:"), gbc);
        gbc.gridx = 1;
        panel.add(totalAmountLabel, gbc);
        
        return panel;
    }
    
    private JPanel createInventorySelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Split pane for inventory and selected items
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(300);
        
        // Available inventory panel
        JPanel availablePanel = new JPanel(new BorderLayout());
        availablePanel.setBorder(BorderFactory.createTitledBorder("Available Inventory"));
        
        inventoryTable = new JTable(new DefaultTableModel(INVENTORY_COLUMNS, 0));
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane inventoryScroll = new JScrollPane(inventoryTable);
        availablePanel.add(inventoryScroll, BorderLayout.CENTER);
        
        JPanel inventoryButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton lowStockButton = new JButton("Add Low Stock Items");
        lowStockButton.setBackground(new Color(155, 89, 182));
        lowStockButton.setForeground(Color.WHITE);
        lowStockButton.addActionListener(e -> loadItemsForReorder());
        
        JButton addButton = new JButton("Add to Order");
        addButton.setBackground(new Color(46, 204, 113));
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(e -> addItemToOrder());
        
        inventoryButtonPanel.add(lowStockButton);
        inventoryButtonPanel.add(addButton);
        availablePanel.add(inventoryButtonPanel, BorderLayout.SOUTH);
        
        // Selected items panel
        JPanel selectedPanel = new JPanel(new BorderLayout());
        selectedPanel.setBorder(BorderFactory.createTitledBorder("Order Items"));
        
        selectedItemsModel = new DefaultTableModel(ORDER_ITEM_COLUMNS, 0);
        selectedItemsTable = new JTable(selectedItemsModel);
        JScrollPane selectedScroll = new JScrollPane(selectedItemsTable);
        selectedPanel.add(selectedScroll, BorderLayout.CENTER);
        
        JPanel selectedButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton removeButton = new JButton("Remove Item");
        removeButton.setBackground(new Color(231, 76, 60));
        removeButton.setForeground(Color.WHITE);
        removeButton.addActionListener(e -> removeItemFromOrder());
        selectedButtonPanel.add(removeButton);
        selectedPanel.add(selectedButtonPanel, BorderLayout.SOUTH);
        
        splitPane.setTopComponent(availablePanel);
        splitPane.setBottomComponent(selectedPanel);
        
        panel.add(splitPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadItemsForReorder() {
        // Get items below reorder level
        List<Inventory> lowStockItems = inventoryDAO.getItemsForReorder();
        
        if (lowStockItems.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "There are no items below reorder level currently.",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Clear the current selection
        selectedItemsModel.setRowCount(0);
        orderDetails.clear();
        
        // Group items by supplier
        Map<Integer, List<Inventory>> supplierItems = new HashMap<>();
        for (Inventory item : lowStockItems) {
            if (!supplierItems.containsKey(item.getSupplierId())) {
                supplierItems.put(item.getSupplierId(), new ArrayList<>());
            }
            supplierItems.get(item.getSupplierId()).add(item);
        }
        
        // Get current supplier ID
        Integer currentSupplierId = null;
        String selectedSupplier = (String) supplierComboBox.getSelectedItem();
        if (selectedSupplier != null) {
            currentSupplierId = supplierMap.get(selectedSupplier);
        }
        
        // Only show items for the current supplier
        if (currentSupplierId != null && supplierItems.containsKey(currentSupplierId)) {
            for (Inventory item : supplierItems.get(currentSupplierId)) {
                // Calculate suggested order quantity (reorder level - current quantity + buffer)
                int suggestedQuantity = item.getReorderLevel() - item.getQuantity() + 5;
                
                // Add to order
                BigDecimal unitPrice = item.getUnitPrice();
                BigDecimal subtotal = unitPrice.multiply(new BigDecimal(suggestedQuantity));
                
                selectedItemsModel.addRow(new Object[]{
                    item.getProductId(),
                    item.getProductName(),
                    suggestedQuantity,
                    String.format("₹%.2f", unitPrice),
                    String.format("₹%.2f", subtotal)
                });
                
                // Create OrderDetail
                OrderDetail detail = new OrderDetail();
                detail.setProductId(item.getProductId());
                detail.setQuantity(suggestedQuantity);
                detail.setUnitPrice(unitPrice);
                orderDetails.add(detail);
            }
            
            // Update total
            updateOrderTotal();
            
            JOptionPane.showMessageDialog(this,
                "Added " + orderDetails.size() + " items that need reordering.",
                "Reorder Items Added",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void calculateExpectedDeliveryDate() {
        if (orderDateChooser.getDate() == null) return;
        
        // Add 7 days to the order date for expected delivery
        Calendar cal = Calendar.getInstance();
        cal.setTime(orderDateChooser.getDate());
        cal.add(Calendar.DAY_OF_MONTH, 7);
        
        expectedDeliveryChooser.setDate(cal.getTime());
    }
    
    private void addItemToOrder() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an item to add to the order.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int productId = (int) inventoryTable.getValueAt(selectedRow, 0);
        String productName = (String) inventoryTable.getValueAt(selectedRow, 1);
        
        // Find the inventory item
        Inventory selectedItem = null;
        for (Inventory item : availableItems) {
            if (item.getProductId() == productId) {
                selectedItem = item;
                break;
            }
        }
        
        if (selectedItem == null) return;
        
        // Check if this product is already in the order
        boolean itemExists = false;
        int existingRow = -1;
        
        for (int i = 0; i < selectedItemsModel.getRowCount(); i++) {
            if ((int)selectedItemsModel.getValueAt(i, 0) == productId) {
                itemExists = true;
                existingRow = i;
                break;
            }
        }
        
        if (itemExists) {
            int response = JOptionPane.showConfirmDialog(this,
                "This item is already in the order. Do you want to update the quantity?",
                "Item Exists",
                JOptionPane.YES_NO_OPTION);
                
            if (response != JOptionPane.YES_OPTION) {
                return;
            }
            
            // Remove existing item first
            for (int i = 0; i < orderDetails.size(); i++) {
                if (orderDetails.get(i).getProductId() == productId) {
                    orderDetails.remove(i);
                    break;
                }
            }
            
            // Remove from table
            selectedItemsModel.removeRow(existingRow);
        }
        
        // Get suggested quantity if inventory is low
        String suggestedQuantity = "";
        if (selectedItem.getQuantity() <= selectedItem.getReorderLevel()) {
            suggestedQuantity = String.valueOf(selectedItem.getReorderLevel() - selectedItem.getQuantity() + 5);
        }
        
        // Ask for quantity
        String quantityStr = (String) JOptionPane.showInputDialog(
            this,
            "Enter quantity for " + productName + ":",
            "Quantity",
            JOptionPane.QUESTION_MESSAGE,
            null,
            null,
            suggestedQuantity
        );
        
        if (quantityStr == null || quantityStr.isEmpty()) return;
        
        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this,
                    "Quantity must be greater than zero.",
                    "Invalid Quantity",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Show warning but allow ordering more than available
            if (quantity > selectedItem.getQuantity()) {
                int response = JOptionPane.showConfirmDialog(this,
                    "Warning: You're ordering more than the available stock (" + selectedItem.getQuantity() + " items).\n" +
                    "Do you want to continue with this quantity?",
                    "Insufficient Stock",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                    
                if (response != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            // Add to order items table
            BigDecimal unitPrice = selectedItem.getUnitPrice();
            BigDecimal subtotal = unitPrice.multiply(new BigDecimal(quantity));
            
            selectedItemsModel.addRow(new Object[]{
                productId,
                productName,
                quantity,
                String.format("₹%.2f", unitPrice),
                String.format("₹%.2f", subtotal)
            });
            
            // Create OrderDetail
            OrderDetail detail = new OrderDetail();
            detail.setProductId(productId);
            detail.setQuantity(quantity);
            detail.setUnitPrice(unitPrice);
            orderDetails.add(detail);
            
            // Update total
            updateOrderTotal();
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid quantity.",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void removeItemFromOrder() {
        int selectedRow = selectedItemsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an item to remove.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int productId = (int) selectedItemsTable.getValueAt(selectedRow, 0);
        
        // Remove from order details
        for (int i = 0; i < orderDetails.size(); i++) {
            if (orderDetails.get(i).getProductId() == productId) {
                orderDetails.remove(i);
                break;
            }
        }
        
        // Remove from table
        selectedItemsModel.removeRow(selectedRow);
        
        // Update total
        updateOrderTotal();
    }
    
    private void updateOrderTotal() {
        orderTotal = BigDecimal.ZERO;
        
        for (OrderDetail detail : orderDetails) {
            BigDecimal itemTotal = detail.getUnitPrice().multiply(new BigDecimal(detail.getQuantity()));
            orderTotal = orderTotal.add(itemTotal);
        }
        
        totalAmountLabel.setText(String.format("₹%.2f", orderTotal));
    }

    private void populateFields() {
        try {
            if (order.getOrderDate() != null) {
                orderDateChooser.setDate(DATE_FORMAT.parse(order.getOrderDate()));
            }
            
            if (order.getExpectedDeliveryDate() != null) {
                expectedDeliveryChooser.setDate(DATE_FORMAT.parse(order.getExpectedDeliveryDate()));
            }
            
            // Set supplier
            var supplier = supplierDAO.getSupplierById(order.getSupplierId());
            if (supplier != null) {
                var supplierItem = String.format("%s (ID: %d)", supplier.supplierName(), supplier.supplierId());
                supplierComboBox.setSelectedItem(supplierItem);
            }
            
            totalAmountLabel.setText(String.format("₹%.2f", order.getTotalAmount()));
            statusComboBox.setSelectedItem(order.getStatus());
            
            // Load order details
            loadOrderDetails();
            
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this,
                String.format("Error parsing date: %s", e.getMessage()),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                String.format("Error populating order data: %s", e.getMessage()),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadOrderDetails() {
        if (order == null || order.getOrderId() == 0) return;
        
        // Clear existing items
        orderDetails.clear();
        selectedItemsModel.setRowCount(0);
        
        // Load order details from database
        List<OrderDetail> details = orderDAO.getOrderDetails(order.getOrderId());
        
        for (OrderDetail detail : details) {
            orderDetails.add(detail);
            
            // Find inventory item to get product name
            Inventory item = inventoryDAO.getInventoryById(detail.getProductId());
            if (item != null) {
                BigDecimal subtotal = detail.getUnitPrice().multiply(new BigDecimal(detail.getQuantity()));
                
                selectedItemsModel.addRow(new Object[]{
                    detail.getProductId(),
                    item.getProductName(),
                    detail.getQuantity(),
                    String.format("₹%.2f", detail.getUnitPrice()),
                    String.format("₹%.2f", subtotal)
                });
            }
        }
        
        updateOrderTotal();
    }

    private boolean validateInput() {
        if (orderDateChooser.getDate() == null) {
            showError("Please select an order date.");
            return false;
        }
        
        if (expectedDeliveryChooser.getDate() == null) {
            showError("Please select an expected delivery date.");
            return false;
        }
        
        if (supplierComboBox.getSelectedItem() == null) {
            showError("Please select a supplier.");
            return false;
        }
        
        if (orderDetails.isEmpty()) {
            showError("Please add at least one item to the order.");
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

    public Order getOrder() {
        if (!approved) return null;
        
        try {
            if (order == null) {
                order = new Order();
            }
            
            order.setOrderDate(DATE_FORMAT.format(orderDateChooser.getDate()));
            
            var supplierSelection = (String) supplierComboBox.getSelectedItem();
            if (supplierSelection != null) {
                Integer supplierId = supplierMap.get(supplierSelection);
                if (supplierId != null) {
                    order.setSupplierId(supplierId);
                }
            }
            
            order.setTotalAmount(orderTotal.doubleValue());
            order.setStatus((String) statusComboBox.getSelectedItem());
            order.setExpectedDeliveryDate(DATE_FORMAT.format(expectedDeliveryChooser.getDate()));
            
            return order;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                String.format("Error creating order: %s", e.getMessage()),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    public List<OrderDetail> getOrderDetails() {
        if (!approved) return null;
        return orderDetails;
    }

    public boolean isApproved() {
        return approved;
    }
} 