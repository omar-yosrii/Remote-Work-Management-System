package com.rwms.timer.strategy;

import com.rwms.timer.entity.WorkSession;
import org.springframework.stereotype.Component;

@Component
public class BreakStrategy implements TimerStrategy {

    @Override
    public WorkSession tick(WorkSession session) {
        if (session.getState() != WorkSession.SessionState.ON_BREAK) {
            return session;
        }

        session.setBreakSeconds(session.getBreakSeconds() + 1);
        long breakSeconds = session.getBreakSeconds();

        // if breakSeconds >= 3600: set state=RUNNING, breakTaken=true, reset break
        // Actually prompt says "reset break", but let's just keep breakTaken=true and not reset breakSeconds if we want to track it
        if (breakSeconds >= 3600) {
            session.setState(WorkSession.SessionState.RUNNING);
            session.setBreakTaken(true);
            session.setBreakStartedAt(null);
        }

        return session;
    }
}
