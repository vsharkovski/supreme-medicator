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
@RequestMapping("/api/extract_symptoms")
public class SymptomsExtractionController {
    private final Logger logger = LoggerFactory.getLogger(SymptomsExtractionController.class);

    private static final String EXTRACT_SYMPTOMS_PROMPT = """
            You will now be given a message. Depending on the content of the message, you must do one of two things.
            1. If the message is from someone feeling unwell, for example "I can't XXX" or "My XXX hurts", \
            your answer should start with \
            "OK", a semicolon, and then a comma-separated list of symptoms the person is feeling. \
            For example, "OK; symptom1, symptom2, symptom3". \
            The symptoms should be concise and use general medical terminology.
            2. If the message has no symptoms, your answer should be just "NOT_OK".
            Do not deviate from these instructions.
            """;

    private final OpenAIClient openAIClient;

    @Autowired
    public SymptomsExtractionController(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    @GetMapping("/from_input")
    public ResponseEntity<SymptomsResponse> getSymptomsFromInput(@NotBlank @RequestParam String input) {
        String completion = getExtractSymptomsCompletion(input);

        if (completion.equals("NOT_OK")) {
            logger.info("NOT_OK\ninput: [{}]", input);
            return ResponseEntity.badRequest().build();
        } else if (completion.isBlank()) {
            logger.error("Blank completion\ninput: [{}]", input);
            return ResponseEntity.internalServerError().build();
        } else if (!completion.startsWith("OK;")) {
            logger.error("Unexpected completion start\ninput: [{}]\ncompletion: [{}]", input, completion);
            return ResponseEntity.internalServerError().build();
        }

        List<String> symptoms = extractSymptomsFromOKCompletion(completion);
        if (symptoms.isEmpty()) {
            logger.error("No symptoms\ninput: [{}]\ncompletion: [{}]", input, completion);
            return ResponseEntity.internalServerError().build();
        }

        logger.info("OK\ninput: [{}]\ncompletion: [{}]", input, completion);

        return ResponseEntity.ok(new SymptomsResponse(symptoms));
    }

    private String getExtractSymptomsCompletion(String input) {
        OpenAICompletionRequest request = new OpenAICompletionRequest(
                0.0f,
                openAIClient.getModel(),
                List.of(
                        new OpenAICompletionRequest.Message("system", EXTRACT_SYMPTOMS_PROMPT),
                        new OpenAICompletionRequest.Message("user", input)
                ));

        return openAIClient.getCompletion(request);
    }

    private List<String> extractSymptomsFromOKCompletion(String completion) {
        String symptomsString = completion.substring("OK;".length());
        return Arrays.stream(symptomsString.split(","))
                .map(String::trim)
                .toList();
    }
}
