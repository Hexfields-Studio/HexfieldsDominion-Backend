package de.hexfieldsstudio.hexfieldsdominion.game.trading;

import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.error.BadRequestException;
import de.hexfieldsstudio.hexfieldsdominion.game.Match;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.TradeBankDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.TradePlayerDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.error.NotEnoughResourcesException;
import de.hexfieldsstudio.hexfieldsdominion.game.player.PlayerRepresentation;
import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import de.hexfieldsstudio.hexfieldsdominion.game.types.TradingStatus;
import lombok.Getter;

import java.util.*;

public class TradingHandler {

    public static final int GIVE_GET_RATIO = 4; // (e.g. give:get 4:1 -> 4 / 1)

    @Getter
    private final Map<Integer, PlayerTrade> playerTrades = new HashMap<>();
    private int nextId = 0;

    public void handlePlayerTrade(User user, Match match, TradePlayerDTO dto) {
        switch (dto.getStatus()) {
            case OFFERED -> this.createTrade(user, match, dto);
            case CHANGED -> this.editTrade(user, match, dto);
            case ACCEPTED -> this.acceptTrade(user, match, dto);
            case DENIED -> this.denyTrade(dto);
            case CANCELLED -> this.cancelTrade(dto);
        }
    }

    //TODO: isPlayersTurn, ... validation
    public void createTrade(User user, Match match, TradePlayerDTO dto) {
        Optional<PlayerRepresentation> playerOptional = match.getPlayers().getPlayerForUser(user);
        if (playerOptional.isEmpty()) {
            return;
        }
        PlayerRepresentation player = playerOptional.get();

        this.createTradeForPlayerId(
                null,
                dto.getStatus(),
                dto.getTarget(),
                player.getPublicId(),
                dto.getOffered(),
                dto.getRequested()
        );
    }

    private void createTradeForPlayerId(Integer predecessorId, TradingStatus status, TradingTarget target, int createdBy, Map<ResourceType, Integer> offered, Map<ResourceType, Integer> requested) {
        int id = nextId++;
        PlayerTrade trade = new PlayerTrade(
                id,
                predecessorId,
                status,
                target,
                createdBy,
                offered,
                requested
        );
        playerTrades.put(id, trade);
    }

    public void editTrade(User user, Match match, TradePlayerDTO dto) {
        PlayerTrade trade = playerTrades.get(dto.getId());
        if (trade.getTarget().allPlayers()) {
            // create separate trade for the two players only
            Optional<PlayerRepresentation> playerOptional = match.getPlayers().getPlayerForUser(user);
            if (playerOptional.isEmpty()) {
                return;
            }
            PlayerRepresentation player = playerOptional.get();

            this.createTradeForPlayerId(
                    trade.getId(),
                    TradingStatus.CHANGED,
                    TradingTarget.ofPlayer(player.getPublicId()),
                    trade.getCreatedBy(),
                    dto.getOffered(),
                    dto.getRequested()
            );
            return;
        }

        trade.setStatus(dto.getStatus());
        trade.setOffered(dto.getOffered());
        trade.setRequested(dto.getRequested());
    }

    public void acceptTrade(User user, Match match, TradePlayerDTO dto) {
        PlayerTrade trade = playerTrades.get(dto.getId());

        if (trade.getStatus() == TradingStatus.OFFERED) {
            Optional<PlayerRepresentation> playerOptional = match.getPlayers().getPlayerForUser(user);
            if (playerOptional.isEmpty()) {
                return;
            }
            PlayerRepresentation player = playerOptional.get();
            trade.setTarget(TradingTarget.ofPlayer(player.getPublicId()));
        } else if (trade.getStatus() != TradingStatus.CHANGED) {
            return;
        }
        trade.setStatus(dto.getStatus());

        Optional<PlayerRepresentation> playerCreatedOptional = match.getPlayers().getPlayerById(trade.getCreatedBy());
        if (playerCreatedOptional.isEmpty()) {
            return;
        }
        PlayerRepresentation playerCreated = playerCreatedOptional.get();

        Optional<PlayerRepresentation> playerTargetOptional = trade.getTarget().allPlayers()
            // current user is the first player who accepted
            ? match.getPlayers().getPlayerForUser(user)
            : match.getPlayers().getPlayerById(trade.getTarget().playerId());
        if (playerTargetOptional.isEmpty()) {
            return;
        }
        PlayerRepresentation playerTarget = playerTargetOptional.get();

        trade.getOffered().forEach((resource, amount) -> {
            playerTarget.getResources().compute(resource,
                    (k, v) -> (v == null) ? amount : v + amount);
            playerCreated.getResources().computeIfPresent(resource,
                    (k, v) -> v - amount);
        });
        trade.getRequested().forEach((resource, amount) -> {
            playerCreated.getResources().compute(resource,
                    (k, v) -> (v == null) ? amount : v + amount);
            playerTarget.getResources().computeIfPresent(resource,
                    (k, v) -> v - amount);
        });
    }

    public void denyTrade(TradePlayerDTO dto) {
        PlayerTrade trade = playerTrades.get(dto.getId());
        if (trade.getStatus() == TradingStatus.OFFERED && trade.getTarget().allPlayers()) {
            // don't cancel the offer sent to everyone
            return;
        }
        trade.setStatus(dto.getStatus());
    }

    public void cancelTrade(TradePlayerDTO dto) {
        PlayerTrade trade = playerTrades.get(dto.getId());
        trade.setStatus(dto.getStatus());
    }

    public void tradeBank(User user, Match match, TradeBankDTO dto) throws NotEnoughResourcesException {
        Optional<PlayerRepresentation> playerOptional = match.getPlayers().getPlayerForUser(user);
        if (playerOptional.isEmpty()) {
            return;
        }
        PlayerRepresentation player = playerOptional.get();

        Integer ownedAmount = player.getResources().get(dto.getResourceOffered());
        if (dto.getAmountOffered() != dto.getAmountRequested() * GIVE_GET_RATIO) {
            throw new BadRequestException("Invalid offered:requested ratio.");
        }
        if (ownedAmount == null || dto.getAmountOffered() > ownedAmount) {
            throw new NotEnoughResourcesException();
        }

        player.getResources().compute(dto.getResourceRequested(),
                (k, v) -> (v == null)
                        ? dto.getAmountRequested()
                        : v + dto.getAmountRequested());
        player.getResources().replace(dto.getResourceOffered(), ownedAmount - dto.getAmountOffered());
    }

    public void clearTrades() {
        playerTrades.clear();
    }

}
