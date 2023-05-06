package com.danielbenami.dropit.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TimeslotRequestDto {


    @JsonFormat(pattern="yyyy-MM-dd HH:mm")
    private LocalDateTime startTime;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm")
    private LocalDateTime endTime;

    private List<AddressDto> addresses;
}
