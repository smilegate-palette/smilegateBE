package org.example.smilegate.project.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.smilegate.project.dto.ProjectDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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


    private static final long MAX_THUMBNAIL_BYTES = 10L * 1024 * 1024;
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public String uploadThumbnailIfBase64(String thumbnail, String thumbnailBucket) {
        if (thumbnail == null || thumbnail.isBlank() || isHttpUrl(thumbnail)) {
            return thumbnail;
        }

        return uploadThumbnailData(parseThumbnail(thumbnail.trim()), thumbnailBucket);
    }

    public String uploadThumbnailFile(MultipartFile file, String thumbnailBucket) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("썸네일 파일이 필요합니다.");
        }
        if (file.getSize() > MAX_THUMBNAIL_BYTES) {
            throw new IllegalArgumentException("썸네일은 10MB 이하의 이미지여야 합니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !Set.of("image/png", "image/jpeg", "image/webp", "image/gif")
                .contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("PNG, JPEG, WEBP, GIF 썸네일만 업로드할 수 있습니다.");
        }

        byte[] bytes = file.getBytes();
        ThumbnailData thumbnailData = new ThumbnailData(bytes, contentType.toLowerCase(Locale.ROOT),
                extensionFor(contentType.toLowerCase(Locale.ROOT)));
        validateImageSignature(thumbnailData);
        return uploadThumbnailData(thumbnailData, thumbnailBucket);
    }

    public void deleteThumbnailIfManaged(String thumbnailUrl, String thumbnailBucket) {
        String objectName = managedThumbnailObjectName(thumbnailUrl, thumbnailBucket);
        if (objectName == null) {
            return;
        }

        String serviceRoleKey = requireSupabaseServiceRoleKey();
        URI deleteUri = URI.create(trimTrailingSlash(supabaseUrl)
                + "/storage/v1/object/" + encodePath(thumbnailBucket + "/" + objectName));
        HttpRequest deleteRequest = HttpRequest.newBuilder(deleteUri)
                .timeout(Duration.ofSeconds(30))
                .header("apikey", serviceRoleKey)
                .header("Authorization", "Bearer " + serviceRoleKey)
                .DELETE()
                .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Supabase thumbnail deletion failed: status={}, body={}",
                        response.statusCode(), response.body());
                throw new IllegalStateException("썸네일 삭제에 실패했습니다.");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Supabase Storage와 통신할 수 없습니다.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("썸네일 삭제가 중단되었습니다.", e);
        }
    }

    private String managedThumbnailObjectName(String thumbnailUrl, String thumbnailBucket) {
        if (thumbnailUrl == null || thumbnailUrl.isBlank() || !isHttpUrl(thumbnailUrl)
                || supabaseUrl == null || supabaseUrl.isBlank()) {
            return null;
        }

        final URI thumbnailUri;
        final URI configuredUri;
        try {
            thumbnailUri = URI.create(thumbnailUrl);
            configuredUri = URI.create(trimTrailingSlash(supabaseUrl));
        } catch (IllegalArgumentException e) {
            return null;
        }

        if (!sameOrigin(thumbnailUri, configuredUri)
                || thumbnailUri.getRawQuery() != null
                || thumbnailUri.getRawFragment() != null) {
            return null;
        }

        String pathPrefix = "/storage/v1/object/public/" + thumbnailBucket + "/";
        String rawPath = thumbnailUri.getRawPath();
        if (rawPath == null || !rawPath.startsWith(pathPrefix)) {
            return null;
        }

        String objectName = rawPath.substring(pathPrefix.length());
        if (!objectName.matches("projects/[0-9a-fA-F-]{36}\\.(png|jpg|webp|gif)")) {
            return null;
        }
        return objectName;
    }

    private boolean sameOrigin(URI first, URI second) {
        return first.getScheme() != null && second.getScheme() != null
                && first.getScheme().equalsIgnoreCase(second.getScheme())
                && first.getHost() != null && second.getHost() != null
                && first.getHost().equalsIgnoreCase(second.getHost())
                && effectivePort(first) == effectivePort(second);
    }

    private int effectivePort(URI uri) {
        if (uri.getPort() >= 0) {
            return uri.getPort();
        }
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }

    private String uploadThumbnailData(ThumbnailData thumbnailData, String thumbnailBucket) {
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

        return trimTrailingSlash(supabaseUrl)
                + "/storage/v1/object/public/" + objectPath;
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
        ThumbnailData thumbnailData = new ThumbnailData(bytes, contentType, extensionFor(contentType));
        validateImageSignature(thumbnailData);
        return thumbnailData;
    }

    private void validateImageSignature(ThumbnailData thumbnail) {
        byte[] bytes = thumbnail.bytes();
        boolean valid = switch (thumbnail.contentType()) {
            case "image/png" -> bytes.length >= 8
                    && (bytes[0] & 0xff) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4e
                    && bytes[3] == 0x47 && bytes[4] == 0x0d && bytes[5] == 0x0a
                    && bytes[6] == 0x1a && bytes[7] == 0x0a;
            case "image/jpeg" -> bytes.length >= 3
                    && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8
                    && (bytes[2] & 0xff) == 0xff;
            case "image/gif" -> bytes.length >= 6
                    && new String(bytes, 0, 3, StandardCharsets.US_ASCII).equals("GIF")
                    && (new String(bytes, 3, 3, StandardCharsets.US_ASCII).equals("87a")
                    || new String(bytes, 3, 3, StandardCharsets.US_ASCII).equals("89a"));
            case "image/webp" -> bytes.length >= 12
                    && new String(bytes, 0, 4, StandardCharsets.US_ASCII).equals("RIFF")
                    && new String(bytes, 8, 4, StandardCharsets.US_ASCII).equals("WEBP");
            default -> false;
        };
        if (!valid) {
            throw new IllegalArgumentException("파일 내용이 지정한 이미지 형식과 일치하지 않습니다.");
        }
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
