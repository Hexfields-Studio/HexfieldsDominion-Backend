package de.hexfieldsstudio.hexfieldsdominion.game;

import lombok.NonNull;
import org.springframework.data.util.Pair;

public class Field {
    private Pair<@NonNull Integer, @NonNull Integer> pos;
    private int number;
    private Resource resource;
}