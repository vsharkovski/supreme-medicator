package com.suprememedicator.suprememedicator.service;

import com.suprememedicator.suprememedicator.domain.EDosageType;
import com.suprememedicator.suprememedicator.domain.Medicine;
import com.suprememedicator.suprememedicator.domain.Product;
import com.suprememedicator.suprememedicator.repository.MedicineRepository;
import com.suprememedicator.suprememedicator.repository.ProductRepository;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DatasetMedicineParser extends DefaultHandler {
    private final MedicineRepository medicineRepository;
    private final ProductRepository productRepository;

    private final StringBuilder currentValue = new StringBuilder();
    Medicine currentMedicine;
    boolean isProduct = false;
    List<Product> products;
    Product currentProduct;
    BigDecimal totalPrice;
    int pricesCount = 0;
    BigDecimal averagePrice;

    public DatasetMedicineParser(MedicineRepository medicineRepository,
                                 ProductRepository productRepository) {
        this.medicineRepository = medicineRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void startDocument() {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        currentValue.setLength(0);

        if (qName.equalsIgnoreCase("products")) {
            products = new ArrayList<>();
            isProduct = true;
        } else if (qName.equalsIgnoreCase("drug")) {
            currentMedicine = new Medicine();
        } else if (qName.equalsIgnoreCase("product")) {
            currentProduct = new Product();
        } else if (qName.equalsIgnoreCase("prices")) {
            totalPrice = new BigDecimal(0);
            averagePrice = new BigDecimal(0);
            pricesCount = 0;
            currentProduct = new Product();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equalsIgnoreCase("name")) {
            if (!isProduct) {
                if (currentMedicine.getGenericName() == null) {
                    currentMedicine.setGenericName(currentValue.toString());
                }
            } else {
                if (currentProduct.getBrandName() == null) {
                    currentProduct.setBrandName(currentValue.toString());
                }
            }
        } else if (qName.equalsIgnoreCase("description")) {
            if (currentMedicine.getDescription() == null) {
                currentMedicine.setDescription(currentValue.toString());
            }
        } else if (qName.equalsIgnoreCase("products")) {
            isProduct = false;
            currentMedicine.setProducts(products);
        } else if (qName.equalsIgnoreCase("drug")) {
            medicineRepository.save(currentMedicine);
            for (Product product : products) {
                product.setMedicine(currentMedicine);
                productRepository.save(product);
            }
        } else if (qName.equalsIgnoreCase("generic")) {
            currentProduct.setGeneric(Boolean.parseBoolean(currentValue.toString()));
        } else if (qName.equalsIgnoreCase("dosage-form")) {
            EDosageType dosageType = EDosageType.valueOfLabel(currentValue.toString().toLowerCase());
            if (dosageType != null) {
                currentProduct.setDosageType(dosageType);
            }
        } else if (qName.equalsIgnoreCase("over-the-counter")) {
            currentProduct.setOverTheCounter(Boolean.parseBoolean(currentValue.toString()));
        } else if (qName.equalsIgnoreCase("product")) {
            products.add(currentProduct);
        } else if (qName.equalsIgnoreCase("cost")) {
            try {
                BigDecimal bd = new BigDecimal(currentValue.toString());
                totalPrice = totalPrice.add(bd);
                pricesCount++;
            } catch (Exception ignored) {
            }
        } else if (qName.equalsIgnoreCase("prices")) {
            try {
                averagePrice = totalPrice.divide(BigDecimal.valueOf(pricesCount));
            } catch (Exception e) {
                averagePrice = new BigDecimal(-1);
            }
            for (Product product : products) {
                product.setPrice(averagePrice);
            }
            currentMedicine.setProducts(products);
        }
    }

    public void characters(char[] ch, int start, int length) {
        currentValue.append(ch, start, length);
    }
}
