package com.booking.dto;

import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotDTO {

    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;
}
