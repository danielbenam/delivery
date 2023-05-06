package com.danielbenami.dropit.dto;

import com.danielbenami.dropit.entity.Timeslot;
import com.danielbenami.dropit.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DeliveryDto {

    private Long id;
    private DeliveryStatus status;
    private TimeslotDto timeslot;
    private AddressDto address;
}
