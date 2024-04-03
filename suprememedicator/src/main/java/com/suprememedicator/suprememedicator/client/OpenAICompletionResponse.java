package com.suprememedicator.suprememedicator.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/**
 * Class for OpenAI chat completion responses as described
 * <a href="https://platform.openai.com/docs/api-reference/chat">here</a>
 */

public record OpenAICompletionResponse(
        String id,
        String object,
        long created,
        String model,
        UsageMetrics usage,
        List<Choice> choices
) {
    public record UsageMetrics(int promptTokens, int completionTokens, int totalTokens) {
        @JsonCreator
        public UsageMetrics(@JsonProperty("prompt_tokens") int promptTokens,
                            @JsonProperty("completion_tokens") int completionTokens,
                            @JsonProperty("total_tokens") int totalTokens) {
            this.promptTokens = promptTokens;
            this.completionTokens = completionTokens;
            this.totalTokens = totalTokens;
        }
    }

    public record Choice(int index, Message message, String finishReason) {
        public record Message(String role, String content) {
            @JsonCreator
            public Message(@JsonProperty("role") String role, @JsonProperty("content") String content) {
                this.role = role;
                this.content = content;
            }
        }

        @JsonCreator
        public Choice(@JsonProperty("index") int index,
                      @JsonProperty("message") Message message,
                      @JsonProperty("finish_reason") String finishReason) {
            this.message = message;
            this.finishReason = finishReason;
            this.index = index;
        }
    }

    @JsonCreator
    public OpenAICompletionResponse(@JsonProperty("id") String id, @JsonProperty("object") String object,
                                    @JsonProperty("created") long created, @JsonProperty("model") String model,
                                    @JsonProperty("usage") UsageMetrics usage, @JsonProperty("choices") List<Choice> choices) {
        this.id = id;
        this.object = object;
        this.created = created;
        this.model = model;
        this.usage = usage;
        this.choices = choices != null ? choices : Collections.emptyList();
    }
}