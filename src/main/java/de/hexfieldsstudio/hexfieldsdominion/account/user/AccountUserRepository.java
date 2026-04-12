package de.hexfieldsstudio.hexfieldsdominion.account.user;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountUserRepository extends UserRepository, JpaRepository<User, Integer> {

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    User save(@NonNull User user);

}
