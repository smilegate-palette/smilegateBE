package org.example.smilegate.story.dto;

import jakarta.persistence.Column;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.smilegate.story.domain.StoryEntity;

import java.time.LocalDateTime;

public class StoryDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class StoryRequest{
        private Long id;
        @Column(nullable = false)
        private String story_title;
        private String thumbnail_url;
        private LocalDateTime created_at;
        private LocalDateTime updated_at;
        private String story_username;

        public StoryRequest(StoryEntity story){
            this.id = story.getId();
            this.story_title = story.getStory_title();
            this.thumbnail_url=story.getThumbnail_url();
            this.story_username=story.getStory_username();
            this.created_at= story.getCreatedAt();
            this.updated_at=story.getUpdatedAt();
        }
    }
}
