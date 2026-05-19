package com.rwms.timer.controller;

import com.rwms.timer.dto.WorkSessionResponse;
import com.rwms.timer.service.TimerService;
import com.rwms.user.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/timer")
public class TimerController {

    private final TimerService timerService;

    public TimerController(TimerService timerService) {
        this.timerService = timerService;
    }

    @PostMapping("/start/{taskId}")
    public ResponseEntity<WorkSessionResponse> startSession(@PathVariable Long taskId,
                                                            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(timerService.startSession(taskId, user.getEmail()));
    }

    @GetMapping("/active")
    public ResponseEntity<WorkSessionResponse> getActiveSession(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(timerService.getActiveSession(user.getEmail()));
    }

    @PostMapping("/sync")
    public ResponseEntity<WorkSessionResponse> syncTick(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(timerService.syncTick(user.getEmail()));
    }

    @PostMapping("/end")
    public ResponseEntity<Void> endSession(@AuthenticationPrincipal User user) {
        timerService.endSession(user.getEmail());
        return ResponseEntity.ok().build();
    }
}
