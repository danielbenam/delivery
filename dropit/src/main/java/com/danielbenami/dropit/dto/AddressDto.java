package com.danielbenami.dropit.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AddressDto {

    @NotBlank(message = "houseNumber must not be null or empty")
    private String houseNumber;

    @NotBlank(message = "street must not be null or empty")
    private String street;

    @NotBlank(message = "city must not be null or empty")
    private String city;

    @NotBlank(message = "postCode must not be null or empty")
    private String postCode;

    @NotBlank(message = "country must not be null or empty")
    private String country;
}
