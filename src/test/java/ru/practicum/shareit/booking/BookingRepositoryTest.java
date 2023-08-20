package ru.practicum.shareit.booking;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;

import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


@DataJpaTest
@AutoConfigureTestDatabase
public class BookingRepositoryTest {

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private final LocalDateTime start = LocalDateTime.now().plusHours(1);
    private final LocalDateTime end = LocalDateTime.now().plusDays(1);
    private User booker;
    private User owner;
    private Item item;
    private Booking booking;
    Sort sortDesc = Sort.by(Sort.Direction.DESC, "start");
    Pageable pageable = PageRequest.of(0 / 20, 20, sortDesc);
    @BeforeEach
    void beforeEach() {
        booker = userRepository.save(new User(1L, "booker", "booker@email.ru"));
        owner = userRepository.save(new User(2L, "owner", "owner@email.ru"));
        item = itemRepository.save(
                new Item(1L, "item", "desc", true, owner, null));
        booking = bookingRepository.save(new Booking(1L, start, end, item, booker, Status.REJECTED));
    }

    @AfterEach
    void afterEach() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    void findByItem_Owner_IdAndStatusIsWaiting() {
        booking.setStatus(Status.WAITING);
        bookingRepository.save(booking);
        List<Booking> actual = bookingRepository.findByItem_Owner_IdAndStatus(
                owner.getId(), Status.WAITING, pageable).toList();

        assertThat(actual.size(), equalTo(1));
        assertThat(actual.get(0).getBooker(), equalTo(booking.getBooker()));
        assertThat(actual.get(0).getStatus(), equalTo(booking.getStatus()));
    }

    @Test
    @Transactional
    void findByItem_Owner_IdAndStatusIsApproved() {
        booking.setStatus(Status.APPROVED);
        bookingRepository.save(booking);
        List<Booking> actual = bookingRepository.findByItem_Owner_IdAndStatus(
                owner.getId(), Status.APPROVED, pageable).toList();

        assertThat(actual.size(), equalTo(1));
        assertThat(actual.get(0).getBooker(), equalTo(booking.getBooker()));
        assertThat(actual.get(0).getStatus(), equalTo(booking.getStatus()));
    }

    @Test
    @Transactional
    void findByItem_Owner_IdAndStatusIsCanceled() {
        booking.setStatus(Status.CANCELED);
        bookingRepository.save(booking);
        List<Booking> actual = bookingRepository.findByItem_Owner_IdAndStatus(
                owner.getId(), Status.CANCELED, pageable).toList();

        assertThat(actual.size(), equalTo(1));
        assertThat(actual.get(0).getBooker(), equalTo(booking.getBooker()));
        assertThat(actual.get(0).getStatus(), equalTo(booking.getStatus()));
    }

    @Test
    @Transactional
    void findFirstByItem_IdAndStartBeforeOrderByEndDesc() {
        booking.setStart(start.minusMinutes(30));
        bookingRepository.save(booking);
        Booking actual = bookingRepository.findFirstByItem_IdAndStartBeforeOrderByEndDesc(
                item.getId(), start);

        assertThat(actual.getItem(), equalTo(booking.getItem()));
        assertThat(actual.getBooker(), equalTo(booking.getBooker()));
    }

    @Test
    @Transactional
    void findFirstByItem_IdAndStartAfterAndStatusApprovedOrderByStartAsc() {
        booking.setStart(start.plusHours(1));
        booking.setStatus(Status.APPROVED);
        bookingRepository.save(booking);
        Booking actual = bookingRepository.findFirstByItem_IdAndStartAfterAndStatusOrderByStartAsc(
                item.getId(), start, Status.APPROVED);

        assertThat(actual.getItem(), equalTo(booking.getItem()));
        assertThat(actual.getBooker(), equalTo(booking.getBooker()));
    }

    @Test
    @Transactional
    void findFirstByItem_IdAndBooker_IdAndEndIsBeforeAndStatusApproved() {
        booking.setEnd(end.minusHours(1));
        booking.setStatus(Status.APPROVED);
        bookingRepository.save(booking);
        Booking actual = bookingRepository.findFirstByItem_IdAndBooker_IdAndEndIsBeforeAndStatus(
                item.getId(), booker.getId(), end, Status.APPROVED);

        assertThat(actual.getItem(), equalTo(booking.getItem()));
        assertThat(actual.getBooker(), equalTo(booking.getBooker()));
    }

    @Test
    @Transactional
    void findByItem_Owner_Id() {
        List<Booking> actual = bookingRepository.findByItem_Owner_Id(
                owner.getId(), pageable).toList();

        assertThat(actual.size(), equalTo(1));
        assertThat(actual.get(0).getBooker(), equalTo(booking.getBooker()));
    }

    @Test
    @Transactional
    void findByItem_Owner_IdAndStartIsBeforeAndEndIsAfter() {
        booking.setStart(start.minusMinutes(30));
        booking.setEnd(end.plusDays(1));
        bookingRepository.save(booking);
        List<Booking> actual = bookingRepository.findByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(
                owner.getId(), start, end, pageable).toList();

        assertThat(actual.size(), equalTo(1));
        assertThat(actual.get(0).getBooker(), equalTo(booking.getBooker()));
    }

    @Test
    @Transactional
    void findByItem_Owner_IdAndEndIsBefore() {
        booking.setEnd(end.minusHours(1));
        bookingRepository.save(booking);
        List<Booking> actual = bookingRepository.findByItem_Owner_IdAndEndIsBefore(
                owner.getId(), end, pageable).toList();

        assertThat(actual.size(), equalTo(1));
        assertThat(actual.get(0).getBooker(), equalTo(booking.getBooker()));
    }

    @Test
    @Transactional
    void findByItem_Owner_IdAndStartIsAfter() {
        booking.setStart(start.plusMinutes(30));
        bookingRepository.save(booking);
        List<Booking> actual = bookingRepository.findByItem_Owner_IdAndStartIsAfter(
                owner.getId(), start, pageable).toList();

        assertThat(actual.size(), equalTo(1));
        assertThat(actual.get(0).getBooker(), equalTo(booking.getBooker()));
    }

    @Test
    @Transactional
    void findByItem_Owner_IdAndStatusIsRejected() {
        List<Booking> actual = bookingRepository.findByItem_Owner_IdAndStatus(
                owner.getId(), Status.REJECTED, pageable).toList();

        assertThat(actual.size(), equalTo(1));
        assertThat(actual.get(0).getBooker(), equalTo(booking.getBooker()));
        assertThat(actual.get(0).getStatus(), equalTo(booking.getStatus()));
    }
}
