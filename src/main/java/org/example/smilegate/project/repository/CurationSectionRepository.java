package org.example.smilegate.project.repository;

import org.example.smilegate.project.domain.CurationSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurationSectionRepository extends JpaRepository<CurationSection, Long> {

}
