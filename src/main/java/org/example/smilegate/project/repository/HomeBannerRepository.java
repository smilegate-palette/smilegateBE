package org.example.smilegate.project.repository;

import org.example.smilegate.project.domain.BannerPage;
import org.example.smilegate.project.domain.BannerSize;
import org.example.smilegate.project.domain.HomeBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeBannerRepository extends JpaRepository<HomeBanner, Long> {
    List<HomeBanner> findAllByOrderByDisplayOrderAsc();
    List<HomeBanner> findByActiveTrueOrderByDisplayOrderAsc();
    java.util.Optional<HomeBanner> findFirstByDisplayOrder(Integer displayOrder);
    java.util.List<HomeBanner> findAllByDisplayOrderOrderByIdAsc(Integer displayOrder);
    java.util.Optional<HomeBanner> findFirstByDisplayOrderAndSize(Integer displayOrder, BannerSize size);
    java.util.List<HomeBanner> findAllByDisplayOrderAndSizeOrderByIdAsc(Integer displayOrder, BannerSize size);
}
