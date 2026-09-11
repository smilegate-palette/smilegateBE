package org.example.smilegate.admin.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.smilegate.admin.dto.AdminDTO;
import org.example.smilegate.comment.domain.Comment;
import org.example.smilegate.comment.dto.CommentDTO;
import org.example.smilegate.comment.repository.CommentRepository;
import org.example.smilegate.project.domain.CurationSection;
import org.example.smilegate.project.domain.CurationSectionProject;
import org.example.smilegate.project.domain.Project;
import org.example.smilegate.project.domain.ProjectStatus;
import org.example.smilegate.project.dto.ProjectDTO;
import org.example.smilegate.project.repository.CurationSectionRepository;
import org.example.smilegate.project.repository.ProjectRepository;
import org.example.smilegate.story.repository.StoryRepository;
import org.example.smilegate.user.domain.User;
import org.example.smilegate.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class AdminService {



    private final ProjectRepository projectRepository;
    private final CommentRepository commentRepository;
    private final CurationSectionRepository curationSectionRepository;
    private final UserRepository userRepository;
    private final StoryRepository storyRepository;


    //관리자 페이지 조회
    public AdminDTO.AdminResponse Adminpage( Long user_id){
        int totalStories = Math.toIntExact(storyRepository.count());
        User user = userRepository.findById(user_id).orElseThrow(()-> new RuntimeException("로그인을 해주세요."));
        List<Project> PendingProjects = projectRepository.findByStatus(ProjectStatus.PENDING);
        List<Project> AllProjects = projectRepository.findAll();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay   = LocalDate.now().plusDays(1).atStartOfDay();
        List<Comment> TodayComments = commentRepository.findByCreatedAtBetween(startOfDay,endOfDay);

        AdminDTO.AdminStatsDto statsDto = new AdminDTO.AdminStatsDto(PendingProjects.size(),AllProjects.size(), TodayComments.size(),totalStories);

        List<AdminDTO.ProjectListItemDto> projectListItemDtos = PendingProjects.stream()
                .map(project -> new AdminDTO.ProjectListItemDto(project))
                .collect(Collectors.toList());


        List<Comment> recentComments = commentRepository.findTop2ByOrderByCreatedAtDesc();
        List<AdminDTO.CommentDto> commentDtoList = recentComments.stream()
                .map(comment -> new AdminDTO.CommentDto(comment))
                .collect(Collectors.toList());

        List<AdminDTO.CurationSectionDto> curationSectionDtos = getCurationSections();

        AdminDTO.AdminResponse adminResponse = new AdminDTO.AdminResponse(statsDto,projectListItemDtos,commentDtoList,curationSectionDtos);

        return adminResponse;
    }

    // 프로젝트 상세 조회
    public AdminDTO.ProjectDetailDto AdminProjectDetail(Long project_id, Long user_id){
        User user = userRepository.findById(user_id).orElseThrow(()-> new RuntimeException("로그인을 해주세요."));
        Project project = projectRepository.findById(project_id).orElseThrow(()-> new RuntimeException("프로젝트를 찾을 수 없습니다."));
        AdminDTO.ProjectDetailDto projectDetailDto = new AdminDTO.ProjectDetailDto(project);
        return projectDetailDto;
    }

    // 프로젝트 승인
    public boolean ApproveProject(Long project_id, Long user_id){
        try {
            User user = userRepository.findById(user_id).orElseThrow(() -> new RuntimeException("로그인을 해주세요."));
            Project project = projectRepository.findById(project_id).orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));
            project.setStatus(ProjectStatus.APPROVED);
            projectRepository.save(project);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    //프로젝트 반려
    public boolean RejectProject(Long project_id, Long user_id,String RejectReason){
        try {
            User user = userRepository.findById(user_id).orElseThrow(() -> new RuntimeException("로그인을 해주세요."));
            Project project = projectRepository.findById(project_id).orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));
            project.setStatus(ProjectStatus.REJECTED);
            project.setReject_reason(RejectReason);
            projectRepository.save(project);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    // 큐레이션 조회
    public List<AdminDTO.CurationSectionDto> getCurationSections() {
        return curationSectionRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private AdminDTO.CurationSectionDto toDto(CurationSection section) {
        List<AdminDTO.CurationSectionProjectDto> dtos;

        if (section.getLinks() != null && !section.getLinks().isEmpty()) {
             //  관리자가 선택한 프로젝트
            dtos = section.getLinks().stream()
                    .sorted(Comparator.comparingInt(CurationSectionProject::getDisplayOrder))
                    .map(link -> {
                        AdminDTO.CurationSectionProjectDto dto = new AdminDTO.CurationSectionProjectDto();
                        dto.setProjectId(link.getProject().getId());
                        dto.setProjectTitle(link.getProject().getProject_title());
                        dto.setMediaUrl(link.getProject().getMedia_url());
                        dto.setDisplayOrder(link.getDisplayOrder());
                        dto.setStatus(link.getProject().getStatus());
                        return dto;
                    })
                    .collect(Collectors.toList());
        } else {
            //  자동 fallback: 좋아요 높은 순 3개
            List<Project> top3 = projectRepository.findTop3ByOrderByLikeCountDesc();
            dtos = top3.stream()
                    .map(project -> {
                        AdminDTO.CurationSectionProjectDto dto = new AdminDTO.CurationSectionProjectDto();
                        dto.setProjectId(project.getId());
                        dto.setProjectTitle(project.getProject_title());
                        dto.setMediaUrl(project.getMedia_url());
                        dto.setDisplayOrder(-1);  // 자동 선별이라 순서 의미 없음
                        return dto;
                    })
                    .collect(Collectors.toList());
        }

        AdminDTO.CurationSectionDto sectionDto = new AdminDTO.CurationSectionDto();
        sectionDto.setSectionId(section.getSectionId());
        sectionDto.setTitle(section.getTitle());
        sectionDto.setDisplayOrder(section.getDisplayOrder());
        sectionDto.setSectionProjects(dtos);
        return sectionDto;
    }

        //큐레이션 저장
        public boolean saveCuration(AdminDTO.CurationSaveRequest request) {
            if (request == null || request.getSection_id() == null) {
                throw new IllegalArgumentException("섹션 ID가 필요합니다.");
            }

            CurationSection section = curationSectionRepository.findById(request.getSection_id())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 큐레이션 섹션입니다."));

            // null이거나 빈 리스트면 기존 링크만 제거
            if (request.getProjectIds() == null || request.getProjectIds().isEmpty()) {
                section.getLinks().clear();
                curationSectionRepository.save(section);
                return true;
            }

            Set<Long> uniqueIds = new LinkedHashSet<>(request.getProjectIds());
            if (uniqueIds.size() > 3) {
                throw new IllegalArgumentException("큐레이션 프로젝트는 최대 3개까지 선택할 수 있습니다.");
            }

            List<Project> projects = projectRepository.findAllById(uniqueIds);
            if (projects.size() != uniqueIds.size()) {
                throw new IllegalArgumentException("존재하지 않는 프로젝트 ID가 포함되어 있습니다.");
            }

            //  기존 링크 전체 제거
            section.getLinks().clear();

            // 새 링크 생성 (request 순서 = displayOrder)
            for (int i = 0; i < projects.size(); i++) {
                CurationSectionProject link = new CurationSectionProject();
                link.setSection(section);
                link.setProject(projects.get(i));
                link.setDisplayOrder(i);
                section.getLinks().add(link);
            }

            curationSectionRepository.save(section);
            return true;
        }




}
