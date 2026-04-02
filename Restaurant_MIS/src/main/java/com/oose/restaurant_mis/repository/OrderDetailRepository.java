package com.oose.restaurant_mis.repository;

import com.oose.restaurant_mis.entity.OrderDetail;
import com.oose.restaurant_mis.enums.ServedStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    List<OrderDetail> findByOrder_OrderId(Integer orderId);
    OrderDetail findFirstByOrder_OrderIdAndMenuItem_ItemIdAndServedStatus(
            Integer orderId, Integer itemId, ServedStatus status
    );
    List<OrderDetail> findByServedStatusOrderByOrderDetailIdAsc(ServedStatus status);
    List<OrderDetail> findByServedStatusInOrderByOrderDetailIdAsc(List<ServedStatus> statuses);

    @Query("SELECT d FROM OrderDetail d WHERE d.servedStatus = 'SERVED' AND FUNCTION('DATE', d.createdAt) = :date")
    List<OrderDetail> findServedByDate(String dateStr);

    //Pie chart
    List<OrderDetail> findByOrder_OrderIdIn(List<Integer> orderIds);
}
