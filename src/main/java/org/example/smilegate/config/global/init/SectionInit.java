package org.example.smilegate.config.global.init;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.example.smilegate.project.domain.CurationSection;
import org.example.smilegate.project.repository.CurationSectionRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SectionInit {
    private final CurationSectionRepository sectionRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if (sectionRepository.count() == 0) {
            CurationSection home = new CurationSection();
            home.setTitle("HOME 큐레이션");
            home.setDisplayOrder(1);
            sectionRepository.save(home);
        }
    }
}
