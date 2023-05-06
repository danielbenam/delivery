package com.danielbenami.dropit.service;

import com.danielbenami.dropit.dto.AddressDto;
import com.danielbenami.dropit.dto.TimeslotDto;
import com.danielbenami.dropit.dto.TimeslotRequestDto;
import com.danielbenami.dropit.dto.TimeslotResponseDto;
import com.danielbenami.dropit.entity.Address;
import com.danielbenami.dropit.entity.Delivery;
import com.danielbenami.dropit.entity.Timeslot;
import com.danielbenami.dropit.enums.DeliveryStatus;
import com.danielbenami.dropit.exception.AddressNotFoundException;
import com.danielbenami.dropit.exception.DeliveryWasAlreadyCompletedException;
import com.danielbenami.dropit.repository.DeliveryRepository;
import com.danielbenami.dropit.repository.TimeslotRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Service
public class TimeslotService {

    @Value("${holiday.api}")
    private String holidayApi;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    private final TimeslotRepository timeslotRepository;

    private final AddressService addressService;

    private final DeliveryRepository deliveryRepository;


    public TimeslotService(RestTemplate restTemplate, ObjectMapper objectMapper, TimeslotRepository timeslotRepository, AddressService addressService, DeliveryRepository deliveryRepository) {
        this.restTemplate = restTemplate;
        this.mapper = objectMapper;
        this.timeslotRepository = timeslotRepository;
        this.addressService = addressService;
        this.deliveryRepository = deliveryRepository;
    }

    @Async
    public CompletableFuture<List<LocalDate>> findHolidays() throws JsonProcessingException {
        String response = restTemplate.getForObject(holidayApi, String.class);
        JsonNode jsonNode = mapper.readTree(response);
        JsonNode holidays = jsonNode.get("holidays");
        List<LocalDate> holidaysDates = IntStream.range(0, holidays.size())
                .mapToObj(i -> holidays.get(i).get("date").asText())
                .map(LocalDate::parse)
                .toList();
        return CompletableFuture.completedFuture(holidaysDates);
    }

    @Async
    public CompletableFuture<List<TimeslotRequestDto>> readTimeSlots() throws IOException {
        TypeReference<List<TimeslotRequestDto>> typeReference = new TypeReference<>() {
        };
        InputStream inputStream = TypeReference.class.getResourceAsStream("/json/timeSlots.json");
        List<TimeslotRequestDto> timeslots = mapper.readValue(inputStream, typeReference);
        return CompletableFuture.completedFuture(timeslots);
    }

    public boolean isFallOnHolidays(TimeslotRequestDto x, List<LocalDate> holidaysDates) {
        return holidaysDates.contains(x.getStartTime().toLocalDate()) || holidaysDates.contains(x.getEndTime().toLocalDate());
    }

    public Timeslot convertDtoToEntity(TimeslotRequestDto timeslotRequestDto) {
        Timeslot timeslot = new Timeslot();
        timeslot.setStartTime(timeslotRequestDto.getStartTime());
        timeslot.setEndTime(timeslotRequestDto.getEndTime());
        return timeslot;
    }

    public TimeslotDto convertEntityToDto(Timeslot timeslot) {
        TimeslotDto timeSlotDto = new TimeslotDto();
        timeSlotDto.setId(timeslot.getId());
        timeSlotDto.setStartTime(timeslot.getStartTime());
        timeSlotDto.setEndTime(timeslot.getEndTime());
        return timeSlotDto;
    }

    public Timeslot save(Timeslot timeslot) {
        return timeslotRepository.save(timeslot);
    }

    public TimeslotResponseDto getAllTimeSlotsForAddress(AddressDto addressDto) {
        Address address = addressService.findByFields(addressDto);
        if (address == null) {
            throw new AddressNotFoundException("address not found");
        }
        final Delivery delivery = address.getDelivery();
        if (delivery != null && delivery.getStatus().equals(DeliveryStatus.COMPLETED)) {
            throw new DeliveryWasAlreadyCompletedException("delivery has already arrived to address between " + delivery.getTimeslot().getStartTime() + " to " + delivery.getTimeslot().getEndTime() +
                    "  There no available timeslots");
        }

        final List<Timeslot> timeslots = timeslotRepository.findAllByAddresses(address).stream().
                filter(this::isValidTimeslot).
                filter(this::isLessThanTenDeliveriesPerDay).
                collect(Collectors.toList());

        if (delivery != null && delivery.getStatus().equals(DeliveryStatus.CREATED)) {
            timeslots.removeIf(timeslot -> timeslot.getId().equals(delivery.getTimeslot().getId()));
        }

        final List<TimeslotDto> timeslotDtos = timeslots.stream().map(this::convertEntityToDto).toList();
        return new TimeslotResponseDto(timeslotDtos);
    }

    private boolean isValidTimeslot(Timeslot timeslot) {
        return computeDeliveriesForTimeSlot(timeslot).size() < 2;
    }

    private boolean isLessThanTenDeliveriesPerDay(Timeslot timeslot) {
        final LocalDate deliveryDay = timeslot.getStartTime().toLocalDate();
        final List<Delivery> deliveries = DeliveryService.getDeliveriesPerDayMap().get(deliveryDay);
        return deliveries == null || deliveries.size() < 10;
    }

    private List<Delivery> computeDeliveriesForTimeSlot(Timeslot timeslot) {
        return deliveryRepository.findAllByTimeslot(timeslot).
                stream().filter(TimeslotService::isNotCanceledDelivery).
                toList();
    }

    private static boolean isNotCanceledDelivery(Delivery delivery) {
        return !delivery.getStatus().equals(DeliveryStatus.CANCELED);
    }


    public Optional<Timeslot> getTimeslotBy(long id) {
        return timeslotRepository.findById(id);
    }
}
