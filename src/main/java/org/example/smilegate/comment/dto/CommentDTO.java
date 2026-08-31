package org.example.smilegate.comment.dto;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import org.example.smilegate.comment.domain.Comment;
import org.example.smilegate.project.domain.Project;

import java.time.LocalDateTime;

public class CommentDTO {

    @Getter
    @Setter
    public static class CommentResponse {
        @Column(nullable = false)
        private String content;
        @Column(nullable = false)
        private String nickname;
        private LocalDateTime created_at;
        private LocalDateTime updated_at;
        private Long comment_id;

        public CommentResponse(Comment comment) {
            this.content = comment.getContent();
            this.nickname = comment.getNickname();
            this.created_at = LocalDateTime.now();
            this.comment_id=comment.getId();

        }


    }
    @Getter
    @Setter
    public static class CommentRequest {
        @Column(nullable = false)
        private String content;
        @Column(nullable = false)
        private String nickname;

    }
}
