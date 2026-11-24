package org.example.smilegate.project.repository;

import org.example.smilegate.project.domain.CurationSection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurationSectionRepository extends JpaRepository<CurationSection, Long> {
    @Override
    @EntityGraph(attributePaths = {"links", "links.project"})
    List<CurationSection> findAll();
}
