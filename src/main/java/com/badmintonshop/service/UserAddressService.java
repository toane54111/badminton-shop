package com.badmintonshop.service;

import com.badmintonshop.dto.profile.AddressRequest;
import com.badmintonshop.dto.profile.AddressResponse;
import com.badmintonshop.entity.User;
import com.badmintonshop.entity.UserAddress;
import com.badmintonshop.exception.BadRequestException;
import com.badmintonshop.exception.ResourceNotFoundException;
import com.badmintonshop.repository.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAddressService {

    private final UserAddressRepository addressRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses() {
        User user = authService.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Người dùng chưa đăng nhập"));

        return addressRepository.findByUser_UserId(user.getUserId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressResponse addAddress(AddressRequest request) {
        User user = authService.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Người dùng chưa đăng nhập"));

        UserAddress address = mapToEntity(request);
        address.setUser(user);

        // If this is the first address, make it default
        List<UserAddress> existingAddresses = addressRepository.findByUser_UserId(user.getUserId());
        if (existingAddresses.isEmpty()) {
            address.setIsDefault(true);
        } else if (request.getIsDefault() != null && request.getIsDefault()) {
            // Remove old default
            existingAddresses.forEach(a -> {
                if (Boolean.TRUE.equals(a.getIsDefault())) {
                    a.setIsDefault(false);
                    addressRepository.save(a);
                }
            });
            address.setIsDefault(true);
        } else {
             address.setIsDefault(false);
        }

        UserAddress savedAddress = addressRepository.save(address);
        log.info("Added new address for user: {}", user.getEmail());
        return mapToResponse(savedAddress);
    }

    @Transactional
    public AddressResponse updateAddress(Long addressId, AddressRequest request) {
        User user = authService.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Người dùng chưa đăng nhập"));

        UserAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ"));

        if (!address.getUser().getUserId().equals(user.getUserId())) {
            throw new BadRequestException("Bạn không có quyền chỉnh sửa địa chỉ này");
        }

        address.setRecipientName(request.getRecipientName());
        address.setRecipientPhone(request.getRecipientPhone());
        address.setAddressLine(request.getAddressLine());
        address.setWard(request.getWard());
        address.setDistrict(request.getDistrict());
        address.setCity(request.getCity());
        address.setAddressType(request.getAddressType());

        if (request.getIsDefault() != null && request.getIsDefault() && !Boolean.TRUE.equals(address.getIsDefault())) {
             // Set as default, unset others
             List<UserAddress> addresses = addressRepository.findByUser_UserId(user.getUserId());
             addresses.forEach(a -> {
                 if (Boolean.TRUE.equals(a.getIsDefault())) {
                     a.setIsDefault(false);
                     addressRepository.save(a);
                 }
             });
             address.setIsDefault(true);
        }

        UserAddress saved = addressRepository.save(address);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteAddress(Long addressId) {
        User user = authService.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Người dùng chưa đăng nhập"));

        UserAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ"));

        if (!address.getUser().getUserId().equals(user.getUserId())) {
            throw new BadRequestException("Bạn không có quyền xóa địa chỉ này");
        }
        
        if (Boolean.TRUE.equals(address.getIsDefault())) {
             throw new BadRequestException("Không thể xóa địa chỉ mặc định");
        }

        addressRepository.delete(address);
        log.info("Deleted address {} for user {}", addressId, user.getEmail());
    }

    @Transactional
    public void setDefaultAddress(Long addressId) {
        User user = authService.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Người dùng chưa đăng nhập"));

        UserAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ"));

        if (!address.getUser().getUserId().equals(user.getUserId())) {
            throw new BadRequestException("Bạn không có quyền chỉnh sửa địa chỉ này");
        }

        if (Boolean.TRUE.equals(address.getIsDefault())) {
            return;
        }

        List<UserAddress> addresses = addressRepository.findByUser_UserId(user.getUserId());
        addresses.forEach(a -> {
            if (Boolean.TRUE.equals(a.getIsDefault())) {
                a.setIsDefault(false);
                addressRepository.save(a);
            }
        });

        address.setIsDefault(true);
        addressRepository.save(address);
    }

    private UserAddress mapToEntity(AddressRequest request) {
        UserAddress address = new UserAddress();
        address.setRecipientName(request.getRecipientName());
        address.setRecipientPhone(request.getRecipientPhone());
        address.setAddressLine(request.getAddressLine());
        address.setWard(request.getWard());
        address.setDistrict(request.getDistrict());
        address.setCity(request.getCity());
        address.setAddressType(request.getAddressType());
        // Default logic needs to be handled in service method to unset others
        return address;
    }

    private AddressResponse mapToResponse(UserAddress address) {
        return AddressResponse.builder()
                .addressId(address.getAddressId())
                .recipientName(address.getRecipientName())
                .recipientPhone(address.getRecipientPhone())
                .addressLine(address.getAddressLine())
                .ward(address.getWard())
                .district(address.getDistrict())
                .city(address.getCity())
                .isDefault(address.getIsDefault())
                .addressType(address.getAddressType())
                .fullAddress(address.getFullAddress())
                .build();
    }
}
