package ru.practicum.shareit.item.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.Optional;

@Component
public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwner(),
                Optional.ofNullable(item.getRequestId()).orElse(null));
    }

    public static Item toItem(ItemDto itemDto) {
        return new Item(
                itemDto.getId() != null ? itemDto.getId() : null,
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                itemDto.getOwner(),
                Optional.ofNullable(itemDto.getRequestId()).orElse(null)
        );
    }

    public static ItemWithBookingsDto toFullItemDto(Item item) {
        return new ItemWithBookingsDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                Optional.ofNullable(item.getRequestId()).orElse(null),
                null,
                null,
                null,
                null);
    }

    public static CommentDto commentDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }

    public static Comment toComment(CommentDto commentDto) {
        return new Comment(
                Optional.ofNullable(commentDto.getId()).orElse(null),
                commentDto.getText(),
                null,
                null,
                commentDto.getCreated()
        );
    }
}