package com.danielbenami.dropit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String houseNumber;
    private String street;
    private String city;
    private String postCode;
    private String country;

    @ManyToMany(mappedBy = "addresses",fetch = FetchType.EAGER,cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    private Set<Timeslot> timeslots = new HashSet<>();

    public void addTimeSlot(Timeslot timeslot){
        this.timeslots.add(timeslot);
    }

    @OneToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST})
   // @JoinColumn(name = "delivery_id")
    private Delivery delivery;

}