package com.rwms.timer.repository;

import com.rwms.timer.entity.WorkSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkSessionRepository extends JpaRepository<WorkSession, Long> {

    Optional<WorkSession> findByEmployeeIdAndState(Long empId, WorkSession.SessionState state);

    @Query("SELECT ws FROM WorkSession ws WHERE ws.employee.id = :empId AND ws.state IN ('RUNNING', 'ON_BREAK')")
    Optional<WorkSession> findActiveByEmployeeId(Long empId);
}
