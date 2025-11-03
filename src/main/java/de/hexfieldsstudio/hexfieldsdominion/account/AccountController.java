package de.hexfieldsstudio.hexfieldsdominion.account;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/auth")
public class AccountController {

    @PostMapping("/login")
    private void login() {
        
    }

    @PostMapping("/register")
    private void register() {

    }

    private void guest() {

    }
}