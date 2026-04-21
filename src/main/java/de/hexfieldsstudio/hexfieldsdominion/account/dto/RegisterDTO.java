package de.hexfieldsstudio.hexfieldsdominion.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RegisterDTO {

    private String username;
    private String password;

}
