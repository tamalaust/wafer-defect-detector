package com.iotml.wdd.predict;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PredictionScoredListener {

    private final PredictionBroadcaster broadcaster;

    public PredictionScoredListener(PredictionBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPredictionScored(PredictionEvent event) {
        broadcaster.broadcast(event);
    }
}
