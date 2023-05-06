package com.danielbenami.dropit.service;

import com.danielbenami.dropit.dto.*;
import com.danielbenami.dropit.entity.Address;
import com.danielbenami.dropit.entity.Delivery;
import com.danielbenami.dropit.entity.Timeslot;
import com.danielbenami.dropit.enums.DeliveryStatus;
import com.danielbenami.dropit.exception.*;
import com.danielbenami.dropit.repository.DeliveryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@Service
public class DeliveryService {


    private final DeliveryRepository deliveryRepository;

    private final TimeslotService timeslotService;

    private final AddressService addressService;

    private final static Map<LocalDate, List<Delivery>> deliveriesPerDayMap = new HashMap<>();


    public DeliveryService(DeliveryRepository deliveryRepository, TimeslotService timeslotService, AddressService addressService) {
        this.deliveryRepository = deliveryRepository;
        this.timeslotService = timeslotService;
        this.addressService = addressService;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Delivery createDelivery(DeliveryRequestDto deliveryRequestDto) {
        validateDelivery(deliveryRequestDto);
        final Delivery delivery = convertDtoToEntity(deliveryRequestDto);
        addDeliveryToDeliveriesPerDayMap(delivery);
        timeslotService.save(delivery.getTimeslot());
        return deliveryRepository.save(delivery);
    }

    private void validateDelivery(DeliveryRequestDto deliveryRequestDto) {
        final Long timeslotId = deliveryRequestDto.getTimeslotId();
        final Timeslot timeslot = timeslotService.getTimeslotBy(timeslotId).orElseThrow(() -> new TimeslotNotFoundException("timeslot not found with id : " + timeslotId));

        final AddressDto address = deliveryRequestDto.getUser().getAddress();
        if (addressService.findByFields(address) == null) {
            throw new AddressNotFoundException("address not found");
        }

        final Delivery delivery = addressService.findByFields(address).getDelivery();
        if (delivery != null) {
            final LocalDateTime startTime = delivery.getTimeslot().getStartTime();
            final LocalDateTime endTime = delivery.getTimeslot().getEndTime();

            if (delivery.getStatus().equals(DeliveryStatus.CREATED)) {
                throw new AddressAlreadyAssignedToTimeslotException("delivery will be arrived to address between   " + startTime + "   to   " + endTime +
                        "  you can cancel the delivery and create new one, please check your available timeslot before cancelling ");
            }
            if (delivery.getStatus().equals(DeliveryStatus.COMPLETED)) {
                throw new DeliveryWasAlreadyCompletedException("delivery has already arrived to address between " + startTime + " to " + endTime);
            }
        }


        TimeslotResponseDto timeSlotsForAddress = timeslotService.getAllTimeSlotsForAddress(address);

        if (timeSlotsForAddress.getTimeSlots().stream().map(TimeslotDto::getId).noneMatch(x -> x.equals(timeslotId))) {
            throw new AddressNotSupportedByTimeslotException("Address can't delivered with timeslot : " + timeslotId);
        }

    }


    public Delivery convertDtoToEntity(DeliveryRequestDto deliveryRequestDto) {
        Delivery delivery = new Delivery();
        final Timeslot timeslot = timeslotService.getTimeslotBy(deliveryRequestDto.getTimeslotId()).get();
        delivery.setTimeslot(timeslot);

        final AddressDto addressDto = deliveryRequestDto.getUser().getAddress();
        final Address address = addressService.findByFields(addressDto);
        delivery.setAddress(address);
        address.setDelivery(delivery);

        delivery.setStatus(DeliveryStatus.CREATED);
        return delivery;
    }

    public DeliveryDto convertEntityToDto(Delivery delivery) {
        DeliveryDto deliveryDto = new DeliveryDto();
        deliveryDto.setId(delivery.getId());
        deliveryDto.setStatus(delivery.getStatus());
        deliveryDto.setTimeslot(timeslotService.convertEntityToDto(delivery.getTimeslot()));
        deliveryDto.setAddress(addressService.convertEntityToDto(delivery.getAddress()));
        return deliveryDto;
    }

    public DeliveryResponseDto getAllDeliveries() {
        final List<Delivery> deliveries = deliveryRepository.findAll();
        final List<DeliveryDto> deliveryDtos = deliveries.stream().
                filter(delivery -> !delivery.getStatus().equals(DeliveryStatus.CANCELED)).
                map(this::convertEntityToDto).toList();
        return new DeliveryResponseDto(deliveryDtos);
    }

    public Delivery cancelDelivery(Long id) {
        final Delivery delivery = deliveryRepository.findById(id).orElseThrow(() -> new DeliveryNotFoundException("delivery not found with id : " + id));
        if (delivery.getStatus().equals(DeliveryStatus.COMPLETED)) {
            throw new DeliveryWasAlreadyCompletedException("user can't cancel delivery which was already completed");
        }
        if (delivery.getStatus().equals(DeliveryStatus.CANCELED)) {
            throw new DeliveryWasAlreadyCanceledException("user can't cancel delivery which was already canceled");
        }
        deleteDeliveryFromDeliveriesPerDayMap(delivery);
        delivery.setStatus(DeliveryStatus.CANCELED);
        return deliveryRepository.save(delivery);
    }

    public Delivery completeDelivery(Long id) {
        final Delivery delivery = deliveryRepository.findById(id).orElseThrow(() -> new DeliveryNotFoundException("delivery not found with id : " + id));
        if (delivery.getStatus().equals(DeliveryStatus.COMPLETED)) {
            throw new DeliveryWasAlreadyCompletedException("user can't complete delivery which was already completed");
        }
        if (delivery.getStatus().equals(DeliveryStatus.CANCELED)) {
            throw new DeliveryWasAlreadyCanceledException("user can't complete delivery which was already canceled");
        }
        delivery.setStatus(DeliveryStatus.COMPLETED);
        changeDeliveryStatusInDeliveriesPerDayMap(delivery);
        return deliveryRepository.save(delivery);
    }



    public static Map<LocalDate, List<Delivery>> getDeliveriesPerDayMap() {
        return Collections.unmodifiableMap(deliveriesPerDayMap);
    }

    private void addDeliveryToDeliveriesPerDayMap(Delivery delivery) {
        final LocalDate deliveryDay = delivery.getTimeslot().getStartTime().toLocalDate();
        deliveriesPerDayMap.computeIfAbsent(deliveryDay, k -> new ArrayList<>());
        final List<Delivery> deliveries = deliveriesPerDayMap.get(deliveryDay);
        deliveries.add(delivery);
        deliveriesPerDayMap.put(deliveryDay,deliveries);
    }

    private void deleteDeliveryFromDeliveriesPerDayMap(Delivery delivery){
        final LocalDate deliveryDay = delivery.getTimeslot().getStartTime().toLocalDate();
        final List<Delivery> deliveries = deliveriesPerDayMap.get(deliveryDay);
        deliveries.removeIf(delivery1 -> delivery1.getId().equals(delivery.getId()));
        deliveriesPerDayMap.put(deliveryDay,deliveries);
    }

    private void changeDeliveryStatusInDeliveriesPerDayMap(Delivery delivery) {
        final LocalDate deliveryDay = delivery.getTimeslot().getStartTime().toLocalDate();
        final List<Delivery> deliveries = deliveriesPerDayMap.get(deliveryDay);
        deliveries.removeIf(delivery1 -> delivery1.getId().equals(delivery.getId()));
        deliveries.add(delivery);
        deliveriesPerDayMap.put(deliveryDay,deliveries);
    }

    public DeliveryResponseDto getAllTodayDeliveries(LocalDate date) {
        final List<Delivery> deliveries = deliveriesPerDayMap.getOrDefault(date, new ArrayList<>());
        final List<DeliveryDto> deliveryDtos = deliveries.stream().
                map(this::convertEntityToDto).toList();
        return new DeliveryResponseDto(deliveryDtos);
    }
}
