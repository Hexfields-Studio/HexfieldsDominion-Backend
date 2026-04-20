package de.hexfieldsstudio.hexfieldsdominion.account.user;

import lombok.NonNull;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class GuestUserRepository implements UserRepository {

    Map<String, User> guestUsers = new HashMap<>();

    @Override
    @NonNull
    public User save(@NonNull User user) {
        if (guestUsers.containsKey(user.getUsername())) {
            throw new RuntimeException("duplicate user " + user.getUsername());
        }
        guestUsers.put(user.getUsername(), user);
        return user;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (!guestUsers.containsKey(username)) {
            return Optional.empty();
        }
        return Optional.of(guestUsers.get(username));
    }

}
