package com.badmintonshop.entity;

import com.badmintonshop.entity.enums.AddressType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity UserAddress - Địa chỉ giao hàng của khách
 */
@Entity
@Table(name = "user_addresses", indexes = {
    @Index(name = "idx_user_addresses_user", columnList = "user_id"),
    @Index(name = "idx_user_addresses_default", columnList = "is_default")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    @Column(name = "address_line", nullable = false, length = 500)
    private String addressLine;

    @Column(name = "ward", length = 100)
    private String ward; // Phường/Xã

    @Column(name = "district", nullable = false, length = 100)
    private String district; // Quận/Huyện

    @Column(name = "city", nullable = false, length = 100)
    private String city; // Tỉnh/Thành phố

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type")
    @Builder.Default
    private AddressType addressType = AddressType.HOME;

    // Helper method
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(addressLine);
        if (ward != null && !ward.isEmpty()) {
            sb.append(", ").append(ward);
        }
        sb.append(", ").append(district);
        sb.append(", ").append(city);
        return sb.toString();
    }
}
