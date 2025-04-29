package com.scm.models;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Inventory {
    private int productId;
    private String productName;
    private String description;
    private int supplierId;
    private int quantity;
    private int reorderLevel;
    private BigDecimal unitPrice;
    private Timestamp createdAt;

    public Inventory() {}
    
    public Inventory(int productId, String productName, String description, int supplierId, int quantity, int reorderLevel, BigDecimal unitPrice, Timestamp createdAt) {
        this.productId = productId;
        this.productName = productName;
        this.description = description;
        this.supplierId = supplierId;
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
        this.unitPrice = unitPrice;
        this.createdAt = createdAt;
    }

    public int getProductId() {
        return productId;
    }
    
    public void setProductId(int productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getSupplierId() {
        return supplierId;
    }
    
    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public int getReorderLevel() {
        return reorderLevel;
    }
    
    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "Inventory [productId=" + productId + ", productName=" + productName + ", quantity=" + quantity
                + ", unitPrice=" + unitPrice + "]";
    }
}