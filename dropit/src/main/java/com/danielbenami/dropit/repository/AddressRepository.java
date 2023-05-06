package com.danielbenami.dropit.repository;

import com.danielbenami.dropit.entity.Address;
import com.danielbenami.dropit.entity.Timeslot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    Address findByHouseNumberAndStreetAndCityAndPostCodeAndCountry(String houseNumber, String street,
                                                                   String city, String postCode, String country);


}
