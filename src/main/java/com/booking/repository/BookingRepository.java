package com.booking.repository;

import com.booking.entity.Booking;
import com.booking.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomerEmail(String customerEmail);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByStaffId(Long staffId);

    List<Booking> findByStaffIdAndStartTimeBetween(Long staffId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
           "WHERE b.staffId = :staffId " +
           "AND b.status != :cancelledStatus " +
           "AND b.startTime < :newEndTime " +
           "AND b.endTime > :newStartTime")
    boolean isStaffBusy(
            @Param("staffId") Long staffId,
            @Param("newStartTime") LocalDateTime newStartTime,
            @Param("newEndTime") LocalDateTime newEndTime,
            @Param("cancelledStatus") BookingStatus cancelledStatus);

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
           "WHERE b.staffId = :staffId " +
           "AND b.id != :excludeId " +
           "AND b.status != :cancelledStatus " +
           "AND b.startTime < :newEndTime " +
           "AND b.endTime > :newStartTime")
    boolean isStaffBusyExcluding(
            @Param("staffId") Long staffId,
            @Param("excludeId") Long excludeId,
            @Param("newStartTime") LocalDateTime newStartTime,
            @Param("newEndTime") LocalDateTime newEndTime,
            @Param("cancelledStatus") BookingStatus cancelledStatus);

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    Page<Booking> findByStaffId(Long staffId, Pageable pageable);

    Page<Booking> findByStaffIdAndStatus(Long staffId, BookingStatus status, Pageable pageable);
}
