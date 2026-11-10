package ru.practicum.shareit.comment;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentDto;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items/{itemId}/comment")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public CommentDto create(
            @RequestHeader("X-Sharer-User-Id") Long authorId,
            @PathVariable Long itemId,
            @Valid @RequestBody CommentDto commentDto) {

        Comment comment = new Comment();
        comment.setText(commentDto.getText());

        return CommentMapper.toCommentDto(
                commentService.create(authorId, itemId, comment)
        );
    }

    @GetMapping
    public List<CommentDto> getByItem(@PathVariable Long itemId) {
        return commentService.getByItem(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }
}