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

import static org.example.smilegate.project.domain.ProjectStatus.APPROVED;

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
    public AdminDTO.AdminResponse Adminpage(Long user_id) {
        userRepository.findById(user_id)
                .orElseThrow(() -> new RuntimeException("로그인을 해주세요."));

        int totalStories = Math.toIntExact(storyRepository.count());
        List<Project> pendingProjects = projectRepository.findByStatus(ProjectStatus.PENDING);
        int totalProjects = Math.toIntExact(projectRepository.count());

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();
        int todayCommentCount = Math.toIntExact(commentRepository.countByCreatedAtBetween(startOfDay, endOfDay));

        AdminDTO.AdminStatsDto statsDto = new AdminDTO.AdminStatsDto(
                pendingProjects.size(), totalProjects, todayCommentCount, totalStories);

        List<AdminDTO.ProjectListItemDto> projectListItemDtos = pendingProjects.stream()
                .map(AdminDTO.ProjectListItemDto::new)
                .collect(Collectors.toList());

        List<AdminDTO.CommentDto> commentDtoList = commentRepository.findTop2ByOrderByCreatedAtDesc().stream()
                .map(AdminDTO.CommentDto::new)
                .collect(Collectors.toList());

        // 관리자용: 승인 프로젝트 전체 + 선택 여부 (fallback 없음)
        List<AdminDTO.CurationSectionDto> curationSectionDtos = getAdminCurationSections();

        return new AdminDTO.AdminResponse(statsDto, projectListItemDtos, commentDtoList, curationSectionDtos);
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

            User user = userRepository.findById(user_id).orElseThrow(() -> new RuntimeException("로그인을 해주세요."));
            Project project = projectRepository.findById(project_id)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));
            project.setStatus(APPROVED);
            projectRepository.save(project);
            return true;

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

    // 관리자 화면용 큐레이션
