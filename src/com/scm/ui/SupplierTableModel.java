package com.scm.ui;

import com.scm.models.Supplier;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class SupplierTableModel extends AbstractTableModel {
    private final String[] columnNames = {"ID", "Name", "Contact Person", "Email", "Phone", "Address"};
    private List<Supplier> suppliers;

    public SupplierTableModel(List<Supplier> suppliers) {
        this.suppliers = suppliers;
    }

    @Override
    public int getRowCount() {
        return suppliers.size();
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
        Supplier supplier = suppliers.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> supplier.supplierId();
            case 1 -> supplier.supplierName();
            case 2 -> supplier.contactPerson();
            case 3 -> supplier.email();
            case 4 -> supplier.phone();
            case 5 -> supplier.address();
            default -> null;
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 0 ? Integer.class : String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void setSuppliers(List<Supplier> suppliers) {
        this.suppliers = suppliers;
        fireTableDataChanged();
    }

    public Supplier getSupplierAt(int rowIndex) {
        return suppliers.get(rowIndex);
    }
} 