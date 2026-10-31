package com.booking.repository;

import com.booking.entity.Booking;
import com.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomerEmail(String customerEmail);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}

