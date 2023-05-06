package com.danielbenami.dropit.service;

import com.danielbenami.dropit.dto.AddressDto;
import com.danielbenami.dropit.entity.Address;
import com.danielbenami.dropit.repository.AddressRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AddressService {

    @Value("${geo.api}")
    private String geoApi;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final AddressRepository addressRepository;


    public AddressService(RestTemplate restTemplate, ObjectMapper objectMapper, AddressRepository addressRepository) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.addressRepository = addressRepository;
    }

    public AddressDto resolveAddress(String text) throws JsonProcessingException {
        String url = geoApi + text;
        String response = restTemplate.getForObject(url, String.class);
        JsonNode jsonNode = objectMapper.readTree(response);
        JsonNode propertiesNode = jsonNode.get("results").get(0);
        return AddressDto.builder()
                .houseNumber(propertiesNode.get("housenumber").asText())
                .street(propertiesNode.get("street").asText())
                .city(propertiesNode.get("city").asText())
                .postCode(propertiesNode.get("postcode").asText())
                .country(propertiesNode.get("country").asText())
                .build();
    }

    public Address convertDtoToEntity(AddressDto addressDto) {
        Address address = new Address();
        address.setHouseNumber(addressDto.getHouseNumber());
        address.setStreet(addressDto.getStreet());
        address.setCity(addressDto.getCity());
        address.setPostCode(addressDto.getPostCode());
        address.setCountry(addressDto.getCountry());
        return address;
    }

    public Address findByFields(AddressDto addressDto) {
        return addressRepository.findByHouseNumberAndStreetAndCityAndPostCodeAndCountry(
                addressDto.getHouseNumber(), addressDto.getStreet(), addressDto.getCity(), addressDto.getPostCode(), addressDto.getCountry());
    }

    public Address save(Address address) {
        return addressRepository.save(address);
    }

    public AddressDto convertEntityToDto(Address address) {
        AddressDto addressDto = new AddressDto();
        addressDto.setHouseNumber(address.getHouseNumber());
        addressDto.setStreet(address.getStreet());
        addressDto.setCity(address.getCity());
        addressDto.setPostCode(address.getPostCode());
        addressDto.setCountry(address.getCountry());
        return addressDto;
    }
}
