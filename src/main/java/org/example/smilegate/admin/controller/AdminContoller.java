package org.example.smilegate.admin.controller;

import lombok.RequiredArgsConstructor;
import org.example.smilegate.admin.dto.AdminDTO;
import org.example.smilegate.admin.service.AdminService;
import org.example.smilegate.comment.dto.CommentDTO;
import org.example.smilegate.comment.service.CommentService;
import org.example.smilegate.project.dto.ProjectDTO;
import org.example.smilegate.project.service.ProjectService;
import org.example.smilegate.user.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api")
public class AdminContoller {
    private final AdminService adminService;
    private final ProjectService projectService;
    private final CommentService commentService;

    //관리자 페이지 조회
    @GetMapping(value = "/admin/{user_id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> Adminpage(@PathVariable Long user_id){
        try {
            AdminDTO.AdminResponse adminResponse = adminService.Adminpage(user_id);
            return ResponseEntity.ok(adminResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("관리자 페이지 조회에 실패했습니다." + e);
        }
    }

    //프로젝트 상세 조회
    @GetMapping(value = "/admin/{user_id}/{project_id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> AdminProjectDetail(@PathVariable Long user_id, @PathVariable Long project_id){
        try{
            AdminDTO.ProjectDetailDto detailDto = adminService.AdminProjectDetail(project_id,user_id);
            return ResponseEntity.ok(detailDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("프로젝트 상세 조회에 실패했습니다." + e);
        }
    }

    //프로젝트 내용 수정
    @PatchMapping(value = "/admin/{user_id}/{project_id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> AdminProjectUpdate(@RequestBody ProjectDTO.ProjectRequest requestDTO, @PathVariable Long project_id) throws Exception {
        ProjectDTO.ProjectResponse projectResponse = projectService.UpdateProject(project_id, requestDTO);
        try {
            return ResponseEntity.ok(projectResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("게시글 수정에 실패했습니다." + e);
        }
    }

    //프로젝트 승인
    @GetMapping(value = "/admin/{user_id}/{project_id}/approve", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> ApproveProject(@PathVariable Long project_id, @PathVariable Long user_id){
        try {
            adminService.ApproveProject(project_id,user_id);
            return ResponseEntity.status(HttpStatus.CREATED).body("프로젝트 승인에 성공했습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("프로젝트 승인에 실패했습니다." + e);
        }
    }

    //프로젝트 반려
    @PostMapping(value = "/admin/{user_id}/{project_id}/reject", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> RejectProject(@PathVariable Long project_id, @PathVariable Long user_id, @RequestBody AdminDTO.ProjectRejectRequestDto requestDto){
        try {
            adminService.RejectProject(project_id,user_id,requestDto.getReject_reason());
            return ResponseEntity.status(HttpStatus.CREATED).body("프로젝트 반려에 성공했습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("프로젝트 반려에 실패했습니다." + e);
        }
    }

    // 큐레이션 조회
    @GetMapping(value = "/admin/{user_id}/curation",consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> GetCuration(@PathVariable Long user_id){
        try {
            List<AdminDTO.CurationSectionDto> responses = adminService.getCurationSections();
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("큐레이션 조회에 실패했습니다." + e);
        }
    }

    // 큐레이션 순서 수정
    @PostMapping(value = "/admin/{user_id}/curation", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> ModifyCurationIndex(@PathVariable Long user_id, @RequestBody AdminDTO.CurationSaveRequest requestDto){
        try {
            adminService.saveCuration(requestDto);
            List<AdminDTO.CurationSectionDto> updated = adminService.getCurationSections();
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("큐레이션 순서 수정에 실패했습니다." + e);
        }
    }

    // 댓글 조회
    @GetMapping(value = "/admin/{user_id}/comment/", consumes = {MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> GetComments(@PathVariable Long user_id){
        try {
            List<CommentDTO.CommentResponse> responses = commentService.GetComment();
            return ResponseEntity.status(HttpStatus.CREATED).body(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("댓글 조회에 실패했습니다." + e);
        }
    }
}
