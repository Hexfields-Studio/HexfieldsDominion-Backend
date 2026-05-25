package de.hexfieldsstudio.hexfieldsdominion.game;

import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;

public record Field (AxialPosition pos, int numberChip, ResourceType resource) {}