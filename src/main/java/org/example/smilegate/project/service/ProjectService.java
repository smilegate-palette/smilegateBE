package org.example.smilegate.project.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.smilegate.admin.dto.AdminDTO;
import org.example.smilegate.admin.service.AdminService;
import org.example.smilegate.project.domain.CurationSection;
import org.example.smilegate.project.domain.Project;
import org.example.smilegate.project.domain.ProjectStatus;
import org.example.smilegate.project.dto.ProjectDTO;
import org.example.smilegate.project.repository.CurationSectionRepository;
import org.example.smilegate.project.repository.ProjectRepository;
import org.example.smilegate.user.domain.User;
import org.example.smilegate.user.repository.UserRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AdminService adminService;
    private final ImageService imageService;
    private final CurationSectionRepository curationSectionRepository;


    //프로젝트 게시글 생성
    public ProjectDTO.ProjectResponse CreateProject(Long user_id, ProjectDTO.ProjectRequest RequestDTO) throws Exception {

        User user = userRepository.findById(user_id).orElseThrow(()-> new Exception("사용자가 존재하지 않습니다."));
        try {
            RequestDTO.setThumbnail_url(imageService.uploadThumbnailIfBase64(RequestDTO.getThumbnail_url(),"thumbnails"));
            Project project = new Project(RequestDTO);
            project.setStatus(ProjectStatus.PENDING);
            project.setUser(user);
            projectRepository.save(project);
            ProjectDTO.ProjectResponse projectResponse = new ProjectDTO.ProjectResponse(project);
            return projectResponse;
        }
            catch (Exception e){
            throw new Exception(e);
        }

    }

    //프로젝트 목록 조회(검색 필터링)
    public List<ProjectDTO.ProjectResponse> GetprojectIndex(Integer year,
                                                            Integer maxYear,
                                                            String program,
                                                            String type,
                                                            String keyword,
                                                            String sort){
        try {
            {
                Specification<Project> spec = Specification.unrestricted();
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("status"), ProjectStatus.APPROVED)
                );

                // 예: 2025 선택
                if (year != null) {
                    spec = spec.and((root, query, cb) ->
                            cb.equal(root.get("year"), year)
                    );
                }

                // 예: ~2023 선택
                if (maxYear != null) {
                    spec = spec.and((root, query, cb) ->
                            cb.lessThanOrEqualTo(root.get("year"), maxYear)
                    );
                }

                // 예: 창의워크숍 / 유스파티 선택
                if (program != null && !program.isBlank()) {
                    spec = spec.and((root, query, cb) ->
                            cb.equal(root.get("program_name"), program)
                    );
                }

                // 예: GAME / AI / VIDEO 선택
                if (type != null && !type.isBlank()) {
                    spec = spec.and((root, query, cb) ->
                            cb.equal(root.get("category"), type)
                    );
                }

                // 프로젝트명 또는 참여자명 검색
                if (keyword != null && !keyword.isBlank()) {
                    String pattern = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";

                    spec = spec.and((root, query, cb) ->
                            cb.or(
                                    cb.like(cb.lower(root.get("projectTitle")), pattern),
                                    cb.like(cb.lower(root.get("participants")), pattern)
                            )
                    );
                }

                org.springframework.data.domain.Sort springSort =
                        "popular".equalsIgnoreCase(sort)
                                ? org.springframework.data.domain.Sort.by(
                                org.springframework.data.domain.Sort.Direction.DESC,
                                "likeCount"
                        )
                                : org.springframework.data.domain.Sort.by(
                                org.springframework.data.domain.Sort.Direction.DESC,
                                "createdAt"
                        );
                List<Project> projects = projectRepository.findAll(spec, springSort);
                List<ProjectDTO.ProjectResponse> projectResponses = projects.stream()
                        .map(project -> new ProjectDTO.ProjectResponse(
                                project)).collect(Collectors.toList());
                return projectResponses;
            }
        }
            catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

   //프로젝트 상세 조회
    public List<ProjectDTO.ProjectDetailResponse> GetprojectDetail(Long proejct_id){
        try{
            Optional<Project> projectOpt = projectRepository.findById(proejct_id);
            Project project = projectOpt.orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다. 아이디: " + proejct_id));
            ProjectDTO.ProjectDetailResponse projectDetailResponse = new ProjectDTO.ProjectDetailResponse(project);
            return Collections.singletonList(projectDetailResponse);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //home 조회
    public List<ProjectDTO.HomeResponse> Getprojecthome(){
        try {
            List<AdminDTO.CurationSectionDto> sections = adminService.getCurationSections();

            List<AdminDTO.CurationSectionProjectDto> allProjects = sections.stream()
                    .map(AdminDTO.CurationSectionDto::getSectionProjects)   // 각 섹션의 프로젝트 리스트
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .filter(entry -> entry.getStatus() == ProjectStatus.APPROVED)
                    .collect(Collectors.toList());

            Set<Long> projectIds = allProjects.stream()
                    .map(AdminDTO.CurationSectionProjectDto::getProjectId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Map<Long, Project> projectsById = projectRepository.findAllById(projectIds).stream()
                    .collect(Collectors.toMap(Project::getId, project -> project));

            return allProjects.stream()
                    .map(dto -> {
                        Project project = projectsById.get(dto.getProjectId());
                        if (project == null) {
                            return null;
                        }
                        return new ProjectDTO.HomeResponse(
                                dto.getProjectTitle(),
                                project.getCategory(),
                                ProjectStatus.APPROVED,
                                dto.getMediaUrl() != null ? dto.getMediaUrl() : project.getMedia_url(),
                                project.getThumbnail_url(),
                                project.getId()
                        );
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    //프로젝트 게시물 수정
    public ProjectDTO.ProjectResponse UpdateProject(Long project_id, ProjectDTO.ProjectRequest RequestDTO) throws Exception{
        Project project = projectRepository.findById(project_id).orElseThrow(()-> new Exception("프로젝트가 존재하지 않습니다."));
        try{
            RequestDTO.setThumbnail_url(imageService.uploadThumbnailIfBase64(RequestDTO.getThumbnail_url(),"thumbnails"));
            project.Update(RequestDTO);
            projectRepository.save(project);
            ProjectDTO.ProjectResponse projectResponse = new ProjectDTO.ProjectResponse(project);
            return projectResponse;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ProjectDTO.ProjectResponse UpdateAdminProject(Long projectId, ProjectDTO.ProjectRequest request) throws Exception {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new Exception("프로젝트가 존재하지 않습니다."));
        request.setThumbnail_url(imageService.uploadThumbnailIfBase64(request.getThumbnail_url(), "thumbnails"));
        if (request.getProject_title() != null) project.setProject_title(request.getProject_title());
        if (request.getCategory() != null) project.setCategory(request.getCategory());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        if (request.getOrganization() != null) project.setOrganization(request.getOrganization());
        if (request.getParticipants() != null) project.setParticipants(request.getParticipants());
        if (request.getMedia_url() != null) project.setMedia_url(request.getMedia_url());
        if (request.getThumbnail_url() != null) project.setThumbnail_url(request.getThumbnail_url());
        projectRepository.save(project);
        return new ProjectDTO.ProjectResponse(project);
    }



    //프로젝트 게시글 삭제
    public boolean DeleteProject(Long project_id) throws Exception{
        Project project = projectRepository.findById(project_id)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));

        imageService.deleteThumbnailIfManaged(project.getThumbnail_url(), "thumbnails");

        // 모든 섹션의 링크 컬렉션에서 제거 → orphanRemoval로 curation_section_project row 삭제
        for (CurationSection section : curationSectionRepository.findAll()) {
            section.getLinks().removeIf(link ->
                    link.getProject() != null && project_id.equals(link.getProject().getId()));
        }

        projectRepository.delete(project);
        return true;
    }






}
