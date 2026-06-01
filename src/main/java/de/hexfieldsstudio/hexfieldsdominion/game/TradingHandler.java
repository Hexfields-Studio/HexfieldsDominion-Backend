package de.hexfieldsstudio.hexfieldsdominion.game;

import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.error.BadRequestException;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.TradeBankDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.error.NotEnoughResourcesException;
import de.hexfieldsstudio.hexfieldsdominion.game.player.PlayerRepresentation;

import java.util.Optional;

public class TradingHandler {

    private static final int GIVE_GET_RATIO = 4; // (e.g. give:get 4:1 -> 4 / 1)

    //private int nextId = 0;

    /*public void createTrade() {

    }*/

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

}
