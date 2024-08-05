package com.solux.innovel.post;

import com.solux.innovel.models.Genre;
import com.solux.innovel.models.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Post> getPostsByGenre(int page, String genreStr) {
        // String을 Genre enum으로 변환
        Genre genre;
        try {
            genre = Genre.valueOf(genreStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 유효하지 않은 장르 문자열일 경우 예외 처리 또는 기본 장르 설정
            throw new RuntimeException("Invalid genre: " + genreStr);
        }

        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Order.desc("createdAt")));
        return postRepository.findPostsByGenre(genre, pageable);
    }

    public Post getPostById(Long id) {
        Optional<Post> post = postRepository.findById(id);
        return post.orElse(null); // 반환할 수 있는 null 체크 또는 예외 처리 추가
    }

    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        postRepository.delete(post);
    }

    @Transactional
    public Post updatePost(Long id, Post postDetails) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        post.updatePost(postDetails);

        return postRepository.save(post);
    }
}
