package com.suprememedicator.suprememedicator.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore // Otherwise infinite recursion might happen if serializing Medicine
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @NotNull
    @Column(unique = true) // Should have the database set up an index to speed up searches
    private String brandName;

    @NotNull
    private boolean isOverTheCounter;

    @NotNull
    private boolean isGeneric;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EDosageType dosageType;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    public Product() {
    }

    public Product(Medicine medicine, String brandName, boolean isOverTheCounter, boolean isGeneric,
                   EDosageType dosageType, BigDecimal price) {
        this.medicine = medicine;
        this.brandName = brandName;
        this.isOverTheCounter = isOverTheCounter;
        this.isGeneric = isGeneric;
        this.dosageType = dosageType;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public String getBrandName() {
        return brandName;
    }

    public boolean isOverTheCounter() {
        return isOverTheCounter;
    }

    public boolean isGeneric() {
        return isGeneric;
    }

    public EDosageType getDosageType() {
        return dosageType;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
