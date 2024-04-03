package com.suprememedicator.suprememedicator.api;

import com.suprememedicator.suprememedicator.domain.Medicine;

import java.util.List;

public record MedicinesResponse(List<Medicine> medicines) {
}
