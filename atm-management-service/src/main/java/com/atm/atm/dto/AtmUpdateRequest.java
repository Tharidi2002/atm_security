package com.atm.atm.dto;

import com.atm.atm.enums.AtmStatus;
import com.atm.atm.enums.ZoneType;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AtmUpdateRequest {
    
    @Size(min = 10, max = 20, message = "Phone number must be between 10 and 20 characters")
    private String phoneNumber;
    
    private Long bankId;
    
    @Size(max = 200, message = "Location name must not exceed 200 characters")
    private String locationName;
    
    private Double latitude;
    
    private Double longitude;
    
    @Size(max = 300, message = "Address must not exceed 300 characters")
    private String address;
    
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;
    
    @Size(max = 100, message = "District must not exceed 100 characters")
    private String district;
    
    private ZoneType zoneType;
    
    @Size(max = 20, message = "Firmware version must not exceed 20 characters")
    private String firmwareVersion;
    
    private AtmStatus status;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
