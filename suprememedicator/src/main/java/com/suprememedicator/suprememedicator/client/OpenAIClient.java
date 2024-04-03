package com.suprememedicator.suprememedicator.client;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Client for communicating with the OpenAPI completions API
 */

@Component
@Validated
public class OpenAIClient {
    private final Logger logger = LoggerFactory.getLogger(OpenAIClient.class);

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    @Value("${open-ai.api-key}")
    @NotBlank
    private String apiKey;

    @Value("${open-ai.model}")
    @NotBlank
    private String model = "gpt-3.5-turbo";

    @Value("${open-ai.on-fail.retry-count:1}")
    @Min(0)
    private int onFailRetryCount;

    @Value("${open-ai.on-fail.retry-delay:3000}")
    @Min(0)
    private int onFailRetryDelay;

    private final RestTemplate restTemplate;

    public OpenAIClient() {
        this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    public String getModel() {
        return model;
    }

    /**
     * @return The OpenAI chat completion for the given request, or an empty string if some error occurred
     */
    public String getCompletion(OpenAICompletionRequest request) {
        logger.debug("Completion request messages: [\n{}\n]",
                request.messages().stream().map(Record::toString).collect(Collectors.joining("\n")));

        OpenAICompletionResponse response = getResponseWithRetry(request);

        // Return content of message, or an empty string if the response isn't in the proper form
        if (response != null) {
            logger.debug("Usage metrics: [{}]", response.usage());

            OpenAICompletionResponse.Choice choice = response.choices().get(0);
            if (choice != null && choice.message() != null && choice.message().content() != null) {
                logger.debug("Completion: [{}]", choice.message().content());

                return choice.message().content();
            }
        }

        return "";
    }

    private OpenAICompletionResponse getResponseWithRetry(OpenAICompletionRequest requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<OpenAICompletionRequest> requestEntity = new HttpEntity<>(requestBody, headers);

        for (int requestsSent = 0; requestsSent < onFailRetryCount + 1; requestsSent++) {
            ResponseEntity<OpenAICompletionResponse> responseEntity =
                    restTemplate.exchange(API_URL, HttpMethod.POST, requestEntity, OpenAICompletionResponse.class);

            if (responseEntity.getStatusCode().is5xxServerError()) {
                // Internal server error occurred, e.g. server timed out from being overloaded
                try {
                    Thread.sleep(onFailRetryDelay);
                } catch (Exception exception) {
                    logger.error("Exception occurred while waiting between request attempts: [{}]", exception.getMessage(),
                            exception);
                }
            } else {
                return responseEntity.getBody();
            }
        }

        // All retries were exhausted unsuccessfully
        return null;
    }
}