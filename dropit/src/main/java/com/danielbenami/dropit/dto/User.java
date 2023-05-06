package com.danielbenami.dropit.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User {

    @NotBlank(message = "firstName must not be null or empty")
    private String firstName;

    @NotBlank(message = "lastName must not be null or empty")
    private String lastName;

    @Valid
    private AddressDto address;
}
