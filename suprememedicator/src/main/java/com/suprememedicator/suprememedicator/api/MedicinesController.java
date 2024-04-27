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
            1. If the message is from someone feeling unwell, and is just a single symptom, \
            or a comma-separated list of symptoms:
            Your answer should start with "OK;" and be followed by a comma-separated list of medicines which \
            could help with those symptoms. \
            For example, "OK; medicine_1, medicine_2, medicine_3".
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

        List<String> medicineOrProductNames = Arrays.stream(completion.split(","))
                .map(String::trim)
                .toList();

        Set<Medicine> medicines = new HashSet<>();
        for (String name : medicineOrProductNames) {
            Set<Medicine> newMedicines = medicineRepository.getMedicinesByGenericNameContainingIgnoreCase(name);
            medicines.addAll(newMedicines);
        }

        return ResponseEntity.ok(new MedicinesResponse(medicines.stream().toList()));

//
//        // Return dummy data
//        Medicine m1 = new Medicine("genericName1", "description1", new ArrayList<>());
//        Medicine m2 = new Medicine("genericName2", "description2", new ArrayList<>());
//        Product p1 = new Product(m1, "brandName1", true, true,
//                EDosageType.BOTTLE, new BigDecimal("1.2"));
//        Product p2 = new Product(m1, "brandName2", false, true,
//                EDosageType.CAPSULE, new BigDecimal("3.4"));
//        Product p3 = new Product(m2, "brandName3", true, false,
//                EDosageType.SUPPOSITORY, new BigDecimal("123.45"));
//        m1.getProducts().add(p1);
//        m1.getProducts().add(p2);
//        m2.getProducts().add(p3);
//
//        return ResponseEntity.ok(new MedicinesResponse(List.of(m1, m2)));
    }

    private String getMedicinesForSymptomsCompletion(List<String> symptoms) {
        OpenAICompletionRequest request = new OpenAICompletionRequest(
                1.0f,
                openAIClient.getModel(),
                List.of(
                        new OpenAICompletionRequest.Message("system", GET_MEDICINES_PROMPT),
                        new OpenAICompletionRequest.Message("user", String.join(",", symptoms))
                ));

        return openAIClient.getCompletion(request);
    }
}
