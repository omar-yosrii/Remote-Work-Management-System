package com.rwms.project.repository;

import com.rwms.project.entity.Project;
import com.rwms.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByDepartment(String department);

    List<Project> findByTeamLeaderId(Long userId);

    List<Project> findByContributorsContaining(User user);
}
