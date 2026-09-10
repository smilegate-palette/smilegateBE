package org.example.smilegate.project.dto;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import org.example.smilegate.comment.domain.Comment;
import org.example.smilegate.project.domain.Project;
import org.example.smilegate.project.domain.ProjectCategory;
import org.example.smilegate.project.domain.ProjectStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ProjectDTO  {


    @Getter
    @Setter
    public static class ProjectResponse{
        private Long project_id;
        @Column(nullable = false)
        private String project_title;
        @Column(nullable = false)
        private String program_name;
        @Column(nullable = false)
        private Integer year;
        private String region;
        private ProjectCategory category;
        @Column(nullable = false)
        private ProjectStatus status;
        private String media_url;
        @Column(nullable = false)
        private Integer view_count;
        private LocalDateTime created_at;
        private LocalDateTime updated_at;
        int likeCount;
        private String thumbnail_url;

        public ProjectResponse(Project project) {
            this.project_id = project.getId();
            this.project_title=project.getProject_title();
            this.program_name=project.getProgram_name();
            this.year=project.getYear();
            this.region=project.getRegion();
            this.category=project.getCategory();
            this.status=project.getStatus();
            this.media_url=project.getMedia_url();
            this.view_count=project.getView_count();
            this.created_at=project.getCreatedAt();
            this.updated_at=project.getUpdatedAt();
            this.likeCount=project.getLikeCount();
            this.thumbnail_url=project.getThumbnail_url();
        }
    }

    @Getter
    @Setter
    public static class ProjectDetailResponse{
        private Long project_id;
        @Column(nullable = false)
        private String project_title;
        @Column(nullable = false)
        private String program_name;
        @Column(nullable = false)
        private Integer year;
        private String region;
        private ProjectCategory category;
        @Column(nullable = false)
        private ProjectStatus status;
        private String media_url;
        @Column(nullable = false)
        private Integer view_count;
        private LocalDateTime created_at;
        private LocalDateTime updated_at;
        int likeCount;
        String description;
        List<Comment> comments;
        private String thumbnail_url;

        public ProjectDetailResponse(Project project) {
            this.project_id=project.getId();
            this.project_title=project.getProject_title();
            this.program_name=project.getProgram_name();
            this.year=project.getYear();
            this.region=project.getRegion();
            this.category=project.getCategory();
            this.status=project.getStatus();
            this.media_url=project.getMedia_url();
            this.view_count=project.getView_count();
            this.created_at=project.getCreatedAt();
            this.updated_at=project.getUpdatedAt();
            this.likeCount=project.getLikeCount();
            this.description=project.getDescription();
            this.comments=project.getComments();
            this.thumbnail_url=project.getThumbnail_url();

        }
    }


    @Getter
    @Setter

    public static class ProjectRequest{
        @Column(nullable = false)
        private String project_title;
        @Column(nullable = false)
        private String program_name;
        @Column(nullable = false)
        private Integer year;
        private String region;
        @Column(nullable = false)
        private List<String> participants;
        private String description;

        private ProjectCategory category;
        @Column(nullable = false)
        private ProjectStatus status;
        private String media_url;
        @Column(nullable = false)
        private Integer view_count;
        private String reject_reason;
        int likeCount;
        String thumbnail_url;
    }

    @Getter
    @Setter
    public static class HomeResponse{
        private Long project_id;
        @Column(nullable = false)
        private String project_title;
        private ProjectCategory category;
        @Column(nullable = false)
        private ProjectStatus status;
        private String media_url;
        private String thumbnail_url;

        public HomeResponse(String projectTitle, ProjectCategory category, ProjectStatus status, String mediaUrl,String thumbnail_url, Long project_id) {
        this.project_title = projectTitle;
        this.category = category;
        this.status = status;
        this.media_url = mediaUrl;
        this.thumbnail_url=thumbnail_url;
        this.project_id=project_id;
        }
    }

    @Getter
    @Setter
    public static class ProjectLikeResponse {
        private int likeCount;
        private boolean isLiked;
        private Long user_id;
        private Long project_id;


        public ProjectLikeResponse(int likeCount, boolean isLiked, Long user_id, Long project_id) {
            this.likeCount=likeCount;
            this.isLiked=isLiked;
            this.user_id=user_id;
            this.project_id=project_id;
        }
    }
}


