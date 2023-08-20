package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private final long id = 1L;
    private final LocalDateTime start = LocalDateTime.now().plusHours(1);
    private final LocalDateTime end = LocalDateTime.now().plusDays(1);
    private final User booker = new User(1L, "booker", "booker@email.ru");
    private final User owner = new User(2L, "owner", "owner@email.ru");
    private final Item item = new Item(1L, "item", "desc", true, owner, null);
    private final Booking booking = new Booking(id, start, end, item, booker, Status.WAITING);
    private final BookingInputDto bookingInputDto = new BookingInputDto(1L, start, end);
    private final UserDto bookerDto = UserMapper.toUserDto(booker);
    private final ItemDto itemDto = ItemMapper.toItemDto(item);
    private final BookingDto bookingDto = new BookingDto(id, start, end, itemDto, bookerDto, Status.WAITING);
    private final Sort sortDesc = Sort.by(Sort.Direction.DESC, "start");
    private final Pageable pageable = PageRequest.of(0 / 20, 20, sortDesc);

    @Test
    @Transactional
    void testFindByOwnerAndStatusWaiting() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto actual = bookingService.add(1L, bookingInputDto);

        assertThat(actual.getStart(), equalTo(bookingDto.getStart()));
        assertThat(actual.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(actual.getItem(), equalTo(bookingDto.getItem()));
        assertThat(actual.getBooker(), equalTo(bookingDto.getBooker()));
        assertThat(actual.getStatus(), equalTo(Status.WAITING));
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @Transactional
    void add_whenBookingInputDtoStartIsAfterEnd_thenValidationException() {
        bookingInputDto.setEnd(start.minusHours(1));

        assertThrows(ValidationException.class, () -> bookingService.add(1L, bookingInputDto));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @Transactional
    void add_whenBookingInputDtoStartEqualsEnd_thenValidationException() {
        bookingInputDto.setEnd(start);

        assertThrows(ValidationException.class, () -> bookingService.add(1L, bookingInputDto));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @Transactional
    void add_whenBookerIdEqualsItemOwnerId_thenBookingNotFoundException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(BookingNotFoundException.class, () -> bookingService.add(2L, bookingInputDto));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @Transactional
    void add_whenItemIsNotAvailable_thenValidationException() {
        item.setAvailable(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.add(1L, bookingInputDto));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @Transactional
    void findById_whenUserIsBooker_thenReturnedBooking() {
        when(bookingRepository.findById(id)).thenReturn(Optional.of(booking));

        BookingDto actual = bookingService.getBookingById(1L, id);

        assertThat(actual.getStart(), equalTo(bookingDto.getStart()));
        assertThat(actual.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(actual.getItem(), equalTo(bookingDto.getItem()));
        assertThat(actual.getBooker(), equalTo(bookingDto.getBooker()));
    }

    @Test
    @Transactional
    void findByIdUserIsOwnerItem_ReturnedBooking() {
        when(bookingRepository.findById(id)).thenReturn(Optional.of(booking));

        BookingDto actual = bookingService.getBookingById(2L, id);

        assertThat(actual.getStart(), equalTo(bookingDto.getStart()));
        assertThat(actual.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(actual.getItem(), equalTo(bookingDto.getItem()));
        assertThat(actual.getBooker(), equalTo(bookingDto.getBooker()));
    }

    @Test
    @Transactional
    void findByIdUserNotBookerOrNotItemOwner_BookingNotFoundException() {
        when(bookingRepository.findById(id)).thenReturn(Optional.of(booking));

        assertThrows(BookingNotFoundException.class, () ->
                bookingService.getBookingById(3L, id));
    }

    @Test
    @Transactional
    void findByIdBooking_BookingNotFoundException() {
        assertThrows(BookingNotFoundException.class, () ->
                bookingService.getBookingById(3L, id));
    }

    @Test
    @Transactional
    void findBookingsByOwnerStateAllReturnedAllBookings() {
        Page<Booking> bookings = new PageImpl<>(List.of(booking));
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItem_Owner_Id(2L, pageable)).thenReturn(bookings);

        List<BookingDto> actual = bookingService.getAllBookingsForOwner(2L, "ALL", 0, 20);

        assertThat(actual.get(0).toString(), equalTo(bookingDto.toString()));
    }

    @Test
    @Transactional
    void findBookingsByOwnerReturnedCurrent() {
        Page<Booking> bookings = new PageImpl<>(List.of(booking));
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(anyLong(),
                any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class))).thenReturn(bookings);

        List<BookingDto> actual = bookingService.getAllBookingsForOwner(2L, "CURRENT", 0, 20);

        assertThat(actual.get(0).toString(), equalTo(bookingDto.toString()));
        verify(bookingRepository).findByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(anyLong(),
                any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    @Transactional
    void findBookingsByOwnerPastReturnedByOwnerId() {
        Page<Booking> bookings = new PageImpl<>(List.of(booking));
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItem_Owner_IdAndEndIsBefore(
                anyLong(), any(LocalDateTime.class), any(Pageable.class))).thenReturn(bookings);

        List<BookingDto> actual = bookingService.getAllBookingsForOwner(2L, "PAST", 0, 20);
        ;

        assertThat(actual.get(0).toString(), equalTo(bookingDto.toString()));
        verify(bookingRepository).findByItem_Owner_IdAndEndIsBefore(
                anyLong(), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    @Transactional
    void testFindByOwnerAndEndBefore() {
        Page<Booking> bookings = new PageImpl<>(List.of(booking));
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItem_Owner_IdAndStartIsAfter(
                anyLong(), any(LocalDateTime.class), any(Pageable.class))).thenReturn(bookings);

        List<BookingDto> actual = bookingService.getAllBookingsForOwner(2L, "FUTURE", 0, 20);
        ;

        assertThat(actual.get(0).toString(), equalTo(bookingDto.toString()));
        verify(bookingRepository).findByItem_Owner_IdAndStartIsAfter(
                anyLong(), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    @Transactional
    void testFindByOwnerAndStartAfter() {
        booking.setStatus(Status.WAITING);
        bookingDto.setStatus(Status.WAITING);
        Page<Booking> bookings = new PageImpl<>(List.of(booking));
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItem_Owner_IdAndStatus(
                2L, Status.WAITING, pageable)).thenReturn(bookings);

        List<BookingDto> actual = bookingService.getAllBookingsForOwner(2L, "WAITING", 0, 20);
        ;

        assertThat(actual.get(0).toString(), equalTo(bookingDto.toString()));
        verify(bookingRepository).findByItem_Owner_IdAndStatus(
                2L, Status.WAITING, pageable);
    }

    @Test
    @Transactional
    void testFindByOwnerAndStatusRejected() {
        booking.setStatus(Status.REJECTED);
        bookingDto.setStatus(Status.REJECTED);
        Page<Booking> bookings = new PageImpl<>(List.of(booking));
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItem_Owner_IdAndStatus(
                2L, Status.REJECTED, pageable)).thenReturn(bookings);

        List<BookingDto> actual = bookingService.getAllBookingsForOwner(2L, "REJECTED", 0, 20);

        assertThat(actual.get(0).toString(), equalTo(bookingDto.toString()));
        verify(bookingRepository).findByItem_Owner_IdAndStatus(2L, Status.REJECTED, pageable);
    }
}