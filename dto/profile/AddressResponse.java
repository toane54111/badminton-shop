package com.badmintonshop.dto.profile;

import com.badmintonshop.entity.enums.AddressType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressResponse {
    private Long addressId;
    private String recipientName;
    private String recipientPhone;
    private String addressLine;
    private String ward;
    private String district;
    private String city;
    private Boolean isDefault;
    private AddressType addressType;
    private String fullAddress;
}
