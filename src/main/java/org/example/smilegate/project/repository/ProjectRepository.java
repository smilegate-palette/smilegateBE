package org.example.smilegate.project.repository;

import org.example.smilegate.project.domain.Project;
import org.example.smilegate.project.domain.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> , JpaSpecificationExecutor<Project> {
    List<Project> findByStatus(ProjectStatus status);
    List<Project> findTop3ByOrderByLikeCountDesc();
}
