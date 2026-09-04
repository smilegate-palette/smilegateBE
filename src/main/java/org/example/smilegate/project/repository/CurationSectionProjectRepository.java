package org.example.smilegate.project.repository;

import org.example.smilegate.project.domain.CurationSectionProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurationSectionProjectRepository extends JpaRepository<CurationSectionProject, Long> {
    @Query("SELECT csp FROM CurationSectionProject csp " +
            "WHERE csp.section.sectionId = :sectionId " +
            "ORDER BY csp.displayOrder ASC")
    List<CurationSectionProject> findBySectionOrder(@Param("sectionId") Long sectionId);
}