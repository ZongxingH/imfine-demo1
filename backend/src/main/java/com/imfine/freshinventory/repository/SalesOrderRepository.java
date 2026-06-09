package com.imfine.freshinventory.repository;

import com.imfine.freshinventory.domain.SalesOrder;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    boolean existsByOrderNo(String orderNo);
    boolean existsByOrderNoAndIdNot(String orderNo, Long id);

    @Query("""
            select so from SalesOrder so
            where (:orderNo is null or lower(so.orderNo) like lower(concat('%', :orderNo, '%')))
              and (:customerName is null or lower(so.customerName) like lower(concat('%', :customerName, '%')))
              and (:fromDate is null or so.outboundDate >= :fromDate)
              and (:toDate is null or so.outboundDate <= :toDate)
            order by so.id desc
            """)
    Page<SalesOrder> search(String orderNo, String customerName, LocalDate fromDate, LocalDate toDate, Pageable pageable);
}
