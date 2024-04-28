package com.suprememedicator.suprememedicator.service;

import com.suprememedicator.suprememedicator.repository.MedicineRepository;
import com.suprememedicator.suprememedicator.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

@Service
public class DatabaseImportService {
    private final Logger logger = LoggerFactory.getLogger(DatabaseImportService.class);

    @Value("${suprememedicator.database.import.should-import}")
    private boolean shouldImportDatabase = false;

    @Value("${suprememedicator.database.import.dataset.path}")
    private String datasetPathString = "";

    private final MedicineRepository medicineRepository;

    private final ProductRepository productRepository;

    @Autowired
    public DatabaseImportService(MedicineRepository medicineRepository, ProductRepository productRepository) {
        this.medicineRepository = medicineRepository;
        this.productRepository = productRepository;
    }

    @EventListener
    void importDatabaseStartupListener(ContextRefreshedEvent event) {
        if (!shouldImportDatabase) {
            return;
        }

        logger.info("Importing database using dataset at: [{}]", datasetPathString);

        Path datasetPath;
        try {
            datasetPath = Path.of(datasetPathString);
        } catch (InvalidPathException exception) {
            logger.error("Invalid dataset file path: [{}]", datasetPathString, exception);
            return;
        }

        if (Files.notExists(datasetPath)) {
            logger.error("Could not find dataset at: [{}]", datasetPath);
            return;
        }

        dropDatabase();
        importDataset(datasetPath);
    }

    void dropDatabase() {
        logger.info("Dropping all products and medicines");

        productRepository.deleteAllInBatch();
        medicineRepository.deleteAllInBatch();
    }

    void importDataset(Path datasetPath) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        DatasetParser datasetParser = new DatasetParser(medicineRepository, productRepository);

        SAXParser saxParser;
        try {
            saxParser = factory.newSAXParser();
        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }

        try {
            saxParser.parse(datasetPath.toString(), datasetParser);
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }

        logger.info("Dataset import success.");
    }
}
