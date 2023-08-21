package ru.practicum.shareit.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemRequestService itemRequestService;

    @Autowired
    private MockMvc mvc;

    private static final String USER_ID = "X-Sharer-User-Id";

    private ItemRequestDto itemRequestDto = new ItemRequestDto(1L, "ItemRequest description",
            LocalDateTime.of(2022, 1, 2, 3, 4, 5), null);

    @SneakyThrows
    @Test
    void addRequest() {
        when(itemRequestService.add(itemRequestDto, 1L)).thenReturn(itemRequestDto);

        String result = mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(mapper.writeValueAsString(itemRequestDto), result);
    }


    @SneakyThrows
    @Test
    void findAllByOwner_whenInvoked_thenResponseStatusOkWithRequestsCollectionInBody() {
        List<ItemRequestDto> expected = List.of(itemRequestDto);
        when(itemRequestService.findAllByOwnerId(1L)).thenReturn(expected);

        String result = mvc.perform(get("/requests")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemRequestService).findAllByOwnerId(1L);
        assertThat(mapper.writeValueAsString(expected), equalTo(result));
    }

    @SneakyThrows
    @Test
    void findAll_whenInvoked_thenResponseStatusOkWithRequestsCollectionInBody() {
        List<ItemRequestDto> expected = List.of(itemRequestDto);
        PageRequest pageRequest = PageRequest.of(0 / 20, 20);
        when(itemRequestService.findAll(1L, pageRequest)).thenReturn(expected);

        String result = mvc.perform(get("/requests/all")
                        .header(USER_ID, 1)
                        .queryParam("from", "0")
                        .queryParam("size", "20"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemRequestService).findAll(1L, pageRequest);
        assertThat(mapper.writeValueAsString(expected), equalTo(result));
    }

    @SneakyThrows
    @Test
    void getRequestById() {
        ItemRequestDto expected = new ItemRequestDto(1L, "ItemRequest description",
                LocalDateTime.of(2022, 1, 2, 3, 4, 5), null);

        when(itemRequestService.findById(1L, 1L)).thenReturn(expected);

        mvc.perform(get("/requests/{requestId}", 1)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expected)));

        verify(itemRequestService).findById(1L, 1L);
    }
}