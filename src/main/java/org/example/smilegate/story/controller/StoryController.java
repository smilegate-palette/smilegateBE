package org.example.smilegate.story.controller;

import lombok.RequiredArgsConstructor;
import org.example.smilegate.project.dto.ProjectDTO;
import org.example.smilegate.story.dto.StoryDTO;
import org.example.smilegate.story.service.StoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api")
public class StoryController {
    private final StoryService storyService;
    //스토리 생성
    @PostMapping(value = "/admin/story/{user_id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> CreateStoryPost(@RequestBody StoryDTO.StoryRequest requestDTO, @PathVariable("user_id") Long user_id) throws Exception {

        StoryDTO.StoryRequest Response = storyService.createStory(user_id, requestDTO);
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(Response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("스토리 생성에 실패했습니다." + e);
        }
    }

    //스토리 목록 조회
    @GetMapping(value = "/admin/story", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> GetStoryIndex() {
        try {
            List<StoryDTO.StoryRequest> Responses = storyService.GetStoryIndex();
            return ResponseEntity.status(HttpStatus.OK).body(Responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("스토리 조회에 실패했습니다." + e);
        }

    }


    //스토리 수정
    @PatchMapping(value = "/admin/story/{user_id}/{story_id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> UpdateStoryPost(@RequestBody StoryDTO.StoryRequest requestDTO, @PathVariable("story_id") Long story_id,@PathVariable("user_id") Long user_id) throws Exception {

        try {
            StoryDTO.StoryRequest Response = storyService.UpdateStory(user_id,requestDTO,story_id);
            return ResponseEntity.status(HttpStatus.CREATED).body(Response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("스토리 수정에 실패했습니다." + e);
        }
    }

    //스토리 삭제
    @DeleteMapping(value = "/admin/story/{user_id}/{story_id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> DeleteStoryPost(@PathVariable("story_id") Long story_id,@PathVariable("user_id") Long user_id) throws Exception {

        boolean success = storyService.DeleteStory(story_id);

        if (success) {
            return ResponseEntity.status(HttpStatus.OK).body("스토리 삭제에 성공했습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("스토리 삭제에 실패하였습니다.");
        }
    }
}
