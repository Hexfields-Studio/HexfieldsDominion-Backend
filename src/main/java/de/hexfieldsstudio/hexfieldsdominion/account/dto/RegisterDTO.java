package de.hexfieldsstudio.hexfieldsdominion.account.dto;

import lombok.Builder;

@Builder
public record RegisterDTO (String username, String password) {}