// - 선택된 프로젝트: displayOrder >= 0 (저장된 순서, 앞에 노출)
// - 선택 안 된 승인 프로젝트: displayOrder = -1 (좋아요 순, 뒤에 노출)
    public List<AdminDTO.CurationSectionDto> getAdminCurationSections() {
        List<Project> approved = approvedByLikes();

        return curationSectionRepository.findAll().stream()
                .sorted(Comparator.comparing(CurationSection::getDisplayOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(section -> {
                    Map<Long, Integer> selected = new HashMap<>();
                    for (CurationSectionProject link : section.getLinks()) {
                        if (link.getProject() != null) {
                            selected.put(link.getProject().getId(), link.getDisplayOrder());
                        }
                    }

                    List<AdminDTO.CurationSectionProjectDto> dtos = approved.stream()
                            .sorted(Comparator.comparing((Project p) -> !selected.containsKey(p.getId()))
                                    .thenComparing(p -> selected.getOrDefault(p.getId(), Integer.MAX_VALUE)))
                            .map(p -> toProjectDto(p, selected.getOrDefault(p.getId(), -1)))
                            .collect(Collectors.toList());

                    return toSectionDto(section, dtos);
                })
                .collect(Collectors.toList());
    }

    // 홈용 큐레이션
// - 저장된 링크 중 APPROVED만 저장 순서대로
// - 없으면 자동 선별 (좋아요 순, 동점 랜덤)
    public List<AdminDTO.CurationSectionDto> getCurationSections() {
        return curationSectionRepository.findAll().stream()
                .sorted(Comparator.comparing(CurationSection::getDisplayOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(section -> {
                    List<AdminDTO.CurationSectionProjectDto> dtos = section.getLinks().stream()
                            .filter(link -> link.getProject() != null
                                    && link.getProject().getStatus() == APPROVED)
                            .sorted(Comparator.comparingInt(CurationSectionProject::getDisplayOrder))
                            .map(link -> toProjectDto(link.getProject(), link.getDisplayOrder()))
                            .collect(Collectors.toList());

                    if (dtos.isEmpty()) {
                        dtos = pickAutoCuration().stream()
                                .map(p -> toProjectDto(p, -1))
                                .collect(Collectors.toList());
                    }
                    return toSectionDto(section, dtos);
                })
                .collect(Collectors.toList());
    }

    // 관리자용 조회는 저장된 링크만 반환하며, 빈 섹션을 자동 큐레이션으로 채우지 않는다.
    public List<AdminDTO.CurationSectionDto> getSavedCurationSections() {
        return curationSectionRepository.findAll().stream()
                .sorted(Comparator.comparing(CurationSection::getDisplayOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(section -> {
                    List<AdminDTO.CurationSectionProjectDto> dtos = section.getLinks().stream()
                            .filter(link -> link.getProject() != null)
                            .sorted(Comparator.comparingInt(CurationSectionProject::getDisplayOrder))
                            .map(link -> toProjectDto(link.getProject(), link.getDisplayOrder()))
                            .collect(Collectors.toList());
                    return toSectionDto(section, dtos);
                })
                .collect(Collectors.toList());
    }


        //큐레이션 저장
        public boolean saveCuration(AdminDTO.CurationSaveRequest request) {
            if (request == null || request.getSection_id() == null) {
                throw new IllegalArgumentException("섹션 ID가 필요합니다.");
            }

            CurationSection section = curationSectionRepository.findById(request.getSection_id())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 큐레이션 섹션입니다."));

            List<Long> requestIds = request.getProjectIds() == null ? List.of() : request.getProjectIds();
            if (requestIds.size() > 3) {
                throw new IllegalArgumentException("큐레이션 프로젝트는 최대 3개까지 선택할 수 있습니다.");
            }
            if (requestIds.stream().anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException("프로젝트 ID는 비어 있을 수 없습니다.");
            }
            if (new HashSet<>(requestIds).size() != requestIds.size()) {
                throw new IllegalArgumentException("중복된 프로젝트 ID가 포함되어 있습니다.");
            }

            // 검증을 먼저 끝내서, 실패하면 기존 링크가 그대로 남도록 함
            List<Project> ordered = new ArrayList<>();
            if (!requestIds.isEmpty()) {
                Map<Long, Project> projectMap = projectRepository.findAllById(requestIds).stream()
                        .collect(Collectors.toMap(Project::getId, p -> p));

                if (projectMap.size() != requestIds.size()) {
                    throw new IllegalArgumentException("존재하지 않는 프로젝트 ID가 포함되어 있습니다.");
                }
                for (Long id : requestIds) {
                    Project project = projectMap.get(id);
                    if (project.getStatus() != APPROVED) {
                        throw new IllegalArgumentException("승인된 프로젝트만 큐레이션에 넣을 수 있습니다. (ID: " + id + ")");
                    }
                    ordered.add(project);
                }
            }

            section.getLinks().clear();   // orphanRemoval로 기존 row 삭제
            for (int i = 0; i < ordered.size(); i++) {
                CurationSectionProject link = new CurationSectionProject();
                link.setSection(section);
                link.setProject(ordered.get(i));
                link.setDisplayOrder(i);
                section.getLinks().add(link);
            }

            curationSectionRepository.save(section);
            return true;
        }

// ===== 공통 헬퍼 =====

    // 승인 프로젝트를 좋아요 내림차순으로
    private List<Project> approvedByLikes() {
        return projectRepository.findByStatus(APPROVED).stream()
                .sorted(Comparator.comparing(
                        (Project p) -> Objects.requireNonNullElse(p.getLikeCount(), 0),
                        Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    // 자동 선별: 좋아요 높은 순 3개, 같은 좋아요 수끼리는 랜덤
    private List<Project> pickAutoCuration() {
        Map<Integer, List<Project>> byLike = approvedByLikes().stream()
                .collect(Collectors.groupingBy(
                        p -> Objects.requireNonNullElse(p.getLikeCount(), 0),
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<Project> result = new ArrayList<>();
        for (List<Project> group : byLike.values()) {
            Collections.shuffle(group);
            result.addAll(group);
            if (result.size() >= 3) break;
        }
        return result.subList(0, Math.min(3, result.size()));
    }

    private AdminDTO.CurationSectionDto toSectionDto(CurationSection section,
                                                     List<AdminDTO.CurationSectionProjectDto> dtos) {
        AdminDTO.CurationSectionDto sectionDto = new AdminDTO.CurationSectionDto();
        sectionDto.setSectionId(section.getSectionId());
        sectionDto.setTitle(section.getTitle());
        sectionDto.setDisplayOrder(section.getDisplayOrder());
        sectionDto.setSectionProjects(dtos);
        return sectionDto;
    }

    private AdminDTO.CurationSectionProjectDto toProjectDto(Project p, int displayOrder) {
        AdminDTO.CurationSectionProjectDto dto = new AdminDTO.CurationSectionProjectDto();
        dto.setProjectId(p.getId());
        dto.setProjectTitle(p.getProject_title());
        dto.setMediaUrl(p.getMedia_url());
        dto.setDisplayOrder(displayOrder);
        dto.setStatus(p.getStatus());
        return dto;
    }






}
