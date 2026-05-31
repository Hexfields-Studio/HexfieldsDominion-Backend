package de.hexfieldsstudio.hexfieldsdominion.game.board;

import de.hexfieldsstudio.hexfieldsdominion.game.AxialPosition;
import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;

public record Field (AxialPosition pos, int numberChip, ResourceType resource) {}