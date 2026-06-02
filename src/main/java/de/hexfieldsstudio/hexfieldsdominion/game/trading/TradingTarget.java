package de.hexfieldsstudio.hexfieldsdominion.game.trading;

public record TradingTarget(boolean allPlayers, Integer playerId) {
    private TradingTarget(Integer playerId) {
        this(false, playerId);
    }

    public static TradingTarget ofPlayer(Integer playerId) {
        return new TradingTarget(playerId);
    }
}