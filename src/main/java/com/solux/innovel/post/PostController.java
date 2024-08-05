package com.solux.innovel.post;

import com.solux.innovel.models.Post;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PostController {
    private final PostService postService;
    private final RecentPostService recentPostService;

    @RequestMapping(value = "/innovel/main", method = RequestMethod.GET)
    public ResponseEntity<List<List<Post>>> mainPage(HttpServletRequest request) {
        try {
            List<List<Post>> response = new ArrayList<>();

            List<Post> recentPosts = recentPostService.getRecentPostsFromCookie(request);
            List<Post> allPosts = postService.findAll();

            // 최근 읽은 게시물 리스트 처리
            if (recentPosts.size() >= 4) {
                response.add(recentPosts.subList(0, 4));
            } else {
                response.add(recentPosts);
            }

            // 모든 게시물 리스트 처리
            if (allPosts.size() >= 4) {
                response.add(allPosts.subList(0, 4));
            } else {
                response.add(allPosts);
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/innovel/posts/genre", method = RequestMethod.POST)
    public ResponseEntity<Page<Post>> showPostsByGenre(@RequestParam(value="page", defaultValue = "0") int page, @RequestParam("genre") String genre) {
        try {
            Page<Post> posts = postService.getPostsByGenre(page, genre);
            if (posts == null || posts.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @RequestMapping(value = "/innovel/posts/all/list/{page}")
    public ResponseEntity<Page<Post>> showAllPosts(@PathVariable("page") int page) {
        try {
            List<Post> allPosts = postService.findAll();
            if (allPosts == null || allPosts.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Order.desc("createdAt")));
            Page<Post> pagePosts = new PageImpl<>(allPosts, pageable, allPosts.size());
            return ResponseEntity.ok(pagePosts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @RequestMapping(value = "/innovel/posts/recent-read/list")
    public ResponseEntity<List<Post>> getRecentPosts(HttpServletRequest request) {
        try {
            List<Post> recentPosts = recentPostService.getRecentPostsFromCookie(request);
            if (recentPosts.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(recentPosts);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @RequestMapping(value = "/innovel/posts/recent-read/add", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> addRecentPost(@RequestParam("postId") Long postId, HttpServletRequest request, HttpServletResponse response) {
        try {
            recentPostService.saveRecentPostToCookie(postId, response, request);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e1) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e2) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    // 마이페이지 - 내가 창작한 소설 내에서, 소설 썸네일 클릭 시 나오는 화면
    // 내의 "삭제"와 "수정" 기능 수행 시 -> db 업데이트
    @RequestMapping(value = "/innovel/mypage/mypost/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deletePost(@PathVariable("id")  Long id) {
        postService.deletePost(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @RequestMapping(value = "/innovel/mypage/mypost/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Post> updatePost(@PathVariable("id") Long id, @RequestBody PostUpdateDTO postupdateDTO) {
        Post post = new Post();
        post.setTitle(postupdateDTO.getTitle());
        post.setContent(postupdateDTO.getContent());
        Post updatedPost = postService.updatePost(id, post);
        return ResponseEntity.ok(updatedPost);
    }

    @GetMapping("/innovel/posts/{id}")
    public ResponseEntity<Post> getPost(@PathVariable("id") Long id) {
        Post post = postService.getPostById(id);
        if (post != null) {
            log.info("Post with ID {} found and returned successfully.", id);
            return ResponseEntity.ok(post);
        } else {
            log.info("Post with ID {} not found.", id);
            return ResponseEntity.notFound().build();
        }
    }
}
