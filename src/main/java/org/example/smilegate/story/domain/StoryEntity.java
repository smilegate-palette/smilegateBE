package org.example.smilegate.story.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.smilegate.project.domain.BaseTimeEntity;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class StoryEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String story_title;
    String thumbnail_url;
    String story_username;
}

