package com.rwms.task.repository;

import com.rwms.task.entity.Subtask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubtaskRepository extends JpaRepository<Subtask, Long> {

    List<Subtask> findByTaskId(Long taskId);

    long countByTaskId(Long taskId);

    long countByTaskIdAndApprovedByAdminTrue(Long taskId);
}
