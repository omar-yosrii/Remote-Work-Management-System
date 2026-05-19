package com.rwms.timer.service;

import com.rwms.common.exception.ResourceNotFoundException;
import com.rwms.task.entity.Task;
import com.rwms.task.repository.TaskRepository;
import com.rwms.timer.dto.WorkSessionResponse;
import com.rwms.timer.entity.WorkSession;
import com.rwms.timer.repository.WorkSessionRepository;
import com.rwms.timer.strategy.TimerContext;
import com.rwms.user.entity.User;
import com.rwms.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TimerService {

    private final WorkSessionRepository workSessionRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TimerContext timerContext;

    public TimerService(WorkSessionRepository workSessionRepository, TaskRepository taskRepository,
                        UserRepository userRepository, TimerContext timerContext) {
        this.workSessionRepository = workSessionRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.timerContext = timerContext;
    }

    private WorkSessionResponse toResponse(WorkSession session) {
        boolean breakWarning = false;
        boolean breakEndingWarning = false;

        // if workedSeconds == 14580 (4h - 3min): trigger GREEN break warning
        // Actually, let's trigger it if workedSeconds >= 14220 (4h - 3m) and workedSeconds < 14400
        if (session.getWorkedSeconds() >= 14220 && session.getWorkedSeconds() < 14400 && !session.isBreakTaken()) {
            breakWarning = true;
        }

        // if breakSeconds == 3420 (1h - 3min): trigger RED break-ending warning
        // Actually let's trigger if breakSeconds >= 3420 and breakSeconds < 3600
        if (session.getBreakSeconds() >= 3420 && session.getBreakSeconds() < 3600 && session.getState() == WorkSession.SessionState.ON_BREAK) {
            breakEndingWarning = true;
        }

        return WorkSessionResponse.builder()
                .sessionState(session.getState().name())
                .workedSeconds(session.getWorkedSeconds())
                .breakSeconds(session.getBreakSeconds())
                .breakTaken(session.isBreakTaken())
                .breakWarning(breakWarning)
                .breakEndingWarning(breakEndingWarning)
                .triggerSubmitPage(session.isSubmissionPageTriggered())
                .build();
    }

    public WorkSessionResponse startSession(Long taskId, String employeeEmail) {
        User employee = userRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (workSessionRepository.findActiveByEmployeeId(employee.getId()).isPresent()) {
            throw new IllegalStateException("An active session already exists for this employee");
        }

        WorkSession session = WorkSession.builder()
                .employee(employee)
                .task(task)
                .sessionStartedAt(LocalDateTime.now())
                .lastSyncedAt(LocalDateTime.now())
                .state(WorkSession.SessionState.RUNNING)
                .build();

        return toResponse(workSessionRepository.save(session));
    }

    public WorkSessionResponse getActiveSession(String employeeEmail) {
        User employee = userRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        WorkSession session = workSessionRepository.findActiveByEmployeeId(employee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No active session found"));

        return toResponse(session);
    }

    public WorkSessionResponse syncTick(String employeeEmail) {
        User employee = userRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        WorkSession session = workSessionRepository.findActiveByEmployeeId(employee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No active session found"));

        // Here we simulate the tick based on elapsed time since last sync, or we could just tick by fixed amount
        // The prompt says "called by frontend every second via polling or scheduled - applies TimerContext.tick() and saves"
        // Since frontend might send it every ~5 seconds, we might want to catch up based on time elapsed.
        // For simplicity as requested, we'll just apply tick N times based on seconds elapsed since last sync.
        LocalDateTime now = LocalDateTime.now();
        long secondsElapsed = java.time.Duration.between(session.getLastSyncedAt(), now).getSeconds();
        
        // Cap elapsed seconds to avoid huge jumps if frontend was closed
        if (secondsElapsed > 0 && secondsElapsed < 3600) { // arbitrary cap to avoid hanging
            for (int i = 0; i < secondsElapsed; i++) {
                session = timerContext.tick(session);
                if (session.getState() == WorkSession.SessionState.COMPLETED) {
                    break;
                }
            }
        }
        
        session.setLastSyncedAt(now);
        return toResponse(workSessionRepository.save(session));
    }

    public void endSession(String employeeEmail) {
        User employee = userRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        WorkSession session = workSessionRepository.findActiveByEmployeeId(employee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No active session found"));

        session.setState(WorkSession.SessionState.COMPLETED);
        workSessionRepository.save(session);
    }
}
