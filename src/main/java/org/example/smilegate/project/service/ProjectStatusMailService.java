package org.example.smilegate.project.service;

import lombok.RequiredArgsConstructor;
import org.example.smilegate.project.domain.Project;
import org.example.smilegate.project.domain.ProjectStatus;
import org.example.smilegate.user.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectStatusMailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendStatusResult(Project project) {
        User owner = project.getUser();
        if (owner == null || owner.getEmail() == null || owner.getEmail().isBlank()) {
            return;
        }

        String title = project.getProject_title() == null ? "프로젝트" : project.getProject_title();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(owner.getEmail());

        if (project.getStatus() == ProjectStatus.APPROVED) {
            message.setSubject("[Smilegate] 프로젝트가 승인되었습니다");
            message.setText("안녕하세요, " + owner.getUsername() + "님.\n\n"
                    + "등록하신 프로젝트 '" + title + "'가 승인되었습니다.\n"
                    + "승인된 프로젝트는 서비스에 공개됩니다.");
        } else if (project.getStatus() == ProjectStatus.REJECTED) {
            message.setSubject("[Smilegate] 프로젝트가 반려되었습니다");
            message.setText("안녕하세요, " + owner.getUsername() + "님.\n\n"
                    + "등록하신 프로젝트 '" + title + "'가 반려되었습니다.\n"
                    + "반려 사유: " + (project.getReject_reason() == null
                    ? "관리자 반려 사유가 등록되지 않았습니다." : project.getReject_reason()));
        } else {
            return;
        }

        try {
            javaMailSender.send(message);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(ProjectStatusMailService.class)
                    .error("프로젝트 승인/반려 이메일 전송 실패: projectId={}", project.getId(), e);
        }
    }
}
