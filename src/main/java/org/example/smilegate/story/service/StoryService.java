package org.example.smilegate.story.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.smilegate.admin.dto.AdminDTO;
import org.example.smilegate.project.domain.Project;
import org.example.smilegate.project.domain.ProjectStatus;
import org.example.smilegate.project.dto.ProjectDTO;
import org.example.smilegate.project.service.ImageService;
import org.example.smilegate.story.domain.StoryEntity;
import org.example.smilegate.story.dto.StoryDTO;
import org.example.smilegate.story.repository.StoryRepository;
import org.example.smilegate.user.domain.User;
import org.example.smilegate.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StoryService {
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final StoryRepository storyRepository;

    public StoryDTO.StoryRequest createStory(Long user_id, StoryDTO.StoryRequest request) throws Exception {
        User user = userRepository.findById(user_id).orElseThrow(()-> new Exception("사용자가 존재하지 않습니다."));
        try {
            StoryEntity story = new StoryEntity();
            story.setStory_title(request.getStory_title());
            story.setThumbnail_url(imageService.uploadThumbnailIfBase64(request.getThumbnail_url(),"story"));
            story.setStory_username(request.getStory_username());
            storyRepository.save(story);
            StoryDTO.StoryRequest Response = new StoryDTO.StoryRequest(story);
            return Response;
        }
        catch (Exception e){
            throw new Exception(e);
        }
    }

    public StoryDTO.StoryRequest UpdateStory(Long user_id, StoryDTO.StoryRequest request, Long story_id) throws Exception {
        try {
            User user = userRepository.findById(user_id).orElseThrow(()-> new Exception("사용자가 존재하지 않습니다."));
            StoryEntity story = storyRepository.findById(story_id).orElseThrow(()->new Exception("스토리가 존재하지 않습니다.")) ;
            story.setStory_title(request.getStory_title());
            story.setThumbnail_url(imageService.uploadThumbnailIfBase64(request.getThumbnail_url(),"story"));
            story.setStory_username(request.getStory_username());
            storyRepository.save(story);
            StoryDTO.StoryRequest Response = new StoryDTO.StoryRequest(story);
            return Response;
        }
        catch (Exception e){
            throw new Exception(e);
        }
    }

    public boolean DeleteStory(Long story_id) throws Exception{
        org.example.smilegate.story.domain.StoryEntity story = storyRepository.findById(story_id).orElseThrow(()-> new Exception("스토리가 존재하지 않습니다."));
        try{
            storyRepository.deleteById(story_id);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<StoryDTO.StoryRequest> GetStoryIndex() {

        List<StoryEntity> storyEntities = storyRepository.findAll();
        return storyEntities.stream()
                .map(StoryDTO.StoryRequest::new)  // 생성자 매핑
                .collect(Collectors.toList());

    }

}
