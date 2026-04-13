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
        if (guestUsers.containsKey(user.getEmail())) {
            throw new RuntimeException("duplicate user " + user.getEmail());
        }
        guestUsers.put(user.getEmail(), user);
        return user;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (!guestUsers.containsKey(email)) {
            return Optional.empty();
        }
        return Optional.of(guestUsers.get(email));
    }

}
