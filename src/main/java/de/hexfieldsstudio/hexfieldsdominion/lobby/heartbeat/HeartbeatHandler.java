package de.hexfieldsstudio.hexfieldsdominion.lobby.heartbeat;

import de.hexfieldsstudio.hexfieldsdominion.game.player.Player;
import de.hexfieldsstudio.hexfieldsdominion.lobby.Lobby;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.concurrent.*;

@RequiredArgsConstructor
public class HeartbeatHandler {

    private final Lobby lobby;
    private final long heartbeatCheckIntervalSeconds;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Map<Integer, HeartbeatExecutor> playerIdsExecutors = new HashMap<>();

    public void registerNoHeartbeat(Player player, NoHeartbeatListener listener) {
        if (!playerIdsExecutors.containsKey(player.getId())) {
            return;
        }
        playerIdsExecutors.get(player.getId()).addListener(listener);
    }

    public void resetTimer(int playerId) {
        if (playerIdsExecutors.containsKey(playerId)) {
            playerIdsExecutors.get(playerId).reschedule();
            return;
        }

        Runnable cleanupAction = () -> {
            playerIdsExecutors.remove(playerId);
        };
        playerIdsExecutors.put(playerId, new HeartbeatExecutor(playerId, this, cleanupAction));
    }

    static class HeartbeatExecutor {

        private final List<NoHeartbeatListener> listeners = new ArrayList<>();
        private final int playerId;
        private final HeartbeatHandler heartbeatHandler;
        private final Runnable cleanupAction;
        private CompletableFuture<Void> completableFuture;

        HeartbeatExecutor(int playerId, HeartbeatHandler heartbeatHandler, Runnable cleanupAction) {
            this.playerId = playerId;
            this.heartbeatHandler = heartbeatHandler;
            this.cleanupAction = cleanupAction;
            this.reschedule();
        }

        private void addListener(NoHeartbeatListener listener) {
            this.listeners.add(listener);
        }

        void reschedule() {
            if (completableFuture != null) {
                completableFuture.cancel(false);
            }

            completableFuture = CompletableFuture.runAsync(() -> {
                for (NoHeartbeatListener listener : this.listeners) {
                    listener.onNoHeartbeat(heartbeatHandler.lobby, playerId);
                }
                this.cleanupAction.run();
            }, CompletableFuture.delayedExecutor(heartbeatHandler.heartbeatCheckIntervalSeconds, TimeUnit.SECONDS, heartbeatHandler.executorService));
        }
    }

}