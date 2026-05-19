package com.rwms.submission.repository;

import com.rwms.submission.entity.TaskSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission, Long> {

    List<TaskSubmission> findByEmployeeId(Long employeeId);

    Optional<TaskSubmission> findByTaskId(Long taskId);

    @Query("SELECT ts FROM TaskSubmission ts WHERE ts.task.project.id = :projectId AND ts.reviewStatus = 'PENDING'")
    List<TaskSubmission> findPendingByProjectId(Long projectId);
}
