package org.example.smilegate.project.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.smilegate.project.dto.ProjectDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ImageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private String supabaseServiceRoleKey;

    @Value("${supabase.storage-bucket}")
    private String thumbnailBucket;

    private static final long MAX_THUMBNAIL_BYTES = 10L * 1024 * 1024;
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public void uploadThumbnailIfBase64(ProjectDTO.ProjectRequest requestDTO) {
        String thumbnail = requestDTO.getThumbnail_url();
        if (thumbnail == null || thumbnail.isBlank() || isHttpUrl(thumbnail)) {
            return;
        }

        ThumbnailData thumbnailData = parseThumbnail(thumbnail.trim());
        String serviceRoleKey = requireSupabaseServiceRoleKey();
        String objectName = "projects/" + UUID.randomUUID() + "." + thumbnailData.extension();
        String objectPath = encodePath(thumbnailBucket) + "/" + encodePath(objectName);
        URI uploadUri = URI.create(trimTrailingSlash(supabaseUrl) + "/storage/v1/object/" + objectPath);

        HttpRequest uploadRequest = HttpRequest.newBuilder(uploadUri)
                .timeout(Duration.ofSeconds(30))
                .header("apikey", serviceRoleKey)
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("Content-Type", thumbnailData.contentType())
                .header("x-upsert", "false")
                .POST(HttpRequest.BodyPublishers.ofByteArray(thumbnailData.bytes()))
                .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(uploadRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Supabase thumbnail upload failed: status={}, body={}", response.statusCode(), response.body());
                throw new IllegalStateException("썸네일 업로드에 실패했습니다.");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Supabase Storage와 통신할 수 없습니다.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("썸네일 업로드가 중단되었습니다.", e);
        }

        requestDTO.setThumbnail_url(trimTrailingSlash(supabaseUrl)
                + "/storage/v1/object/public/" + objectPath);
    }

    private ThumbnailData parseThumbnail(String value) {
        String contentType = "image/png";
        String base64 = value;

        if (value.startsWith("data:")) {
            int separator = value.indexOf(',');
            if (separator < 0) {
                throw new IllegalArgumentException("썸네일 Data URL 형식이 올바르지 않습니다.");
            }
            String metadata = value.substring(5, separator).toLowerCase(Locale.ROOT);
            if (!metadata.endsWith(";base64")) {
                throw new IllegalArgumentException("썸네일은 Base64 이미지여야 합니다.");
            }
            contentType = metadata.substring(0, metadata.length() - ";base64".length());
            base64 = value.substring(separator + 1);
        }

        if (!Set.of("image/png", "image/jpeg", "image/webp", "image/gif").contains(contentType)) {
            throw new IllegalArgumentException("PNG, JPEG, WEBP, GIF 썸네일만 업로드할 수 있습니다.");
        }

        final byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(base64.replaceAll("\\s", ""));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("썸네일 Base64 값이 올바르지 않습니다.", e);
        }
        if (bytes.length == 0 || bytes.length > MAX_THUMBNAIL_BYTES) {
            throw new IllegalArgumentException("썸네일은 10MB 이하의 이미지여야 합니다.");
        }
        return new ThumbnailData(bytes, contentType, extensionFor(contentType));
    }

    private String requireSupabaseServiceRoleKey() {
        if (supabaseUrl == null || supabaseUrl.isBlank() || supabaseServiceRoleKey == null || supabaseServiceRoleKey.isBlank()) {
            throw new IllegalStateException("SUPABASE_URL 및 SUPABASE_SERVICE_ROLE_KEY 환경 변수가 필요합니다.");
        }
        return supabaseServiceRoleKey;
    }

    private boolean isHttpUrl(String value) {
        return value.startsWith("http://") || value.startsWith("https://");
    }

    private String trimTrailingSlash(String value) {
        return value.replaceAll("/+$", "");
    }

    private String encodePath(String path) {
        return Arrays.stream(path.split("/"))
                .map(segment -> java.net.URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20"))
                .collect(Collectors.joining("/"));
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> "png";
        };
    }

    private record ThumbnailData(byte[] bytes, String contentType, String extension) {
    }
}
