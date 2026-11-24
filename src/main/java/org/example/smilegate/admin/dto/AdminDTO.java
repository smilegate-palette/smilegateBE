package org.example.smilegate.admin.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.smilegate.comment.domain.Comment;
import org.example.smilegate.project.domain.Project;
import org.example.smilegate.project.domain.ProjectCategory;
import org.example.smilegate.project.domain.ProjectStatus;
import org.example.smilegate.project.domain.SectionType;
import org.example.smilegate.user.domain.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AdminDTO {

    @Getter
    @Setter
    public static class AdminStatsDto {
        private int pendingApproval;
        private int totalProjects;
        private int todayComments;
        private int totalStories;

        public AdminStatsDto(int pendingApprovalCount,int totalProjectsCount,int todayCommentsCount,int totalStoriesCount){
            this.pendingApproval = pendingApprovalCount;
            this.totalProjects = totalProjectsCount;
            this.todayComments = todayCommentsCount;
            this.totalStories = totalStoriesCount;
        }
    }

    @Getter
    @Setter
    public static class ProjectListItemDto {
        private Long projectId;
        private String projectTitle;
        private String username;
        private LocalDateTime createdDate;
        private ProjectCategory category;
        private ProjectStatus status;
        private String description;
        private String media_url;
        private String thumbnail_url;
        private String organization;
        private String team;
        private List<String> participants;

        public ProjectListItemDto(Project project){
            projectId=project.getId();
            projectTitle=project.getProject_title();
            username= Optional.ofNullable(project.getUser())
                    .map(User::getUsername)
                    .orElse("unknown");
            createdDate=project.getCreatedAt();
            category=project.getCategory();
            status=project.getStatus();
            description=project.getDescription();
            media_url=project.getMedia_url();
            thumbnail_url=project.getThumbnail_url();
            organization=project.getOrganization();
            team=project.getTeam();
            participants=project.getParticipants();
        }
    }

    @Getter
    @Setter
    public static class ProjectDetailDto {
        private Long project_id;
        private String project_title;
        private String username;
        private LocalDateTime createdDate;
        private ProjectCategory category;
        private ProjectStatus status;
        private String description;
        private String media_url;
        private List<String> participants;
        private String organization;
        private String thumbnail_url;
        private String team;

        public ProjectDetailDto(Project project){
            project_id=project.getId();
            project_title=project.getProject_title();
            username=Optional.ofNullable(project.getUser())
                    .map(User::getUsername)
                    .orElse("unknown");
            createdDate=project.getCreatedAt();
            category=project.getCategory();
            status=project.getStatus();
            description=project.getDescription();
            media_url=project.getMedia_url();
            participants=project.getParticipants();
            organization=project.getOrganization();
            thumbnail_url=project.getThumbnail_url();
            team=project.getTeam();
        }
    }

    @Getter
    @Setter
    public static class CommentDto {
        private Long comment_id;
        private String content;
        private String username;
        private String project_title;
        private LocalDateTime createdDate;

        public CommentDto(Comment comment){
            comment_id=comment.getId();
            content=comment.getContent();
            username=Optional.ofNullable(comment.getUser())
                    .map(User::getUsername)
                    .orElse("unknown");
            project_title=comment.getProject().getProject_title();
            createdDate=comment.getCreatedAt();
        }
    }

    @Getter
    @Setter
    public static class CurationSectionDto {
        private Long sectionId;
        private String title;
        private Integer displayOrder;
        private SectionType sectionType;
        private boolean active;
        private List<CurationSectionProjectDto> sectionProjects;
    }

    @Getter
    @Setter
    public static class CurationSectionProjectDto {
        private Long projectId;
        private String projectTitle;
        private Integer displayOrder;
        private String mediaUrl;
        private String thumbnailUrl;
        private ProjectStatus status;
    }

    @Getter
    @Setter
    public static class HomeBannerDto {
        private Long bannerId;
        private String imageUrl;
        private Integer displayOrder;
        private boolean active;
        private org.example.smilegate.project.domain.BannerPage bannerPage;
    }

    @Getter
    @Setter
    public static class HomePageDto {
        private List<HomeBannerDto> banners;
        private List<CurationSectionDto> curationSections;
    }

    @Getter
    @Setter
    public static class ProjectRejectRequestDto {
        String reject_reason;
    }

    @Getter
    public static class AdminResponse {
        private AdminStatsDto stats;
        private List<ProjectListItemDto> pendingProjects;
        private List<CommentDto> recentComments;
        private List<CurationSectionDto> curationSections;
        private List<HomeBannerDto> banners;

        public AdminResponse(AdminStatsDto statsDto,
                             List<ProjectListItemDto> projectListItemDtos,
                             List<CommentDto> commentDtoList,
                             List<CurationSectionDto> curationSectionDtos,
                             List<HomeBannerDto> bannerDtos) {
            this.stats = statsDto;
            this.pendingProjects = projectListItemDtos;
            this.recentComments = commentDtoList;
            this.curationSections = curationSectionDtos;
            this.banners = bannerDtos;
        }
    }

    @Getter
    @Setter
    public static class CurationSaveRequest {
        private Long section_id;
        private String title;
        private Integer displayOrder;
        private SectionType sectionType;
        private boolean active = true;
        private List<Long> projectIds;

        public CurationSaveRequest() {}

        public CurationSaveRequest(Long section_id, List<Long> projectIds){
            this.section_id = section_id;
            this.projectIds = projectIds;
        }
    }

    @Getter
    @Setter
    public static class HomeBannerSaveRequest {
        private Long bannerId;
        private String imageUrl;
        private Integer displayOrder;
        private boolean active = true;
        private org.example.smilegate.project.domain.BannerPage bannerPage;
    }
}