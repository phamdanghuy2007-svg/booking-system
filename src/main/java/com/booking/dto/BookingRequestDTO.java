package com.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequestDTO {

    @NotBlank(message = "Customer name must not be blank")
    @Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters")
    private String customerName;

    @NotBlank(message = "Customer email must not be blank")
    @Email(message = "Invalid email format")
    private String customerEmail;

    @NotBlank(message = "Customer phone must not be blank")
    private String customerPhone;

    @NotBlank(message = "Service name must not be blank")
    private String serviceName;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    @NotNull(message = "Total price is required")
    @PositiveOrZero(message = "Total price must be zero or positive")
    private BigDecimal totalPrice;

    @Size(max = 500, message = "Note cannot exceed 500 characters")
    private String note;
}

