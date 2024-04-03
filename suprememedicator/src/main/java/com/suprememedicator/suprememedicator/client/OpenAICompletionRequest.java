package com.suprememedicator.suprememedicator.client;

import java.util.List;

/**
 * Class for OpenAI chat completion requests as described
 * <a href="https://platform.openai.com/docs/api-reference/chat">here</a>.
 */

public record OpenAICompletionRequest(float temperature, String model, List<Message> messages) {
    public record Message(String role, String content) {
    }
}
