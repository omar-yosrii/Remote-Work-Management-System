package com.rwms.timer.strategy;

import com.rwms.timer.entity.WorkSession;
import org.springframework.stereotype.Component;

@Component
public class TimerContext {

    private final RunningStrategy runningStrategy;
    private final BreakStrategy breakStrategy;

    public TimerContext(RunningStrategy runningStrategy, BreakStrategy breakStrategy) {
        this.runningStrategy = runningStrategy;
        this.breakStrategy = breakStrategy;
    }

    public TimerStrategy selectStrategy(WorkSession.SessionState state) {
        if (state == WorkSession.SessionState.RUNNING) {
            return runningStrategy;
        } else if (state == WorkSession.SessionState.ON_BREAK) {
            return breakStrategy;
        }
        return session -> session; // Default no-op for COMPLETED
    }

    public WorkSession tick(WorkSession session) {
        TimerStrategy strategy = selectStrategy(session.getState());
        return strategy.tick(session);
    }
}
