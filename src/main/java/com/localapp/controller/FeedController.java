package com.localapp.controller;

import com.localapp.model.Post;
import com.localapp.repository.CommentRepository;
import com.localapp.repository.LikeRepository;
import com.localapp.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/feed")
public class FeedController {
    private static final Logger logger = LoggerFactory.getLogger(FeedController.class);
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    public FeedController(PostRepository postRepository,
                          LikeRepository likeRepository,
                          CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
    }

    @GetMapping("/shared")
    public List<Post> getSharedPins(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(required = false) Double radiusMiles,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "all") String type) {
        logger.info("Fetching shared pins: lat={}, lon={}, radius={}, page={}, size={}", lat, lon, radiusMiles, page, size);

        List<Post> posts = (radiusMiles != null)
                ? postRepository.findPostsNear(lat, lon, radiusMiles)
                : postRepository.findAll();

        List<Post> filteredPosts = posts.stream()
                .filter(post -> "pin".equals(post.getCategory()) && post.isShared())
                .filter(post -> "all".equals(type) || type.equals(post.getType()))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .skip((long) page * size)
                .limit(size)
                .map(post -> {
                    post.setLikes(likeRepository.findByPostId(post.getPostId()));
                    post.setComments(commentRepository.findByPostId(post.getPostId()));
                    return post;
                })
                .collect(Collectors.toList());
        logger.info("Returning {} posts for page {}", filteredPosts.size(), page);
        return filteredPosts;
    }
}