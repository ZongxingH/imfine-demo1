package com.imfine.freshinventory.service;

import com.imfine.freshinventory.domain.Status;
import com.imfine.freshinventory.domain.Supplier;
import com.imfine.freshinventory.dto.CommonDtos.PageResponse;
import com.imfine.freshinventory.dto.SupplierDtos.SupplierRequest;
import com.imfine.freshinventory.dto.SupplierDtos.SupplierResponse;
import com.imfine.freshinventory.repository.SupplierRepository;
import com.imfine.freshinventory.validation.ValidationUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        ValidationUtils.requireUnique(!supplierRepository.existsByName(request.name()), "Supplier name already exists");
        Supplier supplier = new Supplier();
        apply(supplier, request);
        return SupplierResponse.from(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = getEntity(id);
        ValidationUtils.requireUnique(!supplierRepository.existsByNameAndIdNot(request.name(), id), "Supplier name already exists");
        apply(supplier, request);
        return SupplierResponse.from(supplierRepository.save(supplier));
    }

    @Transactional(readOnly = true)
    public SupplierResponse detail(Long id) {
        return SupplierResponse.from(getEntity(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<SupplierResponse> page(String keyword, Status status, Pageable pageable) {
        return PageResponse.from(supplierRepository.search(blankToNull(keyword), status, pageable).map(SupplierResponse::from));
    }

    Supplier getEntity(Long id) {
        return ValidationUtils.requireFound(supplierRepository.findById(id), "Supplier not found");
    }

    private void apply(Supplier supplier, SupplierRequest request) {
        supplier.setName(request.name());
        supplier.setContactName(request.contactName());
        supplier.setContactPhone(request.contactPhone());
        supplier.setAddress(request.address());
        supplier.setStatus(request.status() == null ? Status.ACTIVE : request.status());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
