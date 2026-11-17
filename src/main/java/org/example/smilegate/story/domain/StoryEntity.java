package org.example.smilegate.story.domain;

import jakarta.persistence.*;
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
    private Long id;

    @Column(nullable = false)
    private String story_title;

    @Column
    private String thumbnail_url;

    @Column
    private String link;


}
