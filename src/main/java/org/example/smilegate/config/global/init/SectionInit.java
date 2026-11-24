package org.example.smilegate.config.global.init;

import lombok.AllArgsConstructor;
import org.example.smilegate.project.domain.CurationSection;
import org.example.smilegate.project.domain.HomeBanner;
import org.example.smilegate.project.domain.SectionType;
import org.example.smilegate.project.repository.CurationSectionRepository;
import org.example.smilegate.project.repository.HomeBannerRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SectionInit {
    private final CurationSectionRepository sectionRepository;
    private final HomeBannerRepository homeBannerRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        boolean hasHomeCuration = sectionRepository.findAll().stream()
                .anyMatch(section -> section.getSectionType() == SectionType.BOTTOM_CURATION);
        if (!hasHomeCuration) {
            CurationSection section = new CurationSection();
            section.setTitle("추천 프로젝트");
            section.setDisplayOrder(1);
            section.setSectionType(SectionType.BOTTOM_CURATION);
            section.setActive(true);
            sectionRepository.save(section);
        }

        ensureProjectListSection("창의워크숍 프로젝트", 2);
        ensureProjectListSection("유스파티 프로젝트", 3);

        sectionRepository.findAll().forEach(section -> {
            if (section.getSectionType() == null) {
                section.setSectionType(SectionType.BOTTOM_CURATION);
            }
            if (section.getTitle() == null || section.getTitle().isBlank()) {
                section.setTitle("큐레이션");
            }
            if (section.getDisplayOrder() == null) {
                section.setDisplayOrder(1);
            }
        });
        sectionRepository.saveAll(sectionRepository.findAll());

        if (homeBannerRepository.count() == 0) {
            HomeBanner banner = new HomeBanner();
            banner.setImageUrl("");
            banner.setDisplayOrder(0);
            banner.setActive(true);
            homeBannerRepository.save(banner);
        }
    }

    private void ensureProjectListSection(String title, int displayOrder) {
        boolean exists = sectionRepository.findAll().stream()
                .anyMatch(section -> section.getTitle().equals(title));
        if (!exists) {
            CurationSection section = new CurationSection();
            section.setTitle(title);
            section.setDisplayOrder(displayOrder);
            section.setSectionType(SectionType.PROJECT_LIST);
            section.setActive(true);
            sectionRepository.save(section);
        }
    }
}