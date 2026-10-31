package com.booking.dto;

import com.booking.entity.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponseDTO {

    private Long id;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String serviceName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

