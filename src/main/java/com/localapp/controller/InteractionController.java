package com.localapp.controller;

import com.localapp.model.Comment;
import com.localapp.model.Like;
import com.localapp.repository.CommentRepository;
import com.localapp.repository.LikeRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/interactions")
public class InteractionController {
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    public InteractionController(CommentRepository commentRepository, LikeRepository likeRepository) {
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
    }

    @PostMapping("/comment")
    public Comment addComment(@Valid @RequestBody Comment comment, @AuthenticationPrincipal String userId) {
        comment.setCommentId(UUID.randomUUID().toString());
        comment.setUserId(userId);
        comment.setTimestamp(Instant.now().toString());
        commentRepository.save(comment);
        return comment;
    }

    @GetMapping("/comments/{postId}")
    public List<Comment> getComments(@PathVariable String postId) {
        return commentRepository.findByPostId(postId);
    }

    @PostMapping("/like")
    public ResponseEntity<Like> addLike(@Valid @RequestBody Like like, @AuthenticationPrincipal String userId) {
        List<Like> existingLikes = likeRepository.findByPostId(like.getPostId());
        boolean alreadyLiked = existingLikes.stream()
                .anyMatch(l -> l.getUserId().equals(userId));
        if (alreadyLiked) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // 409 Conflict
        }
        like.setLikeId(UUID.randomUUID().toString());
        like.setUserId(userId);
        like.setTimestamp(Instant.now().toString());
        likeRepository.save(like);
        return ResponseEntity.ok(like);
    }

    @GetMapping("/likes/{postId}")
    public List<Like> getLikes(@PathVariable String postId) {
        return likeRepository.findByPostId(postId);
    }
}