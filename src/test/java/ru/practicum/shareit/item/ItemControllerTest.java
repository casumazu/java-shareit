package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ItemServiceImpl itemService;

    private final LocalDateTime created = LocalDateTime.now();
    private final User owner = new User(1L, "owner", "owner@email.ru");
    private final ItemDto itemDto = new ItemDto(
            1L, "item", "description", true, null, null);
    private final CommentDto commentDto = new CommentDto(
            1L, "comment", "author", created);
    private final ItemWithBookingsDto itemFullDto = new ItemWithBookingsDto(1L, "item", "description",
            true, null, owner, null, null, List.of(commentDto));
    private final PageRequest pageRequest = PageRequest.of(0 / 20, 20);


    @Test
    void addItem() throws Exception {
        when(itemService.create(itemDto, 1L)).thenReturn(itemDto);

        String result = mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(itemDto, objectMapper.readValue(result, ItemDto.class));
        verify(itemService).create(itemDto, 1L);
    }

    @SneakyThrows
    @Test
    void updateItem() {
        when(itemService.update(itemDto, 1L, 1L)).thenReturn(itemDto);

        String result = mvc.perform(patch("/items/{itemId}", 1)
                        .header("X-Sharer-User-Id", 1)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(itemDto, objectMapper.readValue(result, ItemDto.class));
        verify(itemService).update(itemDto, 1L, 1L);
    }


    @SneakyThrows
    @Test
    void findById() {
        Long itemAndUserId = 1L;
        when(itemService.getItemById(itemAndUserId, itemAndUserId)).thenReturn(itemFullDto);

        String result = mvc.perform(get("/items/{itemId}", 1)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(itemFullDto), result);
        verify(itemService).getItemById(itemAndUserId, itemAndUserId);
    }

    @SneakyThrows
    @Test
    void addCommentUserNotBookingItemValidationException() {
        when(itemService.createComment(commentDto, 1L, 5L)).thenThrow(ValidationException.class);

        mvc.perform(post("/items/{itemId}/comment", 1)
                        .header("X-Sharer-User-Id", 5)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void searchItems() {
        when(itemService.getItemsBySearchQuery("description", pageRequest)).thenReturn(List.of(itemDto));

        String result = mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "description")
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(List.of(itemDto)), result);
        verify(itemService).getItemsBySearchQuery("description", pageRequest);
    }


    @Test
    void getItemsByOwner() throws Exception {
        List<ItemWithBookingsDto> items = Arrays.asList(itemFullDto, itemFullDto);

        when(itemService.getItemsByOwnerId(owner.getId())).thenReturn(items);

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", owner.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(items)));

        verify(itemService).getItemsByOwnerId(owner.getId());
    }

    @Test
    void testAddComment() throws Exception {
        Long userId = 123L;
        Long itemId = 1L;
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Test comment")
                .authorName("Test Author")
                .created(LocalDateTime.now())
                .build();

        when(itemService.createComment(any(CommentDto.class), eq(itemId), eq(userId))).thenReturn(commentDto);

        mvc.perform(MockMvcRequestBuilders.post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Test comment\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(commentDto.getId()))
                .andExpect(jsonPath("$.text").value(commentDto.getText()))
                .andExpect(jsonPath("$.authorName").value(commentDto.getAuthorName()))
                .andExpect(jsonPath("$.created").exists());

        verify(itemService, times(1)).createComment(any(CommentDto.class), eq(itemId), eq(userId));
    }
}