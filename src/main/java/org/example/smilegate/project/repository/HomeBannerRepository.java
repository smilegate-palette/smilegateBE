package org.example.smilegate.project.repository;

import org.example.smilegate.project.domain.HomeBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeBannerRepository extends JpaRepository<HomeBanner, Long> {
    List<HomeBanner> findAllByOrderByDisplayOrderAsc();
    List<HomeBanner> findByActiveTrueOrderByDisplayOrderAsc();
}

