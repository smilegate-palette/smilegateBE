package org.example.smilegate.admin.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.smilegate.admin.dto.AdminDTO;
import org.example.smilegate.comment.repository.CommentRepository;
import org.example.smilegate.project.domain.CurationSection;
import org.example.smilegate.project.domain.CurationSectionProject;
import org.example.smilegate.project.domain.HomeBanner;
import org.example.smilegate.project.domain.Project;
import org.example.smilegate.project.domain.ProjectStatus;
import org.example.smilegate.project.repository.CurationSectionRepository;
import org.example.smilegate.project.repository.HomeBannerRepository;
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
    private final HomeBannerRepository homeBannerRepository;

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

        List<AdminDTO.CurationSectionDto> curationSectionDtos = getAdminCurationSections();
        return new AdminDTO.AdminResponse(statsDto, projectListItemDtos, commentDtoList, curationSectionDtos);
    }

    public AdminDTO.ProjectDetailDto AdminProjectDetail(Long project_id, Long user_id){
        User user = userRepository.findById(user_id).orElseThrow(()-> new RuntimeException("로그인을 해주세요."));
        Project project = projectRepository.findById(project_id).orElseThrow(()-> new RuntimeException("프로젝트를 찾을 수 없습니다."));
        return new AdminDTO.ProjectDetailDto(project);
    }

    public boolean ApproveProject(Long project_id, Long user_id){
        User user = userRepository.findById(user_id).orElseThrow(() -> new RuntimeException("로그인을 해주세요."));
        Project project = projectRepository.findById(project_id)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));
        project.setStatus(APPROVED);
        projectRepository.save(project);
        return true;
    }

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

    public List<AdminDTO.CurationSectionDto> getAdminCurationSections() {
        List<Project> approved = approvedByLikes();

        return curationSectionRepository.findAll().stream()
                .filter(CurationSection::isActive)
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

    public List<AdminDTO.CurationSectionDto> getCurationSections() {
        return curationSectionRepository.findAll().stream()
                .filter(CurationSection::isActive)
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

    public List<AdminDTO.CurationSectionDto> getSavedCurationSections() {
        return curationSectionRepository.findAll().stream()
                .filter(CurationSection::isActive)
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

    public boolean saveCuration(AdminDTO.CurationSaveRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청 값이 비어 있습니다.");
        }

        CurationSection section;
        if (request.getSection_id() == null) {
            section = new CurationSection();
        } else {
            section = curationSectionRepository.findById(request.getSection_id())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 큐레이션 섹션입니다."));
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            section.setTitle(request.getTitle());
        }

        if (request.getDisplayOrder() != null) {
            section.setDisplayOrder(request.getDisplayOrder());
        }

        if (request.getSectionType() != null) {
            section.setSectionType(request.getSectionType());
        }

        section.setActive(request.isActive());
        curationSectionRepository.save(section);

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

        section.getLinks().clear();
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

    public boolean deleteCurationSection(Long sectionId) {
        CurationSection section = curationSectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 큐레이션 섹션입니다."));
        curationSectionRepository.delete(section);
        return true;
    }

    public List<AdminDTO.HomeBannerDto> getHomeBanners() {
        return homeBannerRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::toBannerDto)
                .collect(Collectors.toList());
    }

    public boolean saveHomeBanner(AdminDTO.HomeBannerSaveRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("배너 요청 값이 비어 있습니다.");
        }

        HomeBanner banner;
        if (request.getBannerId() == null) {
            banner = new HomeBanner();
        } else {
            banner = homeBannerRepository.findById(request.getBannerId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 배너입니다."));
        }

        if (request.getTitle() != null) banner.setTitle(request.getTitle());
        if (request.getSubtitle() != null) banner.setSubtitle(request.getSubtitle());
        if (request.getImageUrl() != null) banner.setImageUrl(request.getImageUrl());
        if (request.getBackgroundColor() != null) banner.setBackgroundColor(request.getBackgroundColor());
        if (request.getButtonText() != null) banner.setButtonText(request.getButtonText());
        if (request.getButtonLink() != null) banner.setButtonLink(request.getButtonLink());
        if (request.getDisplayOrder() != null) banner.setDisplayOrder(request.getDisplayOrder());
        banner.setActive(request.isActive());

        homeBannerRepository.save(banner);
        return true;
    }

    public boolean deleteHomeBanner(Long bannerId) {
        HomeBanner banner = homeBannerRepository.findById(bannerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 배너입니다."));
        homeBannerRepository.delete(banner);
        return true;
    }

    private List<Project> approvedByLikes() {
        return projectRepository.findByStatus(APPROVED).stream()
                .sorted(Comparator.comparing(
                        (Project p) -> Objects.requireNonNullElse(p.getLikeCount(), 0),
                        Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

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
            if (result.size() >= 5) break;
        }
        return result.subList(0, Math.min(5, result.size()));
    }

    private AdminDTO.CurationSectionDto toSectionDto(CurationSection section,
                                                    List<AdminDTO.CurationSectionProjectDto> dtos) {
        AdminDTO.CurationSectionDto sectionDto = new AdminDTO.CurationSectionDto();
        sectionDto.setSectionId(section.getSectionId());
        sectionDto.setTitle(section.getTitle());
        sectionDto.setDisplayOrder(section.getDisplayOrder());
        sectionDto.setSectionType(section.getSectionType());
        sectionDto.setActive(section.isActive());
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

    private AdminDTO.HomeBannerDto toBannerDto(HomeBanner banner) {
        AdminDTO.HomeBannerDto dto = new AdminDTO.HomeBannerDto();
        dto.setBannerId(banner.getId());
        dto.setTitle(banner.getTitle());
        dto.setSubtitle(banner.getSubtitle());
        dto.setImageUrl(banner.getImageUrl());
        dto.setBackgroundColor(banner.getBackgroundColor());
        dto.setButtonText(banner.getButtonText());
        dto.setButtonLink(banner.getButtonLink());
        dto.setDisplayOrder(banner.getDisplayOrder());
        dto.setActive(banner.isActive());
        return dto;
    }
}
