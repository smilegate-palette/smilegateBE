package org.example.smilegate.story.dto;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
        private String link;
        private LocalDateTime created_at;
        private LocalDateTime updated_at;

        public StoryRequest(StoryEntity story){
            this.id = story.getId();
            this.story_title = story.getStory_title();
            this.thumbnail_url = story.getThumbnail_url();
            this.link = story.getLink();
            this.created_at = story.getCreatedAt();
            this.updated_at = story.getUpdatedAt();
        }
    }
}
