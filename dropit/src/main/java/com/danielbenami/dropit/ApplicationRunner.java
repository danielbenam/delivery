package com.danielbenami.dropit;

import com.danielbenami.dropit.dto.AddressDto;
import com.danielbenami.dropit.dto.TimeslotRequestDto;
import com.danielbenami.dropit.entity.Address;
import com.danielbenami.dropit.entity.Timeslot;
import com.danielbenami.dropit.service.AddressService;
import com.danielbenami.dropit.service.TimeslotService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
public class ApplicationRunner implements CommandLineRunner {

    private final TimeslotService timeslotService;

    private final AddressService addressService;

    public ApplicationRunner(TimeslotService timeslotService, AddressService addressService) {
        this.timeslotService = timeslotService;
        this.addressService = addressService;
    }

    @Override
    public void run(String... args) throws Exception {
        CompletableFuture<List<LocalDate>> holidaysDatesFuture = timeslotService.findHolidays();
        CompletableFuture<List<TimeslotRequestDto>> timeSlotsFuture = timeslotService.readTimeSlots();
        CompletableFuture.allOf(holidaysDatesFuture, timeSlotsFuture).join();
        List<TimeslotRequestDto> timeslotsDto = timeSlotsFuture.get();
        List<LocalDate> holidaysDates = holidaysDatesFuture.get();
        timeslotsDto.removeIf(x -> timeslotService.isFallOnHolidays(x, holidaysDates));


        for (TimeslotRequestDto timeslotRequestDto : timeslotsDto) {
            Timeslot timeslot = timeslotService.convertDtoToEntity(timeslotRequestDto);
            for (AddressDto addressDto : timeslotRequestDto.getAddresses()) {
                Address address = addressService.findByFields(addressDto);
                if (address == null) {
                    Address newAddress = addressService.convertDtoToEntity(addressDto);
                    newAddress.addTimeSlot(timeslot);
                    timeslot.addAddress(newAddress);
                    addressService.save(newAddress);
                } else {
                    address.addTimeSlot(timeslot);
                    timeslot.addAddress(address);
                }
            }
            timeslotService.save(timeslot);
        }
    }
}
