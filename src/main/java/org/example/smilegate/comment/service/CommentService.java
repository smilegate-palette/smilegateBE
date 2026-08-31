package org.example.smilegate.comment.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.smilegate.comment.domain.Comment;
import org.example.smilegate.comment.dto.CommentDTO;
import org.example.smilegate.comment.repository.CommentRepository;
import org.example.smilegate.project.domain.Project;
import org.example.smilegate.project.dto.ProjectDTO;
import org.example.smilegate.project.repository.ProjectRepository;
import org.example.smilegate.user.domain.User;
import org.example.smilegate.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;


    //댓글 등록
    public CommentDTO.CommentResponse CommentPost(Long user_id, Long project_id, CommentDTO.CommentRequest request){
        Project project = projectRepository.findById(project_id)
                .orElseThrow(() -> new RuntimeException("프로젝트가 존재하지 않습니다."));
        User user = userRepository.findById(user_id).orElseThrow(()-> new RuntimeException("로그인을 해주세요"));
        Comment comment = Comment.builder()
                        .content(request.getContent())
                        .nickname(request.getNickname())
                        .project(project)
                        .user(user)
                        .build();
        commentRepository.save(comment);
        CommentDTO.CommentResponse response = new CommentDTO.CommentResponse(comment);
        return response;

    }

    //댓글 수정
    public CommentDTO.CommentResponse CommentUpdate(Long user_id, Long project_id, CommentDTO.CommentRequest request, Long comment_id){
        Project project = projectRepository.findById(project_id)
                .orElseThrow(() -> new RuntimeException("프로젝트가 존재하지 않습니다."));
        User user = userRepository.findById(user_id).orElseThrow(()-> new RuntimeException("로그인을 해주세요"));
        Comment comment = commentRepository.findById(comment_id).orElseThrow(()-> new RuntimeException(" 댓글이 존재하지 않습니다."));
        comment.setContent(request.getContent());
        comment.setNickname(request.getNickname());
        comment.setUser(user);
        comment.setProject(project);
        commentRepository.save(comment);
        CommentDTO.CommentResponse response = new CommentDTO.CommentResponse(comment);
        response.setUpdated_at(LocalDateTime.now());
        return response;

    }

    // 댓글 삭제
    public boolean CommentDelete(Long user_id, Long project_id,Long comment_id) {
        try {
            Project project = projectRepository.findById(project_id)
                    .orElseThrow(() -> new RuntimeException("프로젝트가 존재하지 않습니다."));
            User user = userRepository.findById(user_id).orElseThrow(() -> new RuntimeException("로그인을 해주세요"));
            Comment comment = commentRepository.findById(comment_id).orElseThrow(() -> new RuntimeException(" 댓글이 존재하지 않습니다."));
            commentRepository.deleteById(comment_id);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);

        }

    }

    //댓글 조회
    public List<CommentDTO.CommentResponse> CommentDetail(Long project_id){
        try {
            Project project = projectRepository.findById(project_id)
                    .orElseThrow(() -> new RuntimeException("프로젝트가 존재하지 않습니다."));
            List<Comment> commentList = commentRepository.findAllByProjectId(project_id);
            List<CommentDTO.CommentResponse> commentResponses = commentList.stream()
                    .map(comment -> new CommentDTO.CommentResponse(
                            comment)).collect(Collectors.toList());
            return commentResponses;
        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }

}
