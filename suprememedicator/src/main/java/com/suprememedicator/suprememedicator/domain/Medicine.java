package com.suprememedicator.suprememedicator.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Entity
@Table(name = "medicines")
public class Medicine {
    @Transient
    @JsonIgnore
    public final int DESCRIPTION_MAX_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long medicine_id;

    @NotNull
    private String genericName;

    @Length(max = DESCRIPTION_MAX_LENGTH)
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
        return medicine_id;
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

    public void setId(Long id) {
        this.medicine_id = id;
    }

    public void setDescription(String description) {
        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            this.description = description.substring(0, DESCRIPTION_MAX_LENGTH);
        } else {
            this.description = description;
        }
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
