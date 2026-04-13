package de.hexfieldsstudio.hexfieldsdominion.account.user;

import lombok.NonNull;

import java.util.Optional;

public interface UserRepository {

    @NonNull
    User save(@NonNull  User user);

    Optional<User> findByEmail(String email);

}
