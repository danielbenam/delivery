package com.danielbenami.dropit.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TimeslotDto {

    private Long id;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm")
    private LocalDateTime startTime;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm")
    private LocalDateTime endTime;
}
