package de.hexfieldsstudio.hexfieldsdominion.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.util.Pair;

@AllArgsConstructor
@Getter
public class Field {
    private Pair<@NonNull Integer, @NonNull Integer> pos;
    private int number;
    private Resource resource;
}