package de.hexfieldsstudio.hexfieldsdominion.account.user;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@AllArgsConstructor
public class AllUserRepository implements UserRepository {
    private final AccountUserRepository accountUserRepository;
    private final GuestUserRepository guestUserRepository;

    @Override
    @NonNull
    public User save(@NonNull User user) {
        return getRepositoryByUser(user).save(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Optional<User> optionalGuest = guestUserRepository.findByEmail(email);
        if (optionalGuest.isPresent()) {
            return optionalGuest;
        }
        return accountUserRepository.findByEmail(email);
    }

    private UserRepository getRepositoryByUser(User user) {
        return (user.getRole() == Role.GUEST) ? guestUserRepository : accountUserRepository;
    }

}
