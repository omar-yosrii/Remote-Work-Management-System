package com.rwms.timer.strategy;

import com.rwms.timer.entity.WorkSession;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RunningStrategy implements TimerStrategy {

    @Override
    public WorkSession tick(WorkSession session) {
        if (session.getState() != WorkSession.SessionState.RUNNING) {
            return session;
        }

        session.setWorkedSeconds(session.getWorkedSeconds() + 1);
        long workedSeconds = session.getWorkedSeconds();

        // if workedSeconds == 14400 (4h exactly): set state=ON_BREAK, breakStartedAt=now
        if (workedSeconds == 14400 && !session.isBreakTaken()) {
            session.setState(WorkSession.SessionState.ON_BREAK);
            session.setBreakStartedAt(LocalDateTime.now());
        }

        // if workedSeconds == 28200 (8h - 10min): set submissionPageTriggered=true
        if (workedSeconds >= 28200) {
            session.setSubmissionPageTriggered(true);
        }

        // if workedSeconds >= 28800: set state=COMPLETED
        if (workedSeconds >= 28800) {
            session.setState(WorkSession.SessionState.COMPLETED);
        }

        return session;
    }
}
