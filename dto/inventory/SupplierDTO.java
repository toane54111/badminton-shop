package com.badmintonshop.dto.inventory;

import com.badmintonshop.entity.Supplier;
import lombok.*;

/**
 * DTO for Supplier
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDTO {

    private Long supplierId;
    private String name;
    private String code;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private String city;
    private String country;
    private String paymentTerms;
    private String notes;
    private Boolean isActive;

    public static SupplierDTO fromEntity(Supplier supplier) {
        if (supplier == null)
            return null;

        return SupplierDTO.builder()
                .supplierId(supplier.getSupplierId())
                .name(supplier.getName())
                .code(supplier.getCode())
                .contactName(supplier.getContactName())
                .contactEmail(supplier.getContactEmail())
                .contactPhone(supplier.getContactPhone())
                .address(supplier.getAddress())
                .city(supplier.getCity())
                .country(supplier.getCountry())
                .paymentTerms(supplier.getPaymentTerms())
                .notes(supplier.getNotes())
                .isActive(supplier.getIsActive())
                .build();
    }
}
