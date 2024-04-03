package com.suprememedicator.suprememedicator.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Entity
@Table(name = "medicines")
public class Medicine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true) // Should have the database set up an index to speed up searches
    private String genericName;

    @NotNull
    private String description;

    @OneToMany(mappedBy = "medicine")
    private List<Product> products;

    public Medicine() {
    }

    public Medicine(String genericName, String description, List<Product> products) {
        this.genericName = genericName;
        this.description = description;
        this.products = products;
    }

    public Long getId() {
        return id;
    }

    public String getGenericName() {
        return genericName;
    }

    public String getDescription() {
        return description;
    }

    public List<Product> getProducts() {
        return products;
    }
}
