package com.suprememedicator.suprememedicator.api;

import com.suprememedicator.suprememedicator.client.OpenAIClient;
import com.suprememedicator.suprememedicator.client.OpenAICompletionRequest;
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
import java.util.List;

@RestController
@RequestMapping("/api/explain_symptoms")
public class SymptomsExplanationController {
    private final Logger logger = LoggerFactory.getLogger(SymptomsExplanationController.class);

    // TODO: 'heartache' returns NOT_OK, why?
    private static final String EXPLAIN_SYMPTOMS_PROMPT = """
            You will now be given a message. Depending on the content of the message, you must do one of two things.
            1. If the message is from someone feeling unwell, and is just a single symptom, \
            or a comma-separated list of symptoms, \
            your answer should be a 3 to 5 sentence explanation for why these symptoms could happen. \
            Use a formal, neutral tone.
            2. If the message is anything else, your answer should be just "NOT_OK"
            Do not deviate from these instructions.
            """;

    private final OpenAIClient openAIClient;

    @Autowired
    public SymptomsExplanationController(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    @GetMapping("/from_symptoms")
    public ResponseEntity<SymptomsExplanationResponse> getExplanation(
            @NotBlank @RequestParam(name = "symptoms") String symptomsString) {
        List<String> symptoms = Arrays.stream(symptomsString.split(","))
                .map(String::trim)
                .toList();

        if (symptoms.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String completion = getSymptomsExplanationCompletion(symptoms);

        if (completion.equals("NOT_OK")) {
            logger.info("NOT_OK\nsymptomsString: [{}]", symptomsString);
            return ResponseEntity.badRequest().build();
        } else if (completion.isBlank()) {
            logger.error("Blank completion\nsymptomsString: [{}]", symptomsString);
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(new SymptomsExplanationResponse(completion));
    }

    private String getSymptomsExplanationCompletion(List<String> symptoms) {
        OpenAICompletionRequest request = new OpenAICompletionRequest(
                1.0f,
                openAIClient.getModel(),
                List.of(
                        new OpenAICompletionRequest.Message("system", EXPLAIN_SYMPTOMS_PROMPT),
                        new OpenAICompletionRequest.Message("user", String.join(",", symptoms))
                ));

        return openAIClient.getCompletion(request);
    }

}
