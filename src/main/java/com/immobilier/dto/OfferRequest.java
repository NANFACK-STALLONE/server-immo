package com.immobilier.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class OfferRequest {

    private Double amount;

    @NotBlank(message = "Le message est requis")
    private String message;
}
