package com.danielbenami.dropit.entity;

import com.danielbenami.dropit.enums.DeliveryStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Timeslot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endTime;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(name = "timeslot_address",
            joinColumns = {@JoinColumn(name = "timeslot id")},
            inverseJoinColumns = {@JoinColumn(name = "address id")})
    private Set<Address> addresses = new HashSet<>();

    @OneToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "timeslot_id")
    private List<Delivery> deliveries = new ArrayList<>();

    public void addAddress(Address address) {
        this.addresses.add(address);
    }
}
