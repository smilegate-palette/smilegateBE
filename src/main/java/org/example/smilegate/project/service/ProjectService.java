package org.example.smilegate.project.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.smilegate.admin.dto.AdminDTO;
import org.example.smilegate.admin.service.AdminService;
import org.example.smilegate.project.domain.Project;
import org.example.smilegate.project.domain.ProjectStatus;
import org.example.smilegate.project.dto.ProjectDTO;
import org.example.smilegate.project.repository.ProjectRepository;
import org.example.smilegate.user.domain.User;
import org.example.smilegate.user.repository.UserRepository;
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


    //프로젝트 게시글 생성
    public ProjectDTO.ProjectResponse CreateProject(Long user_id, ProjectDTO.ProjectRequest RequestDTO) throws Exception {

        User user = userRepository.findById(user_id).orElseThrow(()-> new Exception("사용자가 존재하지 않습니다."));
        try {
            imageService.uploadThumbnailIfBase64(RequestDTO);
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

    //프로젝트 목록 조회
    public List<ProjectDTO.ProjectResponse> GetprojectIndex(){
        try{
        List<Project> projects = projectRepository.findAll();
        List<ProjectDTO.ProjectResponse> projectResponses = projects.stream()
                .map(project -> new ProjectDTO.ProjectResponse(
                        project)).collect(Collectors.toList());
        return projectResponses;} catch (Exception e) {
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

    public List<ProjectDTO.HomeResponse> Getprojecthome(){
        try {
            List<AdminDTO.CurationSectionDto> sections = adminService.getCurationSections();

            List<AdminDTO.CurationSectionProjectDto> allProjects = sections.stream()
                    .map(AdminDTO.CurationSectionDto::getSectionProjects)   // 각 섹션의 프로젝트 리스트
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            return allProjects.stream()
                    .map(dto -> {
                        Project project = projectRepository.findById(dto.getProjectId())
                                .orElse(null);
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
            imageService.uploadThumbnailIfBase64(RequestDTO);
            project.Update(RequestDTO);
            projectRepository.save(project);
            ProjectDTO.ProjectResponse projectResponse = new ProjectDTO.ProjectResponse(project);
            return projectResponse;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    //프로젝트 게시글 삭제
    public boolean DeleteProject(Long project_id) throws Exception{
        Project project = projectRepository.findById(project_id).orElseThrow(()-> new Exception("프로젝트가 존재하지 않습니다."));
        try{
            projectRepository.deleteById(project_id);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }






}
