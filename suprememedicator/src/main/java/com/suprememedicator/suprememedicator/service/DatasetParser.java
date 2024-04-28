package com.suprememedicator.suprememedicator.service;

import com.suprememedicator.suprememedicator.domain.EDosageType;
import com.suprememedicator.suprememedicator.domain.Medicine;
import com.suprememedicator.suprememedicator.domain.Product;
import com.suprememedicator.suprememedicator.repository.MedicineRepository;
import com.suprememedicator.suprememedicator.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class DatasetParser extends DefaultHandler {
    private static final int DRUG_TREE_DEPTH = 2;
    private static final int DRUG_PROPERTY_TREE_DEPTH = 3;
    private static final int PRODUCT_PROPERTY_TREE_DEPTH = 5;

    private static final int LOG_MEDICINE_INTERVAL = 1000;
    private final Logger logger = LoggerFactory.getLogger(DatasetParser.class);

    private final MedicineRepository medicineRepository;
    private final ProductRepository productRepository;

    private final StringBuilder currentValue = new StringBuilder();
    int treeDepth;

    Medicine currentMedicine;
    boolean isProduct;
    Map<String, Product> productsByBrandName;
    Product currentProduct;
    boolean productHasMarketingEndDate;
    BigDecimal totalPrice;
    int pricesCount;
    BigDecimal averagePrice;
    int savedMedicineCount;
    int savedProductCount;

    public DatasetParser(MedicineRepository medicineRepository,
                         ProductRepository productRepository) {
        this.medicineRepository = medicineRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        currentValue.append(ch, start, length);
    }

    @Override
    public void startDocument() {
        currentValue.setLength(0);
        treeDepth = 0;

        currentMedicine = null;
        isProduct = false;
        productsByBrandName = null;
        currentProduct = null;
        productHasMarketingEndDate = false;
        totalPrice = null;
        pricesCount = 0;
        averagePrice = null;
        savedMedicineCount = 0;
        savedProductCount = 0;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        treeDepth++;
        currentValue.setLength(0);
        String qNameLowercase = qName.toLowerCase();

        switch (qNameLowercase) {
            case "drug" -> {
                if (treeDepth == DRUG_TREE_DEPTH) {
                    currentMedicine = new Medicine();
                }
            }
            case "products" -> {
                productsByBrandName = new HashMap<>();
            }
            case "product" -> {
                currentProduct = new Product();
                productHasMarketingEndDate = false;
                isProduct = true;
            }
            case "prices" -> {
                totalPrice = BigDecimal.ZERO;
                averagePrice = BigDecimal.ZERO;
                pricesCount = 0;
            }
        }

//        System.out.println("START " + treeDepth + " " + qNameLowercase);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        String qNameLowercase = qName.toLowerCase();

        switch (qNameLowercase) {
            // Both drug and product properties
            case "name" -> {
                if (treeDepth == PRODUCT_PROPERTY_TREE_DEPTH && isProduct) {
                    currentProduct.setBrandNameTrimmed(currentValue.toString().toLowerCase());
                } else if (treeDepth == DRUG_PROPERTY_TREE_DEPTH) {
                    currentMedicine.setGenericNameTrimmed(currentValue.toString().toLowerCase());
                }
            }
            // Drug properties
            case "description" -> {
                if (treeDepth == DRUG_PROPERTY_TREE_DEPTH) {
                    currentMedicine.setDescriptionTrimmed(currentValue.toString());
                }
            }
            // Product properties
            case "generic" -> {
                boolean isGeneric = Boolean.parseBoolean(currentValue.toString());
                currentProduct.setGeneric(isGeneric);
            }
            case "dosage-form" -> {
                EDosageType dosageType = EDosageType.valueOfLabel(currentValue.toString().toLowerCase());
                currentProduct.setDosageType(dosageType);
            }
            case "over-the-counter" -> {
                boolean isOverTheCounter = Boolean.parseBoolean(currentValue.toString());
                currentProduct.setOverTheCounter(isOverTheCounter);
            }
            case "ended-marketing-on" -> {
                productHasMarketingEndDate = !currentValue.isEmpty();
            }
            // Product end
            case "product" -> {
                // Add product if no marketing end date i.e. it is still being marketed
                if (!productHasMarketingEndDate) {
                    productsByBrandName.putIfAbsent(currentProduct.getBrandName(), currentProduct);
                }

                isProduct = false;
            }
            // Prices properties
            case "cost" -> {
                try {
                    BigDecimal bd = new BigDecimal(currentValue.toString());
                    totalPrice = totalPrice.add(bd);
                    pricesCount++;
                } catch (Exception ignored) {
                }
            }
            // Prices end
            case "prices" -> {
                try {
                    averagePrice = totalPrice.divide(BigDecimal.valueOf(pricesCount), RoundingMode.DOWN);
                } catch (Exception e) {
                    averagePrice = null;
                }
            }
            // Drug end
            case "drug" -> {
                // Only save the medicine if it has at least one product
                if (!productsByBrandName.isEmpty()) {
                    // Save the medicine, and get the same object with its real ID
                    currentMedicine = medicineRepository.save(currentMedicine);

                    // Save the products
                    for (Product product : productsByBrandName.values()) {
                        // Set the product's average price
                        product.setPrice(averagePrice);
                        // Set the product's medicine, i.e. the medicine ID
                        product.setMedicine(currentMedicine);
                        // Save the product
                        productRepository.save(product);
                        savedProductCount++;
                    }

                    savedMedicineCount++;
                    if (savedMedicineCount % LOG_MEDICINE_INTERVAL == 0) {
                        logCounts();
                    }
                }
            }
        }

//        System.out.println("END " + treeDepth + " " + qNameLowercase);
        treeDepth--;
    }

    @Override
    public void endDocument() {
        logCounts();
    }

    private void logCounts() {
        logger.info("Total saved medicines so far: [{}]; Total saved products so far: [{}]",
                savedMedicineCount, savedProductCount);
    }
}
