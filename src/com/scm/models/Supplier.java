package com.scm.models;

import java.sql.Timestamp;

public record Supplier(
    int supplierId,
    String supplierName,
    String contactPerson,
    String email,
    String phone,
    String address,
    Timestamp createdAt
) {
    // Custom constructor for default values
    public Supplier() {
        this(0, "", "", "", "", "", new Timestamp(System.currentTimeMillis()));
    }

    @Override
    public String toString() {
        return String.format("Supplier[id=%d, name=%s, contact=%s, email=%s, phone=%s]",
            supplierId, supplierName, contactPerson, email, phone);
    }
}