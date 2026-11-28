package org.example.smilegate.admin.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.smilegate.admin.dto.AdminDTO;
import org.example.smilegate.comment.repository.CommentRepository;
import org.example.smilegate.project.domain.BannerPage;
import org.example.smilegate.project.domain.BannerSize;
import org.example.smilegate.project.domain.CurationSection;
import org.example.smilegate.project.domain.CurationSectionProject;
import org.example.smilegate.project.domain.HomeBanner;
import org.example.smilegate.project.domain.Project;
import org.example.smilegate.project.domain.ProjectStatus;
import org.example.smilegate.project.domain.SectionType;
import org.example.smilegate.project.repository.CurationSectionRepository;
import org.example.smilegate.project.repository.HomeBannerRepository;
import org.example.smilegate.project.repository.ProjectRepository;
import org.example.smilegate.project.service.ImageService;
import org.example.smilegate.project.service.ProjectStatusMailService;
import org.example.smilegate.story.repository.StoryRepository;
import org.example.smilegate.user.domain.User;
import org.example.smilegate.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final ImageService imageService;
    private final ProjectStatusMailService projectStatusMailService;

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
        return new AdminDTO.AdminResponse(statsDto, projectListItemDtos, commentDtoList, curationSectionDtos, getHomeBanners());
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
        projectStatusMailService.sendStatusResult(project);
        return true;
    }

    public boolean RejectProject(Long project_id, Long user_id,String RejectReason){
        try {
            User user = userRepository.findById(user_id).orElseThrow(() -> new RuntimeException("로그인을 해주세요."));
            if (RejectReason == null || RejectReason.isBlank()) {
                throw new IllegalArgumentException("반려 사유를 입력해주세요.");
            }
            Project project = projectRepository.findById(project_id).orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));
            project.setStatus(ProjectStatus.REJECTED);
            project.setReject_reason(RejectReason.trim());
            projectRepository.save(project);
            projectStatusMailService.sendStatusResult(project);
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
                            .limit(10)
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
                        return toSectionDto(section, List.of());
                    }
                    return toSectionDto(section, dtos);
                })
                .collect(Collectors.toList());
    }

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

        if (request.getSectionType() != null) {
            section.setSectionType(request.getSectionType());
        }

        if (section.getSectionType() == SectionType.BOTTOM_CURATION) {
            section.setDisplayOrder(1);
        } else {
            if (request.getDisplayOrder() == null || request.getDisplayOrder() < 2) {
                throw new IllegalArgumentException("일반 큐레이션 노출 순서는 2 이상이어야 합니다.");
            }
            section.setDisplayOrder(request.getDisplayOrder());
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            section.setTitle(request.getTitle());
        }

        section.setActive(request.isActive());
        curationSectionRepository.save(section);

        List<CurationSection> nonHomeSections = curationSectionRepository.findAll().stream()
                .filter(item -> item.getSectionType() != SectionType.BOTTOM_CURATION)
                .sorted(Comparator.comparing(CurationSection::getDisplayOrder,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(CurationSection::getSectionId,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        int nextOrder = 2;
        for (CurationSection item : nonHomeSections) {
            item.setDisplayOrder(nextOrder++);
        }
        curationSectionRepository.saveAll(nonHomeSections);

        List<Long> requestIds = request.getProjectIds() == null ? List.of() : request.getProjectIds();
        if (requestIds.size() > 10) {
            throw new IllegalArgumentException("큐레이션 프로젝트는 최대 10개까지 선택할 수 있습니다.");
        }
        if (new HashSet<>(requestIds).size() != requestIds.size()) {
            throw new IllegalArgumentException("중복된 프로젝트 ID가 포함되어 있습니다.");
        }

        Map<Long, Project> projectMap = requestIds.isEmpty()
                ? Map.of()
                : projectRepository.findAllById(requestIds).stream()
                        .collect(Collectors.toMap(Project::getId, project -> project));
        if (projectMap.size() != requestIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 프로젝트 ID가 포함되어 있습니다.");
        }

        section.getLinks().clear();
        int projectOrder = 0;
        for (Long projectId : requestIds) {
            Project project = projectMap.get(projectId);
            if (project.getStatus() != APPROVED) {
                throw new IllegalArgumentException("승인된 프로젝트만 큐레이션에 넣을 수 있습니다. (ID: " + projectId + ")");
            }
            CurationSectionProject link = new CurationSectionProject();
            link.setSection(section);
            link.setProject(project);
            link.setDisplayOrder(projectOrder++);
            section.getLinks().add(link);
        }
        curationSectionRepository.save(section);

        return true;
    }

    public boolean deleteCurationSection(Long sectionId) {
        CurationSection section = curationSectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 큐레이션 섹션입니다."));
        if (section.getSectionType() == SectionType.BOTTOM_CURATION) {
            throw new IllegalArgumentException("HOME 큐레이션은 삭제할 수 없습니다.");
        }
        curationSectionRepository.delete(section);
        return true;
    }

    public List<AdminDTO.HomeBannerDto> getHomeBanners(BannerPage page) {
        return homeBannerRepository.findAllByDisplayOrderOrderByIdAsc(page.displayOrder()).stream()
                .map(this::toBannerDto)
                .collect(Collectors.toList());
    }

    public List<AdminDTO.HomeBannerDto> getHomeBanners() {
        return getHomeBanners(BannerPage.HOME);
    }

    public List<AdminDTO.HomeBannerDto> getPublicHomeBanners() {
        return getHomeBanners(BannerPage.HOME).stream()
                .filter(AdminDTO.HomeBannerDto::isActive)
                .collect(Collectors.toList());
    }

    public List<AdminDTO.HomeBannerDto> getPublicOffTheRecordBanners() {
        return getHomeBanners(BannerPage.OFF_THE_RECORD).stream()
                .filter(AdminDTO.HomeBannerDto::isActive)
                .collect(Collectors.toList());
    }

    public AdminDTO.HomeBannerDto uploadBanner(BannerPage page, BannerSize size, MultipartFile file) throws java.io.IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 배너 이미지를 선택해주세요.");
        }

        String uploadedUrl = imageService.uploadThumbnailFile(file, "banner");
        Integer displayOrder = page.displayOrder();

        HomeBanner banner = homeBannerRepository.findFirstByDisplayOrderAndSize(displayOrder, size)
                .orElseGet(HomeBanner::new);
        String previousUrl = banner.getImageUrl();

        banner.setImageUrl(uploadedUrl);
        banner.setDisplayOrder(displayOrder);
        banner.setSize(size);
        banner.setActive(true);
        HomeBanner saved = homeBannerRepository.save(banner);

        if (previousUrl != null && !previousUrl.equals(uploadedUrl)) {
            imageService.deleteThumbnailIfManaged(previousUrl, "banner");
        }
        return toBannerDto(saved);
    }

    public boolean deleteBanner(BannerPage page, BannerSize size) {
        List<HomeBanner> banners = homeBannerRepository.findAllByDisplayOrderAndSizeOrderByIdAsc(page.displayOrder(), size);
        if (banners.isEmpty()) {
            throw new IllegalArgumentException("등록된 배너가 없습니다.");
        }

        for (HomeBanner banner : banners) {
            imageService.deleteThumbnailIfManaged(banner.getImageUrl(), "banner");
            homeBannerRepository.delete(banner);
        }
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
            if (result.size() >= 10) break;
        }
        return result.subList(0, Math.min(10, result.size()));
    }

    private List<Project> pickProjectListCuration(String title) {
        String keyword = title != null && title.contains("유스") ? "유스" : "창의";
        return approvedByLikes().stream()
                .filter(project -> project.getProgram_name() != null
                        && project.getProgram_name().contains(keyword))
                .limit(10)
                .collect(Collectors.toList());
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
        dto.setThumbnailUrl(p.getThumbnail_url());
        dto.setDisplayOrder(displayOrder);
        dto.setStatus(p.getStatus());
        return dto;
    }

    private AdminDTO.HomeBannerDto toBannerDto(HomeBanner banner) {
        AdminDTO.HomeBannerDto dto = new AdminDTO.HomeBannerDto();
        dto.setBannerId(banner.getId());
        dto.setImageUrl(banner.getImageUrl());
        dto.setDisplayOrder(banner.getDisplayOrder());
        dto.setActive(banner.isActive());
        dto.setBannerPage(BannerPage.fromDisplayOrder(banner.getDisplayOrder()));
        dto.setSize(banner.getSize() == null ? BannerSize.PC : banner.getSize());

        return dto;
    }
}
