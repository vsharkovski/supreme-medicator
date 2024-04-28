package com.suprememedicator.suprememedicator.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {
    @Transient
    @JsonIgnore
    public static final int BRAND_NAME_MAX_LENGTH = 40;
    @Transient
    @JsonIgnore
    public static final int DOSAGE_TYPE_MAX_LENGTH = 12;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore // Otherwise infinite recursion might happen if serializing Medicine
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(length = BRAND_NAME_MAX_LENGTH)
    @NotNull
    private String brandName;

    @NotNull
    private boolean isOverTheCounter;

    @NotNull
    private boolean isGeneric;

    @Column(length = DOSAGE_TYPE_MAX_LENGTH)
    @Enumerated(EnumType.STRING)
    private EDosageType dosageType;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    public Product() {
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public void setBrandNameTrimmed(String brandName) {
        if (brandName.length() > BRAND_NAME_MAX_LENGTH) {
            this.brandName = brandName.substring(0, BRAND_NAME_MAX_LENGTH);
        } else {
            this.brandName = brandName;
        }
    }

    public void setDosageType(EDosageType dosageType) {
        this.dosageType = dosageType;
    }

    public void setGeneric(boolean generic) {
        isGeneric = generic;
    }

    public  void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setOverTheCounter(boolean overTheCounter) {
        isOverTheCounter = overTheCounter;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }
}
