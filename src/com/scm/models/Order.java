package com.scm.models;

public class Order {
    private int orderId;
    private String orderDate;
    private int supplierId;
    private double totalAmount;
    private String status;
    private String expectedDeliveryDate;

    public Order() {}
    
    public Order(int orderId, String orderDate, int supplierId, double totalAmount, String status, String expectedDeliveryDate) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.supplierId = supplierId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public int getOrderId() {
        return orderId;
    }
    
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    
    public String getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }
    
    public int getSupplierId() {
        return supplierId;
    }
    
    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }
    
    public double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }
    
    public void setExpectedDeliveryDate(String expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }
    
    @Override
    public String toString() {
        return "Order [orderId=" + orderId + ", orderDate=" + orderDate + ", supplierId=" + supplierId
                + ", totalAmount=" + totalAmount + ", status=" + status + "]";
    }
}
