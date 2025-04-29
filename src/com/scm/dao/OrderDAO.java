package com.scm.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.sql.Date;

import com.scm.config.DatabaseConfig;
import com.scm.models.Order;
import com.scm.models.OrderDetail;

public class OrderDAO {
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setOrderDate(rs.getString("order_date"));
                order.setSupplierId(rs.getInt("supplier_id"));
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setStatus(rs.getString("status"));
                order.setExpectedDeliveryDate(rs.getString("expected_delivery_date"));
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all orders: " + e.getMessage());
        }
        
        return orders;
    }

    public Order getOrderById(int id) {
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setOrderDate(rs.getString("order_date"));
                    order.setSupplierId(rs.getInt("supplier_id"));
                    order.setTotalAmount(rs.getDouble("total_amount"));
                    order.setStatus(rs.getString("status"));
                    order.setExpectedDeliveryDate(rs.getString("expected_delivery_date"));
                    return order;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting order by ID: " + e.getMessage());
        }
        
        return null;
    }

    public boolean addOrder(Order order) {
        String sql = "INSERT INTO orders (order_date, supplier_id, total_amount, status, expected_delivery_date) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, order.getOrderDate());
            stmt.setInt(2, order.getSupplierId());
            stmt.setDouble(3, order.getTotalAmount());
            stmt.setString(4, order.getStatus());
            stmt.setString(5, order.getExpectedDeliveryDate());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 1) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        order.setOrderId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding order: " + e.getMessage());
        }
        
        return false;
    }

    public List<Order> getOrdersBySupplier(int supplierId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE supplier_id = ? ORDER BY order_date DESC";
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, supplierId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setOrderDate(rs.getString("order_date"));
                    order.setSupplierId(rs.getInt("supplier_id"));
                    order.setTotalAmount(rs.getDouble("total_amount"));
                    order.setStatus(rs.getString("status"));
                    order.setExpectedDeliveryDate(rs.getString("expected_delivery_date"));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting orders by supplier: " + e.getMessage());
        }
        
        return orders;
    }

    public boolean createOrder(Order order, List<OrderDetail> orderDetails) {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            conn.setAutoCommit(false);

            String orderSql = "INSERT INTO orders (supplier_id, total_amount, status, expected_delivery_date) VALUES (?, ?, ?, ?)";
            
            try (PreparedStatement orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                orderStmt.setInt(1, order.getSupplierId());
                orderStmt.setDouble(2, order.getTotalAmount());
                orderStmt.setString(3, order.getStatus());
                orderStmt.setString(4, order.getExpectedDeliveryDate());
                
                int affectedRows = orderStmt.executeUpdate();
                
                if (affectedRows == 1) {
                    try (ResultSet generatedKeys = orderStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int orderId = generatedKeys.getInt(1);
                            order.setOrderId(orderId);

                            String detailSql = "INSERT INTO order_details (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
                            
                            try (PreparedStatement detailStmt = conn.prepareStatement(detailSql)) {
                                for (OrderDetail detail : orderDetails) {
                                    detailStmt.setInt(1, orderId);
                                    detailStmt.setInt(2, detail.getProductId());
                                    detailStmt.setInt(3, detail.getQuantity());
                                    detailStmt.setBigDecimal(4, detail.getUnitPrice());
                                    detailStmt.addBatch();
                                }                           
                                
                                detailStmt.executeBatch();
                                conn.commit();
                                return true;
                            }
                        }
                    }
                }
            }
            
            conn.rollback();
            return false;
        } catch (SQLException e) {
            System.err.println("Error creating order: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("Error rolling back transaction: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, orderId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows == 1;
        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
        }
        
        return false;
    }

    public List<OrderDetail> getOrderDetails(int orderId) {
        List<OrderDetail> orderDetails = new ArrayList<>();
        String sql = "SELECT * FROM order_details WHERE order_id = ?";
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, orderId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderDetail detail = new OrderDetail();
                    detail.setOrderDetailId(rs.getInt("order_detail_id"));
                    detail.setOrderId(rs.getInt("order_id"));
                    detail.setProductId(rs.getInt("product_id"));
                    detail.setQuantity(rs.getInt("quantity"));
                    detail.setUnitPrice(rs.getBigDecimal("unit_price"));
                    orderDetails.add(detail);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting order details: " + e.getMessage());
        }
        
        return orderDetails;
    }

    public boolean deleteOrder(int orderId) {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            conn.setAutoCommit(false);

            String detailsSql = "DELETE FROM order_details WHERE order_id = ?";
            
            try (PreparedStatement detailsStmt = conn.prepareStatement(detailsSql)) {
                detailsStmt.setInt(1, orderId);
                detailsStmt.executeUpdate();

                String orderSql = "DELETE FROM orders WHERE order_id = ?";
                
                try (PreparedStatement orderStmt = conn.prepareStatement(orderSql)) {
                    orderStmt.setInt(1, orderId);
                    int affectedRows = orderStmt.executeUpdate();
                    
                    if (affectedRows == 1) {
                        conn.commit();
                        return true;
                    }
                }
            }
            
            conn.rollback();
            return false;
        } catch (SQLException e) {
            System.err.println("Error deleting order: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("Error rolling back transaction: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    public int getPendingOrdersCount() {
        String sql = "SELECT COUNT(*) AS total FROM orders WHERE status = 'Pending'";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error getting pending orders count: " + e.getMessage());
        }
        return 0;
    }
}