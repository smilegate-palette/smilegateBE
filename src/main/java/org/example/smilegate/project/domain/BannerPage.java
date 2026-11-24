package org.example.smilegate.project.domain;

public enum BannerPage {
    HOME,
    OFF_THE_RECORD;

    public static BannerPage fromPath(String value) {
        if (value == null) {
            throw new IllegalArgumentException("배너 페이지는 필수입니다.");
        }

        String normalized = value.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        if ("HOME".equals(normalized)) return HOME;
        if ("OFF_THE_RECORD".equals(normalized) || "STORY".equals(normalized)) return OFF_THE_RECORD;
        throw new IllegalArgumentException("지원하지 않는 배너 페이지입니다: " + value);
    }

    public int displayOrder() {
        return this == HOME ? 0 : 1;
    }

    public static BannerPage fromDisplayOrder(Integer displayOrder) {
        return displayOrder != null && displayOrder == OFF_THE_RECORD.displayOrder()
                ? OFF_THE_RECORD
                : HOME;
    }
}
