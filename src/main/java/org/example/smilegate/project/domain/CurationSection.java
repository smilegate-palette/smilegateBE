package org.example.smilegate.project.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class CurationSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sectionId;

    @Column(nullable = false)
    private String title = "";

    @Column(nullable = false)
    private Integer displayOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SectionType sectionType = SectionType.BOTTOM_CURATION;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "section",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<CurationSectionProject> links = new ArrayList<>();
}