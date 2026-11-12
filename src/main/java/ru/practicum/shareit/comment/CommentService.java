package ru.practicum.shareit.comment;

import java.util.List;

public interface CommentService {

    Comment create(Long authorId, Long itemId, Comment comment);

    List<Comment> getByItem(Long itemId);
}