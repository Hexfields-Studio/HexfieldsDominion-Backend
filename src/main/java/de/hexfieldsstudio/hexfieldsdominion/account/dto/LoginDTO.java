package de.hexfieldsstudio.hexfieldsdominion.account.dto;

import lombok.Builder;

@Builder
public record LoginDTO(String username, String password) {}
