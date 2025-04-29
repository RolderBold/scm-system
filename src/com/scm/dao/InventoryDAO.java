package com.scm.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

import com.scm.config.DatabaseConfig;
import com.scm.models.Inventory;

public class InventoryDAO {

    public List<Inventory> getAllInventory() {
        List<Inventory> inventoryItems = new ArrayList<>();
        String sql = "SELECT * FROM inventory";
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Inventory item = new Inventory();
                item.setProductId(rs.getInt("product_id"));
                item.setProductName(rs.getString("product_name"));
                item.setDescription(rs.getString("description"));
                item.setSupplierId(rs.getInt("supplier_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setReorderLevel(rs.getInt("reorder_level"));
                item.setUnitPrice(rs.getBigDecimal("unit_price"));
                item.setCreatedAt(rs.getTimestamp("created_at"));
                inventoryItems.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all inventory items: " + e.getMessage());
        }
        
        return inventoryItems;
    }

    public List<Inventory> getItemsForReorder() {
        List<Inventory> inventoryItems = new ArrayList<>();
        String sql = "SELECT * FROM inventory WHERE quantity <= reorder_level";
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Inventory item = new Inventory();
                item.setProductId(rs.getInt("product_id"));
                item.setProductName(rs.getString("product_name"));
                item.setDescription(rs.getString("description"));
                item.setSupplierId(rs.getInt("supplier_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setReorderLevel(rs.getInt("reorder_level"));
                item.setUnitPrice(rs.getBigDecimal("unit_price"));
                item.setCreatedAt(rs.getTimestamp("created_at"));
                inventoryItems.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error getting items for reorder: " + e.getMessage());
        }
        
        return inventoryItems;
    }

    public Inventory getInventoryById(int productId) {
        Inventory item = null;
        String sql = "SELECT * FROM inventory WHERE product_id = ?";
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, productId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    item = new Inventory();
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setDescription(rs.getString("description"));
                    item.setSupplierId(rs.getInt("supplier_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setReorderLevel(rs.getInt("reorder_level"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setCreatedAt(rs.getTimestamp("created_at"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting inventory by ID: " + e.getMessage());
        }
        
        return item;
    }

    public boolean addInventoryItem(Inventory item) {
        String sql = "INSERT INTO inventory (product_name, description, supplier_id, quantity, reorder_level, unit_price) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, item.getProductName());
            stmt.setString(2, item.getDescription());
            stmt.setInt(3, item.getSupplierId());
            stmt.setInt(4, item.getQuantity());
            stmt.setInt(5, item.getReorderLevel());
            stmt.setBigDecimal(6, item.getUnitPrice());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 1) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setProductId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding inventory item: " + e.getMessage());
        }
        
        return false;
    }

    public boolean updateInventoryItem(Inventory item) {
        String sql = "UPDATE inventory SET product_name = ?, description = ?, supplier_id = ?, quantity = ?, reorder_level = ?, unit_price = ? WHERE product_id = ?";
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, item.getProductName());
            stmt.setString(2, item.getDescription());
            stmt.setInt(3, item.getSupplierId());
            stmt.setInt(4, item.getQuantity());
            stmt.setInt(5, item.getReorderLevel());
            stmt.setBigDecimal(6, item.getUnitPrice());
            stmt.setInt(7, item.getProductId());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows == 1;
        } catch (SQLException e) {
            System.err.println("Error updating inventory item: " + e.getMessage());
        }
        
        return false;
    }

    public boolean updateInventoryQuantity(int productId, int quantityChange) {
        String sql = "UPDATE inventory SET quantity = quantity + ? WHERE product_id = ?";
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, quantityChange);
            stmt.setInt(2, productId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows == 1;
        } catch (SQLException e) {
            System.err.println("Error updating inventory quantity: " + e.getMessage());
        }
        
        return false;
    }

    public boolean deleteInventoryItem(int productId) {
        String sql = "DELETE FROM inventory WHERE product_id = ?";
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, productId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows == 1;
        } catch (SQLException e) {
            System.err.println("Error deleting inventory item: " + e.getMessage());
        }
        
        return false;
    }

    public int getTotalProducts() {
        String sql = "SELECT COUNT(*) AS total FROM inventory";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error getting total products: " + e.getMessage());
        }
        return 0;
    }

    public int getReorderItemsCount() {
        String sql = "SELECT COUNT(*) AS total FROM inventory WHERE quantity <= reorder_level";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error getting reorder items count: " + e.getMessage());
        }
        return 0;
    }
}