package com.rwms.timer.strategy;

import com.rwms.timer.entity.WorkSession;

public interface TimerStrategy {
    WorkSession tick(WorkSession session);
}
