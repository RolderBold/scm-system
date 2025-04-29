package com.scm.ui;

import com.scm.models.Inventory;
import com.scm.models.Supplier;
import com.scm.dao.SupplierDAO;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class InventoryTableModel extends AbstractTableModel {
    private final String[] columnNames = {"ID", "Product Name", "Description", "Supplier", "Quantity", "Reorder Level", "Unit Price"};
    private List<Inventory> inventoryItems;
    private final SupplierDAO supplierDAO;

    public InventoryTableModel(List<Inventory> inventoryItems) {
        this.inventoryItems = inventoryItems;
        this.supplierDAO = new SupplierDAO();
    }

    @Override
    public int getRowCount() {
        return inventoryItems.size();
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
        Inventory item = inventoryItems.get(rowIndex);
        Supplier supplier = supplierDAO.getSupplierById(item.getSupplierId());
        
        return switch (columnIndex) {
            case 0 -> item.getProductId();
            case 1 -> item.getProductName();
            case 2 -> item.getDescription();
            case 3 -> supplier != null ? supplier.supplierName() : "Unknown";
            case 4 -> item.getQuantity();
            case 5 -> item.getReorderLevel();
            case 6 -> String.format("₹%.2f", item.getUnitPrice());
            default -> null;
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0, 4, 5 -> Integer.class;
            default -> String.class;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void setInventory(List<Inventory> items) {
        this.inventoryItems = items;
        fireTableDataChanged();
    }

    public Inventory getInventoryAt(int rowIndex) {
        return inventoryItems.get(rowIndex);
    }

    public void addInventoryItem(Inventory item) {
        inventoryItems.add(item);
        fireTableRowsInserted(inventoryItems.size() - 1, inventoryItems.size() - 1);
    }

    public void updateInventoryItem(int rowIndex, Inventory item) {
        inventoryItems.set(rowIndex, item);
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    public void removeInventoryItem(int rowIndex) {
        inventoryItems.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }
}