package com.scm.ui;

import com.scm.models.Order;
import com.scm.models.Supplier;
import com.scm.dao.SupplierDAO;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class OrderTableModel extends AbstractTableModel {
    private final String[] columnNames = {"ID", "Date", "Supplier", "Total Amount", "Status", "Expected Delivery"};
    private List<Order> orders;
    private final SupplierDAO supplierDAO;

    public OrderTableModel(List<Order> orders) {
        this.orders = orders;
        this.supplierDAO = new SupplierDAO();
    }

    @Override
    public int getRowCount() {
        return orders.size();
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
    public Object getValueAt(int rowIndex, int columnIndex) {
        Order order = orders.get(rowIndex);
        Supplier supplier = supplierDAO.getSupplierById(order.getSupplierId());
        
        return switch (columnIndex) {
            case 0 -> order.getOrderId();
            case 1 -> order.getOrderDate();
            case 2 -> supplier != null ? supplier.supplierName() : "Unknown";
            case 3 -> String.format("₹%.2f", order.getTotalAmount());
            case 4 -> order.getStatus();
            case 5 -> order.getExpectedDeliveryDate();
            default -> null;
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> Integer.class;
            default -> String.class;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
        fireTableDataChanged();
    }

    public Order getOrderAt(int rowIndex) {
        return orders.get(rowIndex);
    }
} 