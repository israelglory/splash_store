package com.example.splashstore.service;

import com.example.splashstore.dto.AddressRequest;
import com.example.splashstore.dto.AddressResponse;
import com.example.splashstore.model.AddressModel;
import com.example.splashstore.model.AppUser;
import com.example.splashstore.repository.AddressRepository;
import com.example.splashstore.repository.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final AppUserRepository userRepository;

    public AddressService(AddressRepository addressRepository, AppUserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    private AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    @Transactional
    public AddressResponse createAddress(AddressRequest request) {
        AppUser currentUser = getCurrentUser();

        AddressModel address = new AddressModel();
        address.setLabel(request.getLabel());
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setPhone(request.getPhone());
        address.setIsDefault(request.getIsDefault() != null && request.getIsDefault());
        address.setUser(currentUser);

        return mapToResponse(addressRepository.save(address));
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getMyAddresses() {
        AppUser currentUser = getCurrentUser();
        return addressRepository.findByUserId(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AddressResponse getAddressById(Long id) {
        AddressModel address = addressRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));

        AppUser currentUser = getCurrentUser();
        boolean isOwner = address.getUser() != null && address.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() != null && currentUser.getRole().equalsIgnoreCase("admin");

        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this address");
        }

        return mapToResponse(address);
    }

    @Transactional
    public AddressResponse updateAddress(Long id, AddressRequest request) {
        AddressModel address = addressRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));

        AppUser currentUser = getCurrentUser();
        boolean isOwner = address.getUser() != null && address.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() != null && currentUser.getRole().equalsIgnoreCase("admin");

        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to update this address");
        }

        address.setLabel(request.getLabel());
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setPhone(request.getPhone());
        address.setIsDefault(request.getIsDefault() != null && request.getIsDefault());

        return mapToResponse(addressRepository.save(address));
    }

    private AddressResponse mapToResponse(AddressModel address) {
        AddressResponse response = new AddressResponse();
        response.setId(address.getId());
        response.setLabel(address.getLabel());
        response.setStreet(address.getStreet());
        response.setCity(address.getCity());
        response.setState(address.getState());
        response.setPostalCode(address.getPostalCode());
        response.setCountry(address.getCountry());
        response.setPhone(address.getPhone());
        response.setIsDefault(address.getIsDefault());
        response.setCreatedAt(address.getCreatedAt());
        return response;
    }
}

