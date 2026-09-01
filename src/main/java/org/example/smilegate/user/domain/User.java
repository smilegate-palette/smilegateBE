package org.example.smilegate.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.smilegate.comment.domain.Comment;
import org.example.smilegate.project.domain.Project;
import org.example.smilegate.user.dto.UserDTO;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false)
    String username;
    @Column(nullable = false)
    String email;
    @Column(nullable = false)
    String password;
    @Enumerated(EnumType.STRING)
    UserRole role;
    String provider; // "naver", "google", "kakao"
    String providerId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> projects = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();



    public User(UserDTO.UserSignupRequest request){
        this.username=request.getUsername();
        this.email=request.getEmail();
        this.password=request.getPassword();
        this.role=UserRole.USER;
    }
}
