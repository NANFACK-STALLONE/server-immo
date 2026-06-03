package com.immobilier.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class VisitRequest {

    private String requestedDate;

    @NotBlank(message = "Le message est requis")
    private String message;
}
