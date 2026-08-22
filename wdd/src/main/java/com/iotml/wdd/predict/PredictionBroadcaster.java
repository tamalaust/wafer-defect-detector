package com.iotml.wdd.predict;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class PredictionBroadcaster {

    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        emitters.add(emitter);

        Runnable remove = () -> emitters.remove(emitter);
        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        emitter.onError(ex -> remove.run());

        return emitter;
    }

    public void broadcast(PredictionEvent event) {
        List<SseEmitter> dead = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("prediction")
                        .data(event));
            } catch (IOException | IllegalStateException ex) {
                dead.add(emitter);
            }
        }

        emitters.removeAll(dead);
    }
}
