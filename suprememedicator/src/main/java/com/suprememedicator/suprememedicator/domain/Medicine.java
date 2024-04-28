package com.suprememedicator.suprememedicator.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Entity
@Table(name = "medicines")
public class Medicine {
    @Transient
    @JsonIgnore
    public static final int GENERIC_NAME_MAX_LENGTH = 40;
    @Transient
    @JsonIgnore
    public static final int DESCRIPTION_MAX_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long medicine_id;

    @Column(length = GENERIC_NAME_MAX_LENGTH)
    @NotNull
    private String genericName;

    @Column(length = DESCRIPTION_MAX_LENGTH)
    private String description;

    @OneToMany(mappedBy = "medicine", fetch = FetchType.EAGER)
    private List<Product> products;

    public Medicine() {
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
        this.description = description;
    }

    public void setDescriptionTrimmed(String description) {
        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            this.description = description.substring(0, DESCRIPTION_MAX_LENGTH);
        } else {
            this.description = description;
        }
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public void setGenericNameTrimmed(String genericName) {
        if (genericName.length() > GENERIC_NAME_MAX_LENGTH) {
            this.genericName = genericName.substring(0, GENERIC_NAME_MAX_LENGTH);
        } else {
            this.genericName = genericName;
        }
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
