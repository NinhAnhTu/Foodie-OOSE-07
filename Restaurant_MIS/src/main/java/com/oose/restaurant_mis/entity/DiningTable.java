package com.oose.restaurant_mis.entity;

import com.oose.restaurant_mis.enums.TableStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "restaurant_tables")
public class DiningTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "table_id")
    private Integer tableId;

    @Column(name = "table_name", nullable = false, length = 20)
    private String tableName;

    @Column(name = "table_code", nullable = false, unique = true, length = 10)
    private String tableCode;

    @Min(1)
    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('EMPTY', 'OCCUPIED', 'RESERVED') DEFAULT 'EMPTY'")
    private TableStatus status = TableStatus.EMPTY;

    @Transient
    private boolean hasPendingPayment;

    @Column(length = 50)
    private String zone;

    // Constructors
    public DiningTable() {}

    public DiningTable(Integer tableId, String tableName, String tableCode, Integer capacity, TableStatus status, String zone) {
        this.tableId = tableId;
        this.tableName = tableName;
        this.tableCode = tableCode;
        this.capacity = capacity;
        this.status = status;
        this.zone = zone;
    }

    // Getters and Setters
    public Integer getTableId() { return tableId; }
    public void setTableId(Integer tableId) { this.tableId = tableId; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public String getTableCode() { return tableCode; }
    public void setTableCode(String tableCode) { this.tableCode = tableCode; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public TableStatus getStatus() { return status; }
    public void setStatus(TableStatus status) { this.status = status; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }
    public boolean isHasPendingPayment() {
        return hasPendingPayment;
    }

    public void setHasPendingPayment(boolean hasPendingPayment) {
        this.hasPendingPayment = hasPendingPayment;
    }
}