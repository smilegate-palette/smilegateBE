package org.example.smilegate.project.domain;

import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlSchemaType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.smilegate.comment.domain.Comment;
import org.example.smilegate.project.dto.ProjectDTO;
import org.example.smilegate.user.domain.User;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Project extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false)
    String project_title;
    @Column(nullable = false)
    String program_name;
    @Column(nullable = false)
    Integer year;
    String region;
    @Column(nullable = false)
    List<String> participants;
    String description;
    @Enumerated(EnumType.STRING)
    ProjectCategory category;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    ProjectStatus status;
    String media_url;
    @Column(nullable = false)
    Integer view_count;
    String reject_reason;
    int like_count;

    // 정렬 기준
    @Enumerated(EnumType.STRING)
    Sort sort;
    Integer page; // 페이지 번호
    Integer limit; // 페이지당 개수

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
    public Project(ProjectDTO.ProjectRequest request) {
        this.project_title=request.getProject_title();
        this.category=request.getCategory();
        this.media_url=request.getMedia_url();
        this.description=request.getDescription();
        this.program_name=request.getProgram_name();
        this.participants=request.getParticipants();
        this.region=request.getRegion();
        this.status=request.getStatus();
        this.view_count=0;
        this.year=request.getYear();
        this.like_count=request.getLike_count();
    }

    public void Update(ProjectDTO.ProjectRequest request){
        this.project_title=request.getProject_title();
        this.category=request.getCategory();
        this.media_url=request.getMedia_url();
        this.description=request.getDescription();
        this.program_name=request.getProgram_name();
        this.participants=request.getParticipants();
        this.region=request.getRegion();
        this.status=request.getStatus();
        this.view_count=request.getView_count();
        this.year=request.getYear();
        this.like_count=request.getLike_count();
    }

}
