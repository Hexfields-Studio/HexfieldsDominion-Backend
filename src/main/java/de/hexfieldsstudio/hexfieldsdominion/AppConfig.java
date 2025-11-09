package de.hexfieldsstudio.hexfieldsdominion;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class AppConfig {
    @Value("${app.lobbyManager.initialCapacity}")
    private int initialCapacity;
}

