package ru.practicum.shareit.comment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.client.CommentClient;
import ru.practicum.shareit.comment.dto.CommentDto;

@RestController
@RequestMapping("/items/{itemId}/comment")
@RequiredArgsConstructor
@Validated
public class CommentController {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final CommentClient commentClient;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader(USER_HEADER) @Positive Long userId,
            @PathVariable @Positive Long itemId,
            @RequestBody @Valid CommentDto commentDto) {
        return commentClient.createComment(userId, itemId, commentDto);
    }

    @GetMapping
    public ResponseEntity<Object> getByItem(@PathVariable @Positive Long itemId) {
        return commentClient.getComments(itemId);
    }
}
