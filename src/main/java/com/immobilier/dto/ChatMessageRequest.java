package com.immobilier.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ChatMessageRequest {

    @NotBlank(message = "Le message est requis")
    private String content;
}
