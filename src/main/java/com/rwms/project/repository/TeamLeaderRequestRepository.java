package com.rwms.project.repository;

import com.rwms.project.entity.TeamLeaderRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamLeaderRequestRepository extends JpaRepository<TeamLeaderRequest, Long> {

    List<TeamLeaderRequest> findByStatus(TeamLeaderRequest.RequestStatus status);
}
