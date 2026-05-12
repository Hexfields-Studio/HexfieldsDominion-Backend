package de.hexfieldsstudio.hexfieldsdominion.game.player;

import de.hexfieldsstudio.hexfieldsdominion.account.user.Role;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Player {
    private final int id;
    private final boolean isAccount;
    private final User user;

    public Player(User user, int id) {
        this.user = user;
        this.id = id;
        this.isAccount = !user.getRole().equals(Role.GUEST);
    }

    public String getUsername() {
        return user.getUsername();
    }
}