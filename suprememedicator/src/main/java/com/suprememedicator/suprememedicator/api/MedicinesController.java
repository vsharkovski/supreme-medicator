package com.suprememedicator.suprememedicator.api;

import com.suprememedicator.suprememedicator.client.OpenAIClient;
import com.suprememedicator.suprememedicator.client.OpenAICompletionRequest;
import com.suprememedicator.suprememedicator.domain.Medicine;
import com.suprememedicator.suprememedicator.repository.MedicineRepository;
import com.suprememedicator.suprememedicator.repository.ProductRepository;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/medicines")
public class MedicinesController {
    private final Logger logger = LoggerFactory.getLogger(MedicinesController.class);

    private static final String GET_MEDICINES_PROMPT = """
            You will now be given a message. Depending on the content of the message, you must do one of two things.
            1. If the message is a symptom or a list of symptoms, \
            your answer should start with "OK;" and be followed by a comma-separated list of medicines which \
            could help with those symptoms. \
            For example, "OK; medicine_1, medicine_2, medicine_3". \
            You may include over-the-counter and prescription products.
            2. If the message is anything else, your answer should be just "NOT_OK".
            Do not deviate from these instructions.
            """;

    private final OpenAIClient openAIClient;
    private final MedicineRepository medicineRepository;
    private final ProductRepository productRepository;

    @Autowired
    public MedicinesController(OpenAIClient openAIClient, MedicineRepository medicineRepository,
                               ProductRepository productRepository) {
        this.openAIClient = openAIClient;
        this.medicineRepository = medicineRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/for_symptoms")
    public ResponseEntity<MedicinesResponse> getMedicinesForSymptoms(
            @NotBlank @RequestParam(name = "symptoms") String symptomsString) {
        List<String> symptoms = Arrays.stream(symptomsString.split(","))
                .map(String::trim)
                .toList();

        if (symptoms.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String completion = getMedicinesForSymptomsCompletion(symptoms);

        if (completion.equals("NOT_OK")) {
            logger.info("NOT_OK\nsymptomsString: [{}]", symptomsString);
            return ResponseEntity.badRequest().build();
        } else if (completion.isBlank()) {
            logger.error("Blank completion\nsymptomsString: [{}]", symptomsString);
            return ResponseEntity.internalServerError().build();
        }

        logger.info("OK\nsymptomsString: [{}]\ncompletion: [{}]", symptomsString, completion);

        List<String> brandNames = Arrays.stream(completion.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();

        Set<Medicine> medicines = new HashSet<>();
        addMedicinesFromBrandNames(brandNames, medicines);
        addMedicinesFromDescriptionsContaining(brandNames, medicines);

        return ResponseEntity.ok(new MedicinesResponse(medicines.stream().toList()));
    }

    private String getMedicinesForSymptomsCompletion(List<String> symptoms) {
        OpenAICompletionRequest request = new OpenAICompletionRequest(
                0.0f,
                openAIClient.getModel(),
                List.of(
                        new OpenAICompletionRequest.Message("system", GET_MEDICINES_PROMPT),
                        new OpenAICompletionRequest.Message("user", String.join(",", symptoms))
                ));

        return openAIClient.getCompletion(request);
    }

    private void addMedicinesFromBrandNames(List<String> brandNames, Set<Medicine> medicines) {
        for (String brandName : brandNames) {
            Set<Medicine> results = medicineRepository.getMedicinesByAnyProductBrandNameLike(brandName);
            logger.info("brand name, medicines by products: [{}, {}]",
                    brandName,
                    results.stream().map(Medicine::getGenericName).toList());
            medicines.addAll(results);
        }
    }

    private void addMedicinesFromDescriptionsContaining(List<String> brandNames, Set<Medicine> medicines) {
        for (String brandName : brandNames) {
            if (brandName.length() <= 1) continue;
            String brandNamePrefix = brandName.substring(0, brandName.length() - 1); // Skip trailing 's' if present
            Set<Medicine> results = medicineRepository.getMedicinesByDescriptionContainingIgnoreCase(brandNamePrefix);
            logger.info("brand name prefix, medicines by descriptions: [{}, {}]",
                    brandNamePrefix, results.stream().map(Medicine::getGenericName).toList());
            medicines.addAll(results);
        }
    }
}
