package org.example.smilegate.comment.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.smilegate.project.domain.BaseTimeEntity;
import org.example.smilegate.project.domain.Project;
import org.example.smilegate.user.domain.User;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Comment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nickname;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

}
