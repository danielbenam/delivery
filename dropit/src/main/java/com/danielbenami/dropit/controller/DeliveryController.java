package com.danielbenami.dropit.controller;

import com.danielbenami.dropit.dto.*;
import com.danielbenami.dropit.entity.Delivery;
import com.danielbenami.dropit.service.AddressService;
import com.danielbenami.dropit.service.DeliveryService;
import com.danielbenami.dropit.service.TimeslotService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
public class DeliveryController {

    private final AddressService addressService;
    private final TimeslotService timeslotService;
    private final DeliveryService deliveryService;


    public DeliveryController(AddressService addressService, TimeslotService timeslotService, DeliveryService deliveryService) {
        this.addressService = addressService;
        this.timeslotService = timeslotService;
        this.deliveryService = deliveryService;
    }

    @PostMapping("/resolve-address")
    public AddressDto resolveAddress(@RequestParam String text) throws JsonProcessingException {
        AddressDto addressDto = addressService.resolveAddress(text);
        return addressDto;
    }


    @PostMapping("/timeslots")
    public TimeslotResponseDto getAllTimeSlotsForAddress(@Valid @RequestBody AddressDto addressDto) throws JsonProcessingException {
        return timeslotService.getAllTimeSlotsForAddress(addressDto);
    }

    @PostMapping("/deliveries")
    public DeliveryDto createDelivery(@Valid @RequestBody DeliveryRequestDto deliveryRequestDto) {
        final Delivery delivery = deliveryService.createDelivery(deliveryRequestDto);
        final DeliveryDto deliveryDto = deliveryService.convertEntityToDto(delivery);
        return deliveryDto;
    }

    @GetMapping("/deliveries/weekly")
    public DeliveryResponseDto getAllDeliveries() {
        return deliveryService.getAllDeliveries();
    }


    @GetMapping("/deliveries/daily")
    public DeliveryResponseDto getAllTodayDeliveries(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate date) {
        return deliveryService.getAllTodayDeliveries(date);
    }


    @PostMapping("/deliveries/{id}/complete")
    public DeliveryDto completeDelivery(@PathVariable Long id) {
        final Delivery delivery = deliveryService.completeDelivery(id);
        final DeliveryDto deliveryDto = deliveryService.convertEntityToDto(delivery);
        return deliveryDto;
    }


    @DeleteMapping("/deliveries/{id}")
    public DeliveryDto cancelDelivery(@PathVariable Long id) {
        final Delivery delivery = deliveryService.cancelDelivery(id);
        final DeliveryDto deliveryDto = deliveryService.convertEntityToDto(delivery);
        return deliveryDto;
    }


}
