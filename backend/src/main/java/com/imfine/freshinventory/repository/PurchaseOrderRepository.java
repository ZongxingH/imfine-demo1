package com.imfine.freshinventory.repository;

import com.imfine.freshinventory.domain.PurchaseOrder;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    boolean existsByOrderNo(String orderNo);
    boolean existsByOrderNoAndIdNot(String orderNo, Long id);

    @Query("""
            select po from PurchaseOrder po
            where (:orderNo is null or lower(po.orderNo) like lower(concat('%', :orderNo, '%')))
              and (:supplierId is null or po.supplier.id = :supplierId)
              and (:fromDate is null or po.inboundDate >= :fromDate)
              and (:toDate is null or po.inboundDate <= :toDate)
            order by po.id desc
            """)
    Page<PurchaseOrder> search(String orderNo, Long supplierId, LocalDate fromDate, LocalDate toDate, Pageable pageable);
}
