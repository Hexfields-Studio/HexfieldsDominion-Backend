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
class AllUserRepositoryTest {

    @InjectMocks
    private AllUserRepository allUserRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @Mock
    private GuestUserRepository guestUserRepository;

    private User guestUser;
    private User accountUser;

    @BeforeEach
    void setupEach() {
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
    void testSaveGuest() {
        allUserRepository.save(guestUser);

        verify(guestUserRepository).save(guestUser);
        verifyNoInteractions(accountUserRepository);
    }

    @Test
    void testSaveAccount() {
        allUserRepository.save(accountUser);

        verify(accountUserRepository).save(accountUser);
        verifyNoInteractions(guestUserRepository);
    }

    @Test
    void testFindByUsernameGuest() {
        when(guestUserRepository.findByUsername(guestUser.getUsername())).thenReturn(Optional.of(guestUser));

        Optional<User> userOptional = allUserRepository.findByUsername(guestUser.getUsername());

        assertTrue(userOptional.isPresent());
        assertEquals(guestUser, userOptional.get());
    }

    @Test
    void testFindByUsernameAccount() {
        when(accountUserRepository.findByUsername(accountUser.getUsername())).thenReturn(Optional.of(accountUser));

        Optional<User> userOptional = allUserRepository.findByUsername(accountUser.getUsername());

        assertTrue(userOptional.isPresent());
        assertEquals(accountUser, userOptional.get());
    }

    @Test
    void testFindByUsernameIgnoreCaseGuest() {
        when(guestUserRepository.findByUsernameIgnoreCase(guestUser.getUsername())).thenReturn(Optional.of(guestUser));

        Optional<User> userOptional = allUserRepository.findByUsernameIgnoreCase(guestUser.getUsername());

        assertTrue(userOptional.isPresent());
        assertEquals(guestUser, userOptional.get());
    }

    @Test
    void testFindByUsernameIgnoreCaseAccount() {
        when(accountUserRepository.findByUsernameIgnoreCase(accountUser.getUsername())).thenReturn(Optional.of(accountUser));

        Optional<User> userOptional = allUserRepository.findByUsernameIgnoreCase(accountUser.getUsername());

        assertTrue(userOptional.isPresent());
        assertEquals(accountUser, userOptional.get());
    }

    @Test
    void testFindByUsernameUnknownUser() {
        when(guestUserRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(accountUserRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertFalse(allUserRepository.findByUsername("someUser").isPresent());
    }

    @Test
    public void testFindByUsernameIgnoreCaseUnknownUser() {
        assertFalse(allUserRepository.findByUsername("someUser").isPresent());
    }

    @Test
    public void testDeleteAll() {
        allUserRepository.save(guestUser);
        allUserRepository.save(accountUser);

        allUserRepository.deleteAll();

        verify(guestUserRepository, times(1)).deleteAll();
        verify(accountUserRepository, times(1)).deleteAll();
    }

}
