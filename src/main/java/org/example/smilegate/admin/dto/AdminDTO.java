package org.example.smilegate.admin.dto;

import jdk.jfr.Category;
import lombok.Getter;
import lombok.Setter;
import org.example.smilegate.comment.domain.Comment;
import org.example.smilegate.project.domain.Project;
import org.example.smilegate.project.domain.ProjectCategory;
import org.example.smilegate.project.domain.ProjectStatus;
import org.example.smilegate.user.domain.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AdminDTO {

    @Getter
    @Setter
    public static class AdminStatsDto {
        private int pendingApproval;   // 승인대기 건수
        private int totalProjects;     // 총 프로젝트
        private int todayComments;     // 오늘 댓글
        private int totalStories;      // 스토리 수

        public AdminStatsDto(int pendingApprovalCount,int totalProjectsCount,int todayCommentsCount,int totalStoriesCount){
           this.pendingApproval = pendingApprovalCount;
            this.totalProjects = totalProjectsCount;
            this.todayComments = todayCommentsCount;
            this.totalStories = totalStoriesCount;
        }
    }


    // 승인 대기 프로젝트
    @Getter
    @Setter
    public static class ProjectListItemDto {
        private Long projectId;
        private String projectTitle;
        private String username;           // 제출자
        private LocalDateTime createdDate;
        private ProjectCategory category;
        private ProjectStatus status;

        public ProjectListItemDto(Project project){
            projectId=project.getId();
            projectTitle=this.getProjectTitle();
            username= Optional.ofNullable(project.getUser())
                    .map(User::getUsername)
                    .orElse("unknown");
            createdDate=project.getCreatedAt();
            category=project.getCategory();
            status=project.getStatus();
        }
    }


    // 프로젝트 상세 수정
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
            participants = project.getParticipants();
            organization = project.getOrganization();
            thumbnail_url =project.getThumbnail_url();
        }

    }


    // 댓글
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


    // 큐레이션 섹션
    @Getter
    @Setter
    public static class CurationSectionDto {
        private Long sectionId;
        private String title;
        private Integer displayOrder;
        private List<CurationSectionProjectDto> sectionProjects;
    }

    @Getter
    @Setter
    public static class CurationSectionProjectDto {
        private Long projectId;
        private String projectTitle;     // 화면 표시용
        private Integer displayOrder;  // 섹션 내 순서
        private String mediaUrl;
        private ProjectStatus status;
    }


    @Getter
    @Setter
    public static class ProjectRejectRequestDto {
        String reject_reason;
    }


    // 최종
    @Getter
    public static class AdminResponse {
        private AdminStatsDto stats;
        private List<ProjectListItemDto> pendingProjects;
        private List<CommentDto> recentComments;
        private List<CurationSectionDto> curationSections;

        public AdminResponse(AdminStatsDto statsDto,
                             List<ProjectListItemDto> projectListItemDtos,
                             List<CommentDto> commentDtoList,
                             List<CurationSectionDto> curationSectionDtos) {
            this.stats = statsDto;
            this.pendingProjects = projectListItemDtos;
            this.recentComments = commentDtoList;
            this.curationSections = curationSectionDtos;
        }
    }

    @Getter
    @Setter
    public static class CurationSaveRequest {
        private Long section_id;
        private List<Long> projectIds;
        public CurationSaveRequest(Long section_id, List<Long> projectIds){
            this.section_id=section_id;
            this.projectIds=projectIds;
        }
    }



}
