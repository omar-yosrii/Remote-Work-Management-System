package com.rwms.submission.repository;

import com.rwms.submission.entity.SubmissionComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionCommentRepository extends JpaRepository<SubmissionComment, Long> {

    List<SubmissionComment> findBySubmissionIdOrderByCreatedAtAsc(Long submissionId);
    
    List<SubmissionComment> findBySubmissionIdAndIsPrivateNoteFalseOrderByCreatedAtAsc(Long submissionId);
}
