package com.danielbenami.dropit.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DeliveryRequestDto {

    @Valid
    private User user;

    @NotNull(message = "timeslotId must not be null")
    @Min(value=1, message="timeslotId must be equal or greater than 1")
    private Long timeslotId;


}
