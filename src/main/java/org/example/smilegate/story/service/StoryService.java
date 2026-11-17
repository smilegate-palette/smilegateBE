package org.example.smilegate.story.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.smilegate.story.domain.StoryEntity;
import org.example.smilegate.story.dto.StoryDTO;
import org.example.smilegate.story.repository.StoryRepository;
import org.example.smilegate.user.domain.User;
import org.example.smilegate.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StoryService {
    private final UserRepository userRepository;
    private final org.example.smilegate.project.service.ImageService imageService;
    private final StoryRepository storyRepository;

    public StoryDTO.StoryRequest createStory(Long user_id, StoryDTO.StoryRequest request) throws Exception {
        User user = userRepository.findById(user_id).orElseThrow(() -> new Exception("사용자가 존재하지 않습니다."));
        try {
            StoryEntity story = new StoryEntity();
            story.setStory_title(request.getStory_title());
            story.setThumbnail_url(imageService.uploadThumbnailIfBase64(request.getThumbnail_url(), "story"));
            story.setLink(request.getLink());
            storyRepository.save(story);
            return new StoryDTO.StoryRequest(story);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public StoryDTO.StoryRequest UpdateStory(Long user_id, StoryDTO.StoryRequest request, Long story_id) throws Exception {
        try {
            User user = userRepository.findById(user_id).orElseThrow(() -> new Exception("사용자가 존재하지 않습니다."));
            StoryEntity story = storyRepository.findById(story_id).orElseThrow(() -> new Exception("스토리가 존재하지 않습니다."));
            if (request.getStory_title() != null) story.setStory_title(request.getStory_title());
            if (request.getThumbnail_url() != null) {
                story.setThumbnail_url(imageService.uploadThumbnailIfBase64(request.getThumbnail_url(), "story"));
            }
            if (request.getLink() != null) story.setLink(request.getLink());
            storyRepository.save(story);
            return new StoryDTO.StoryRequest(story);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public boolean DeleteStory(Long story_id) throws Exception {
        StoryEntity story = storyRepository.findById(story_id).orElseThrow(() -> new Exception("스토리가 존재하지 않습니다."));
        try {
            storyRepository.deleteById(story_id);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<StoryDTO.StoryRequest> GetStoryIndex() {
        return storyRepository.findAll().stream()
                .sorted(Comparator.comparing(StoryEntity::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(StoryDTO.StoryRequest::new)
                .collect(Collectors.toList());
    }
}