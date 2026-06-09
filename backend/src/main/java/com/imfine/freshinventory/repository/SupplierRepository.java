package com.imfine.freshinventory.repository;

import com.imfine.freshinventory.domain.Status;
import com.imfine.freshinventory.domain.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);

    @Query("""
            select s from Supplier s
            where (:status is null or s.status = :status)
              and (:keyword is null or lower(s.name) like lower(concat('%', :keyword, '%')))
            order by s.id desc
            """)
    Page<Supplier> search(String keyword, Status status, Pageable pageable);
}
