package org.example.smilegate.project.controller;

import lombok.RequiredArgsConstructor;
import org.example.smilegate.project.dto.ProjectDTO;
import org.example.smilegate.project.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api")
public class Project {

    private final ProjectService projectService;

    //게시글 생성
    @PostMapping(value = "/project/{user_id}", consumes = {MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> CreateProjectPost(@RequestBody ProjectDTO.ProjectRequest requestDTO,@PathVariable("user_id") Long user_id ) throws Exception {

        ProjectDTO.ProjectResponse projectResponse = projectService.CreateProject(user_id, requestDTO);
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(projectResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("게시글 생성에 실패했습니다." + e);
        }
    }

    //게시글 목록 조회
    @GetMapping(value = "/project", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> GetProjectIndex(){
        List<ProjectDTO.ProjectResponse> projectResponses = projectService.GetprojectIndex();
        try{
            return ResponseEntity.status(HttpStatus.OK).body(projectResponses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("게시글 조회에 실패했습니다." + e);
        }

    }

    //게시글 상세 조회
    @GetMapping(value = "/project/{project_id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> GetProjectDetail(@PathVariable("project_id") Long project_id){
        List<ProjectDTO.ProjectDetailResponse> projectDetailResponses = projectService.GetprojectDetail(project_id);
        try{
            return ResponseEntity.status(HttpStatus.OK).body(projectDetailResponses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("게시글 상세 조회에 실패했습니다." + e);
        }

    }



    //게시글 수정
    @PatchMapping(value = "/project/{user_id}/{project_id}", consumes = {MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> UpdateProjectPost(@RequestBody ProjectDTO.ProjectRequest requestDTO,@PathVariable("project_id") Long project_id ) throws Exception {

        ProjectDTO.ProjectResponse projectResponse = projectService.UpdateProject(project_id, requestDTO);

        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(projectResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("게시글 수정에 실패했습니다." + e);
        }
    }

    //게시글 삭제
    @DeleteMapping(value = "/project/{user_id}/{project_id}", consumes = {MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> DeleteProjectPost(@PathVariable("project_id") Long project_id ) throws Exception {

        boolean success = projectService.DeleteProject(project_id);

        if (success) {
            return ResponseEntity.status(HttpStatus.OK).body("게시글 삭제에 성공했습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("게시글 삭제에 실패하였습니다.");
        }
    }

    @GetMapping
    public ResponseEntity<?> home(){
        try{
            List<ProjectDTO.HomeResponse> homeResponses = projectService.Getprojecthome();
            return ResponseEntity.status(HttpStatus.OK).body(homeResponses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("게시글 조회에 실패했습니다." + e);
        }

    }


}
