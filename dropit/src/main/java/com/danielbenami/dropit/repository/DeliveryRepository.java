package com.danielbenami.dropit.repository;

import com.danielbenami.dropit.entity.Address;
import com.danielbenami.dropit.entity.Delivery;
import com.danielbenami.dropit.entity.Timeslot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findAllByTimeslot(Timeslot timeslot);

}
