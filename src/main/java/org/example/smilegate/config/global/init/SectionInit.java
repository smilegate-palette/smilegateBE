package org.example.smilegate.config.global.init;

import jakarta.annotation.PostConstruct;
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
        if (sectionRepository.count() == 0) {
            CurationSection home = new CurationSection();
            home.setTitle("HOME 큐레이션");
            home.setDisplayOrder(1);
            home.setSectionType(SectionType.BOTTOM_CURATION);
            home.setActive(true);
            sectionRepository.save(home);
        }

        if (homeBannerRepository.count() == 0) {
            HomeBanner banner = new HomeBanner();
            banner.setTitle("상단 배너");
            banner.setSubtitle("메인 배너");
            banner.setImageUrl("");
            banner.setBackgroundColor("#111827");
            banner.setButtonText("자세히 보기");
            banner.setButtonLink("/project");
            banner.setDisplayOrder(1);
            banner.setActive(true);
            homeBannerRepository.save(banner);
        }
    }
}