package de.hexfieldsstudio.hexfieldsdominion;

import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class SseSender <T> {

    protected final Map<T, Map<String, SseEmitter>> groupsEmitters = new ConcurrentHashMap<>();

    protected SseEmitter createEmitter(String username, T group) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        groupsEmitters.computeIfAbsent(group, k -> new ConcurrentHashMap<>()).put(username, emitter);

        emitter.onCompletion(() -> unsubscribe(group, username));
        emitter.onError((throwable) -> unsubscribe(group, username));
        emitter.onTimeout(() -> unsubscribe(group, username));

        return emitter;
    }

    protected void sendEvent(Map<String, SseEmitter> emitters, String name, Object data, T group) {
        if (emitters == null) {
            return;
        }

        for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
            try {
                entry.getValue().send(SseEmitter.event().name(name).data(data));
            } catch (IOException e) {
                unsubscribe(group, entry.getKey());
            }
        }
    }

    protected Map<String, SseEmitter> allEmitters(T group) {
        return groupsEmitters.get(group);
    }

    protected Map<String, SseEmitter> allEmittersExcept(T group, User user) {
        return allEmitters(group).entrySet().stream()
                .filter(entry -> !entry.getKey().equals(user.getUsername()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    protected Map<String, SseEmitter> emittersOfOnly(T group, String username) {
        return allEmitters(group).entrySet().stream()
                .filter(entry -> entry.getKey().equals(username))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void unsubscribe(T group, String username) {
        Map<String, SseEmitter> emitters = groupsEmitters.get(group);
        if (emitters == null) {
            return;
        }
        emitters.remove(username);

        onUnsubscribe(group, username);

        if (emitters.isEmpty()) {
            groupsEmitters.remove(group);
        }
    }

    public abstract SseEmitter subscribe(T group, String username);

    protected void onUnsubscribe(T group, String username) {}

}
