package de.hexfieldsstudio.hexfieldsdominion.lobby.heartbeat;

import de.hexfieldsstudio.hexfieldsdominion.account.user.Role;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.game.player.Player;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HeartbeatHandlerTest {

    @InjectMocks
    private HeartbeatHandler heartbeatHandler;

    private static Field executorsMapField;
    private static Field executorListenersField;
    private static Field executorCompletableFutureField;

    private Map<Integer, HeartbeatHandler.HeartbeatExecutor> executorMap;

    private Player player;

    @BeforeAll
    public static void setup() throws NoSuchFieldException {
        executorsMapField = HeartbeatHandler.class.getDeclaredField("playerIdsExecutors");
        executorsMapField.setAccessible(true);
        executorListenersField = HeartbeatHandler.HeartbeatExecutor.class.getDeclaredField("listeners");
        executorListenersField.setAccessible(true);
        executorCompletableFutureField = HeartbeatHandler.HeartbeatExecutor.class.getDeclaredField("completableFuture");
        executorCompletableFutureField.setAccessible(true);
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setupEach() throws IllegalAccessException {
        User user = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .build();
        this.player = new Player(user, 0);

        executorMap = (Map<Integer, HeartbeatHandler.HeartbeatExecutor>) executorsMapField.get(heartbeatHandler);
    }

    @AfterAll
    public static void afterAll() {
        executorsMapField.setAccessible(false);
        executorListenersField.setAccessible(false);
        executorCompletableFutureField.setAccessible(false);
    }

    @Test
    public void testRegisterNoHeartbeatMapContainsId() throws IllegalAccessException {
        HeartbeatHandler.HeartbeatExecutor heartbeatExecutor = new HeartbeatHandler.HeartbeatExecutor(player.getId(), heartbeatHandler, () -> {});
        executorMap.put(player.getId(), heartbeatExecutor);

        NoHeartbeatListener listener = mock(NoHeartbeatListener.class);

        heartbeatHandler.registerNoHeartbeat(player, listener);

        assertTrue(executorMap.containsKey(player.getId()));
        assertEquals(heartbeatExecutor, executorMap.get(player.getId()));
        assertTrue(this.getListeners(heartbeatExecutor).contains(listener));
    }

    @Test
    public void testRegisterNoHeartbeatMapDoesNotContainId() throws IllegalAccessException {
        NoHeartbeatListener listener = mock(NoHeartbeatListener.class);

        heartbeatHandler.registerNoHeartbeat(player, listener);

        assertFalse(executorMap.containsKey(player.getId()));
    }

    @Test
    public void testResetTimerMapContainsId() throws IllegalAccessException {
        HeartbeatHandler.HeartbeatExecutor heartbeatExecutor = new HeartbeatHandler.HeartbeatExecutor(player.getId(), heartbeatHandler, () -> {});
        executorMap.put(player.getId(), heartbeatExecutor);
        CompletableFuture<Void> completableFutureBefore = this.getCompletableFuture(heartbeatExecutor);

        heartbeatHandler.resetTimer(player.getId());

        assertTrue(executorMap.containsKey(player.getId()));
        assertEquals(heartbeatExecutor, executorMap.get(player.getId()));
        assertNotEquals(completableFutureBefore, this.getCompletableFuture(heartbeatExecutor));
    }

    @Test
    public void testResetTimerMapDoesNotContainId() throws IllegalAccessException {
        heartbeatHandler.resetTimer(player.getId());

        assertTrue(executorMap.containsKey(player.getId()));
        assertNotNull(this.getCompletableFuture(executorMap.get(player.getId())));
    }

    @SuppressWarnings("unchecked")
    private List<NoHeartbeatListener> getListeners(HeartbeatHandler.HeartbeatExecutor heartbeatExecutor) throws IllegalAccessException {
        return (List<NoHeartbeatListener>) executorListenersField.get(heartbeatExecutor);
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<Void> getCompletableFuture(HeartbeatHandler.HeartbeatExecutor heartbeatExecutor) throws IllegalAccessException {
        return (CompletableFuture<Void>) executorCompletableFutureField.get(heartbeatExecutor);
    }

}
