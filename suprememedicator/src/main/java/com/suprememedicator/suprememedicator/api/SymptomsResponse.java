package com.suprememedicator.suprememedicator.api;

import java.util.List;

public record SymptomsResponse(List<String> symptoms, String message) {
    public SymptomsResponse(List<String> symptoms) {
        this(symptoms, null);
    }
}
