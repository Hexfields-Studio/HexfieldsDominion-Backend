package de.hexfieldsstudio.hexfieldsdominion.account.user;

import lombok.NonNull;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class GuestUserRepository implements UserRepository {

    private final Map<String, User> guestUsers = new HashMap<>();

    @Override
    @NonNull
    public User save(@NonNull User user) {
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

    @Override
    public Optional<User> findByUsernameIgnoreCase(String username) {
        Optional<String> existingUsernameOptional = guestUsers.keySet().stream()
                .filter(storedUsername -> storedUsername.equalsIgnoreCase(username))
                .findFirst();
        return existingUsernameOptional.map(guestUsers::get);
    }

    @Override
    public void deleteAll() {
        guestUsers.clear();
    }
}
