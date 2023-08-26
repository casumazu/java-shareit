package ru.practicum.shareit.item;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private final User booker = new User(2L, "booker", "booker@email.ru");
    private final LocalDateTime created = LocalDateTime.now();
    private final User owner = new User(1L, "owner", "owner@email.ru");
    private final Item item = new Item(1L, "item", "description", true, owner, null);
    private final ItemDto itemDto = new ItemDto(
            1L, "item", "description", true, null, null);
    private final CommentDto commentDto = new CommentDto(
            1L, "comment", "booker", created);
    private final LocalDateTime start1 = LocalDateTime.now().minusDays(1);
    private final LocalDateTime end1 = LocalDateTime.now().minusHours(5);
    private final LocalDateTime start2 = LocalDateTime.now().plusHours(1);
    private final LocalDateTime end2 = LocalDateTime.now().plusDays(1);
    private final Booking lastBooking = new Booking(1L, start1, end1, item, booker, Status.APPROVED);
    private final Booking nextBooking = new Booking(2L, start2, end2, item, booker, Status.APPROVED);
    private final PageRequest pageRequest = PageRequest.of(0 / 20, 20);
    private final Comment comment = new Comment(
            1L, "comment", item, booker, created);
    private final ItemWithBookingsDto itemFullDto = new ItemWithBookingsDto(1L, "item", "description",
            true, null, owner, BookingMapper.toBookingShortDto(lastBooking),
            BookingMapper.toBookingShortDto(nextBooking), List.of(commentDto));

    @Test
    void createItem() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto actual = itemService.create(itemDto, owner.getId());

        assertThat(actual.getName(), equalTo(itemDto.getName()));
        assertThat(actual.getDescription(), equalTo(itemDto.getDescription()));
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void updateItem() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        item.setName("name");

        itemDto.setName("name");
        itemDto.setDescription(null);
        itemDto.setAvailable(null);

        when(itemRepository.save(item)).thenReturn(item);

        ItemDto actual = itemService.update(itemDto, item.getId(), owner.getId());

        assertThat(actual.getName(), equalTo(itemDto.getName()));
        assertThat(actual.getDescription(), equalTo(item.getDescription()));
        assertThat(actual.getAvailable(), equalTo(item.getAvailable()));

        verify(itemRepository).save(item);
    }

    @Test
    void updateItemItemNotFoundException() {
        assertThrows(ItemNotFoundException.class, () -> itemService.update(itemDto, item.getId(), 3L));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void getItemByWrongId() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByItem_IdAndStartBeforeOrderByEndDesc(anyLong(),
                any(LocalDateTime.class))).thenReturn(lastBooking);
        when(bookingRepository.findFirstByItem_IdAndStartAfterAndStatusOrderByStartAsc(anyLong(),
                any(LocalDateTime.class), eq(Status.APPROVED))).thenReturn(nextBooking);
        when(commentRepository.findByItemId(item.getId())).thenReturn(List.of(comment));

        ItemWithBookingsDto actual = itemService.getItemById(item.getId(), owner.getId());

        assertThat(actual.getName(), equalTo(itemFullDto.getName()));
        assertThat(actual.getComments(), equalTo(itemFullDto.getComments()));
        assertThat(actual.getLastBooking(), equalTo(itemFullDto.getLastBooking()));
        assertThat(actual.getNextBooking(), equalTo(itemFullDto.getNextBooking()));
    }

    @Test
    void findById_whenUserIdEqualsItemOwnerId_thenReturnedItemFullDtoWithInfoAboutBookings() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByItem_IdAndStartBeforeOrderByEndDesc(anyLong(),
                any(LocalDateTime.class))).thenReturn(lastBooking);
        when(bookingRepository.findFirstByItem_IdAndStartAfterAndStatusOrderByStartAsc(anyLong(),
                any(LocalDateTime.class), eq(Status.APPROVED))).thenReturn(nextBooking);
        when(commentRepository.findByItemId(item.getId())).thenReturn(List.of(comment));

        ItemWithBookingsDto actual = itemService.getItemById(item.getId(), owner.getId());

        assertThat(actual.getName(), equalTo(itemFullDto.getName()));
        assertThat(actual.getComments(), equalTo(itemFullDto.getComments()));
        assertThat(actual.getLastBooking(), equalTo(itemFullDto.getLastBooking()));
        assertThat(actual.getNextBooking(), equalTo(itemFullDto.getNextBooking()));
    }

    @Test
    void searchItems_whenTextIsBlank_thenReturnedCollectionIsEmpty() {
        List<ItemDto> actual = itemService.getItemsBySearchQuery("", pageRequest);

        assertThat(actual.size(), equalTo(0));
    }

    @Test
    void addComment_whenUserHasBookingItem_thenReturnedNewCommentDto() {
        when(bookingRepository.findFirstByItem_IdAndBooker_IdAndEndIsBeforeAndStatus(anyLong(),
                eq(booker.getId()), any(LocalDateTime.class), eq(Status.APPROVED))).thenReturn(lastBooking);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto actual = itemService.createComment(commentDto, item.getId(), booker.getId());

        assertThat(actual.getText(), equalTo(commentDto.getText()));
        assertThat(actual.getAuthorName(), equalTo(commentDto.getAuthorName()));
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addCommentUserDidNotBookingItemValidationException() {
        when(bookingRepository.findFirstByItem_IdAndBooker_IdAndEndIsBeforeAndStatus(anyLong(),
                eq(3L), any(LocalDateTime.class), eq(Status.APPROVED))).thenReturn(null);

        assertThrows(ValidationException.class, () ->
                itemService.createComment(commentDto, item.getId(), 3L));
    }

    @Test
    void findById_UserIdNotEqualsItemOwnerIdItemFullDtoWithoutInfoAboutBookings() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(item.getId())).thenReturn(List.of(comment));

        ItemWithBookingsDto actual = itemService.getItemById(item.getId(), 3L);

        assertThat(actual.getName(), equalTo(itemFullDto.getName()));
        assertThat(actual.getComments(), equalTo(itemFullDto.getComments()));
        assertThat(actual.getLastBooking(), equalTo(null));
        assertThat(actual.getNextBooking(), equalTo(null));
    }

    @Test
    void testGetItemsByOwnerId() {
        when(itemRepository.findAllByOwnerId(owner.getId())).thenReturn(List.of(item));
        when(bookingRepository.findFirstByItem_IdAndStartBeforeOrderByEndDesc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(lastBooking);
        when(bookingRepository.findFirstByItem_IdAndStartAfterAndStatusOrderByStartAsc(
                anyLong(), any(LocalDateTime.class), eq(Status.APPROVED))).thenReturn(nextBooking);
        when(commentRepository.findByItemId(item.getId())).thenReturn(List.of(comment));

        List<ItemWithBookingsDto> items = itemService.getItemsByOwnerId(owner.getId());

        assertEquals(1, items.size());
        assertEquals(itemDto.getName(), items.get(0).getName());
        assertEquals(itemFullDto.getLastBooking(), items.get(0).getLastBooking());
        assertEquals(itemFullDto.getNextBooking(), items.get(0).getNextBooking());
        assertEquals(itemFullDto.getComments(), items.get(0).getComments());
    }

    @Test
    void testGetItemsBySearchQueryWithBlankText() {
        List<ItemDto> result = itemService.getItemsBySearchQuery("", PageRequest.of(0, 20));

        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateCommentWithoutBooking() {
        when(bookingRepository.findFirstByItem_IdAndBooker_IdAndEndIsBeforeAndStatus(
                anyLong(), anyLong(), any(LocalDateTime.class), eq(Status.APPROVED))).thenReturn(null);

        assertThrows(ValidationException.class, () ->
                itemService.createComment(commentDto, item.getId(), 3L));
    }

    @Test
    void testGetItemByIdForOtherUser() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(item.getId())).thenReturn(List.of(comment));

        ItemWithBookingsDto result = itemService.getItemById(item.getId(), 3L);

        assertEquals(itemDto.getName(), result.getName());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertEquals(itemFullDto.getComments(), result.getComments());
    }

    @Test
    void testGetItemByIdForOwner() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByItem_IdAndStartBeforeOrderByEndDesc(
                anyLong(), any(LocalDateTime.class))).thenReturn(lastBooking);
        when(bookingRepository.findFirstByItem_IdAndStartAfterAndStatusOrderByStartAsc(
                anyLong(), any(LocalDateTime.class), eq(Status.APPROVED))).thenReturn(nextBooking);
        when(commentRepository.findByItemId(item.getId())).thenReturn(List.of(comment));

        ItemWithBookingsDto result = itemService.getItemById(item.getId(), owner.getId());

        assertEquals(itemFullDto.getName(), result.getName());
        assertEquals(itemFullDto.getLastBooking(), result.getLastBooking());
        assertEquals(itemFullDto.getNextBooking(), result.getNextBooking());
        assertEquals(itemFullDto.getComments(), result.getComments());
    }
}