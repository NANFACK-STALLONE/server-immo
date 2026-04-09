package com.immobilier.entity;

public enum RoleEnum {
    ROLE_ADMIN("Administrateur"),
    ROLE_AGENT("Agent Immobilier"),
    ROLE_BUYER("Acheteur"),
    ROLE_SELLER("Vendeur"),
    ROLE_USER("Utilisateur");

    private final String description;

    RoleEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
