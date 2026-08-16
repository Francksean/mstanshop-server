package com.mstan.shop.supplier.service;

import com.mstan.shop.common.exception.DuplicateResourceException;
import com.mstan.shop.common.exception.ResourceNotFoundException;
import com.mstan.shop.supplier.dto.SupplierRequest;
import com.mstan.shop.supplier.dto.SupplierResponse;
import com.mstan.shop.supplier.entity.Supplier;
import com.mstan.shop.supplier.mapper.SupplierMapper;
import com.mstan.shop.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Transactional(readOnly = true)
    public List<SupplierResponse> getAll() {
        return supplierRepository.findAll().stream()
                .map(supplierMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SupplierResponse getById(UUID id) {
        return supplierMapper.toResponse(findSupplier(id));
    }

    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        if (supplierRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Un fournisseur avec ce nom existe déjà");
        }

        Supplier supplier = new Supplier();
        supplier.setName(request.name());
        supplier.setPrimaryPhone(request.primaryPhone());
        supplier.setSecondaryPhone(request.secondaryPhone());
        supplier.setLocation(request.location());
        supplierRepository.save(supplier);

        return supplierMapper.toResponse(supplier);
    }

    @Transactional
    public SupplierResponse update(UUID id, SupplierRequest request) {
        Supplier supplier = findSupplier(id);
        supplier.setName(request.name());
        supplier.setPrimaryPhone(request.primaryPhone());
        supplier.setSecondaryPhone(request.secondaryPhone());
        supplier.setLocation(request.location());
        supplierRepository.save(supplier);

        return supplierMapper.toResponse(supplier);
    }

    @Transactional
    public void delete(UUID id) {
        Supplier supplier = findSupplier(id);
        supplierRepository.delete(supplier);
    }

    private Supplier findSupplier(UUID id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Fournisseur", id));
    }
}
