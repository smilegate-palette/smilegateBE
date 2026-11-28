package org.example.smilegate.admin.controller;

import lombok.RequiredArgsConstructor;
import org.example.smilegate.admin.dto.AdminDTO;
import org.example.smilegate.admin.service.AdminService;
import org.example.smilegate.comment.dto.CommentDTO;
import org.example.smilegate.comment.service.CommentService;
import org.example.smilegate.project.dto.ProjectDTO;
import org.example.smilegate.project.service.ImageService;
import org.example.smilegate.project.service.ProjectService;
import org.example.smilegate.project.domain.BannerPage;
import org.example.smilegate.project.domain.BannerSize;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api")
public class AdminContoller {
    private final AdminService adminService;
    private final ProjectService projectService;
    private final CommentService commentService;
    private final ImageService imageService;

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
        ProjectDTO.ProjectResponse projectResponse = projectService.UpdateAdminProject(project_id, requestDTO);
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
            List<AdminDTO.CurationSectionDto> responses = adminService.getSavedCurationSections();
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("큐레이션 조회에 실패했습니다." + e);
        }
    }

    // 큐레이션 섹션 생성
    @PostMapping(value = "/admin/{user_id}/curation/section", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> CreateCurationSection(@PathVariable Long user_id,
                                                  @RequestBody AdminDTO.CurationSaveRequest requestDto){
        try {
            adminService.saveCuration(requestDto);
            List<AdminDTO.CurationSectionDto> updated = adminService.getSavedCurationSections();
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("큐레이션 생성에 실패했습니다." + e);
        }
    }

    // 큐레이션 섹션 수정
    @PutMapping(value = "/admin/{user_id}/curation/section/{section_id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> UpdateCurationSection(@PathVariable Long user_id,
                                                  @PathVariable Long section_id,
                                                  @RequestBody AdminDTO.CurationSaveRequest requestDto){
        try {
            requestDto.setSection_id(section_id);
            adminService.saveCuration(requestDto);
            List<AdminDTO.CurationSectionDto> updated = adminService.getSavedCurationSections();
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("큐레이션 수정에 실패했습니다." + e);
        }
    }

    // 큐레이션 섹션 삭제
    @DeleteMapping(value = "/admin/{user_id}/curation/section/{section_id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> DeleteCurationSection(@PathVariable Long user_id,
                                                  @PathVariable Long section_id){
        try {
            adminService.deleteCurationSection(section_id);
            return ResponseEntity.ok("큐레이션 섹션 삭제에 성공했습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("큐레이션 섹션 삭제에 실패했습니다." + e);
        }
    }

    // 큐레이션 순서 수정
    @PostMapping(value = "/admin/{user_id}/curation", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> ModifyCurationIndex(@PathVariable Long user_id, @RequestBody AdminDTO.CurationSaveRequest requestDto){
        try {
            adminService.saveCuration(requestDto);
            List<AdminDTO.CurationSectionDto> updated = adminService.getSavedCurationSections();
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("큐레이션 순서 수정에 실패했습니다." + e);
        }
    }

    // 배너 조회 (page=HOME 또는 OFF_THE_RECORD)
    @GetMapping(value = "/admin/{user_id}/banner")
    public ResponseEntity<?> getBanners(@PathVariable Long user_id,
                                        @RequestParam("page") String page) {
        try {
            BannerPage bannerPage = BannerPage.fromPath(page);
            return ResponseEntity.ok(adminService.getHomeBanners(bannerPage));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    // 배너 이미지 업로드 (신규 등록)
    @PostMapping(value = "/admin/{user_id}/banner/{page}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadBanner(@PathVariable Long user_id,
                                          @PathVariable String page,
                                          @RequestParam("file") MultipartFile file,
                                          @RequestParam(value = "size", required = true) String size) {
        try {
            BannerPage bannerPage = BannerPage.fromPath(page);
            BannerSize bannerSize = BannerSize.fromParam(size);
            AdminDTO.HomeBannerDto banner = adminService.uploadBanner(bannerPage, bannerSize, file);
            return ResponseEntity.ok(banner);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("message", "배너 이미지 업로드에 실패했습니다."));
        }
    }

    // 배너 이미지 수정 (같은 업서트 방식)
    @PutMapping(value = "/admin/{user_id}/banner/{page}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateBanner(@PathVariable Long user_id,
                                          @PathVariable String page,
                                          @RequestParam("file") MultipartFile file,
                                          @RequestParam(value = "size", required = true) String size) {
        try {
            BannerPage bannerPage = BannerPage.fromPath(page);
            BannerSize bannerSize = BannerSize.fromParam(size);
            AdminDTO.HomeBannerDto banner = adminService.uploadBanner(bannerPage, bannerSize, file);
            return ResponseEntity.ok(banner);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("message", "배너 이미지 수정에 실패했습니다."));
        }
    }

    // 배너 삭제
    @DeleteMapping(value = "/admin/{user_id}/banner/{page}")
    public ResponseEntity<?> deleteBanner(@PathVariable Long user_id,
                                          @PathVariable String page,
                                          @RequestParam(value = "size", required = true) String size) {
        try {
            BannerPage bannerPage = BannerPage.fromPath(page);
            BannerSize bannerSize = BannerSize.fromParam(size);
            adminService.deleteBanner(bannerPage, bannerSize);
            return ResponseEntity.ok(java.util.Map.of("message", "배너가 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    // 공개 배너 조회 (page=HOME 또는 OFF_THE_RECORD)
    @GetMapping(value = "/banner")
    public ResponseEntity<?> getPublicBanner(@RequestParam("page") String page) {
        try {
            BannerPage bannerPage = BannerPage.fromPath(page);
            return ResponseEntity.ok(bannerPage == BannerPage.HOME
                    ? adminService.getPublicHomeBanners()
                    : adminService.getPublicOffTheRecordBanners());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }


    // 댓글 조회
    @GetMapping(value = "/admin/{user_id}/comment", consumes = {MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> GetComments(@PathVariable Long user_id){
        try {
            List<CommentDTO.CommentResponse> responses = commentService.GetComment();
            return ResponseEntity.status(HttpStatus.CREATED).body(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("댓글 조회에 실패했습니다." + e);
        }
    }

    // 관리자 홈 설정 조회(배너 + 큐레이션 섹션)
    @GetMapping(value = "/admin/{user_id}/home", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> GetHomeSettings(@PathVariable Long user_id){
        try {
            AdminDTO.HomePageDto response = new AdminDTO.HomePageDto();
            response.setBanners(adminService.getHomeBanners());
            response.setCurationSections(adminService.getAdminCurationSections());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("홈 설정 조회에 실패했습니다." + e);
        }
    }
}
