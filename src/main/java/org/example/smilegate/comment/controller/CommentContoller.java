package org.example.smilegate.comment.controller;

import lombok.RequiredArgsConstructor;
import org.example.smilegate.comment.dto.CommentDTO;
import org.example.smilegate.comment.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api")
public class CommentContoller {
    private final CommentService commentService;

    @PostMapping(value = "/comment/{project_id}/{user_id}", consumes = {MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> PostComment(@PathVariable("project_id") Long project_id, @PathVariable("user_id") Long user_id , @RequestBody CommentDTO.CommentRequest request){
        CommentDTO.CommentResponse response = commentService.CommentPost(user_id,project_id,request);
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("댓글 등록에 실패했습니다." + e);
        }


    }

    @PatchMapping(value = "/comment/{project_id}/{user_id}/{comment_id}", consumes = {MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> UpdateComment(@PathVariable("project_id") Long project_id, @PathVariable("user_id") Long user_id , @RequestBody CommentDTO.CommentRequest request,@PathVariable("comment_id") Long comment_id){
        CommentDTO.CommentResponse response = commentService.CommentUpdate(user_id,project_id,request,comment_id);
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("댓글 수정에 실패했습니다." + e);
        }

    }

    @DeleteMapping(value = "/comment/{project_id}/{user_id}/{comment_id}", consumes = {MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> UpdateComment(@PathVariable("project_id") Long project_id, @PathVariable("user_id") Long user_id , @PathVariable("comment_id") Long comment_id) {
           boolean success = commentService.CommentDelete(user_id,project_id,comment_id);
           if(success){
               return ResponseEntity.ok("댓글 삭제에 성공했습니다.");
           }else{ return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("댓글 삭제에 실패했습니다.");
           }
    }

    @GetMapping(value = "/comment/{project_id}", consumes = {MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> GetComments(@PathVariable("project_id") Long project_id){
        try {
            List<CommentDTO.CommentResponse> responses = commentService.CommentDetail(project_id);
            return ResponseEntity.status(HttpStatus.CREATED).body(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("댓글 조회에 실패했습니다." + e);
        }
    }





}
