package de.hexfieldsstudio.hexfieldsdominion.account.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AllUserRepositoryTest {

    @InjectMocks
    private AllUserRepository allUserRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @Mock
    private GuestUserRepository guestUserRepository;

    private User guestUser;
    private User accountUser;

    @BeforeEach
    public void setupEach() {
        guestUser = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .build();

        accountUser = User.builder()
                .username("testuser2")
                .role(Role.PLAYER)
                .build();
    }

    @Test
    public void testSaveGuest() {
        allUserRepository.save(guestUser);

        verify(guestUserRepository).save(guestUser);
        verifyNoInteractions(accountUserRepository);
    }

    @Test
    public void testSaveAccount() {
        allUserRepository.save(accountUser);

        verify(accountUserRepository).save(accountUser);
        verifyNoInteractions(guestUserRepository);
    }

    @Test
    public void testFindByUsernameGuest() {
        when(guestUserRepository.findByUsername(guestUser.getUsername())).thenReturn(Optional.of(guestUser));

        Optional<User> userOptional = allUserRepository.findByUsername(guestUser.getUsername());

        assertTrue(userOptional.isPresent());
        assertEquals(guestUser, userOptional.get());
    }

    @Test
    public void testFindByUsernameAccount() {
        when(accountUserRepository.findByUsername(accountUser.getUsername())).thenReturn(Optional.of(accountUser));

        Optional<User> userOptional = allUserRepository.findByUsername(accountUser.getUsername());

        assertTrue(userOptional.isPresent());
        assertEquals(accountUser, userOptional.get());
    }

    @Test
    public void testFindByUsernameUnknownUser() {
        when(guestUserRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(accountUserRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertFalse(allUserRepository.findByUsername("someUser").isPresent());
    }

}
