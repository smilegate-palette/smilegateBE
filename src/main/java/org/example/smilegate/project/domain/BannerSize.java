package org.example.smilegate.project.domain;

public enum BannerSize {
    PC,
    PC_FULLSCREEN,
    MOBILE;

    public static BannerSize fromParam(String value) {
        if (value == null) {
            throw new IllegalArgumentException("배너 규격(size)은 필수입니다.");
        }

        String normalized = value.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        try {
            return BannerSize.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 배너 규격입니다: " + value);
        }
    }
}

