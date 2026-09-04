package org.example.smilegate.comment.repository;

import org.example.smilegate.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByProjectId(Long project_id) ;
    List<Comment> findTop2ByOrderByCreatedAtDesc();

    List<Comment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);;
}
