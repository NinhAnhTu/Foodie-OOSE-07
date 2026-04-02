package com.oose.restaurant_mis.controller;

import com.oose.restaurant_mis.entity.Order;
import com.oose.restaurant_mis.entity.OrderDetail;
import com.oose.restaurant_mis.enums.ServedStatus;
import com.oose.restaurant_mis.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/chef")
public class ChefController {
    @Autowired
    private OrderService orderService;

    @GetMapping("")
    public String kitchenPage() {
        return "chef/index";
    }

    @GetMapping("/api/items")
    @ResponseBody
    public List<Map<String, Object>> getKitchenItems() {
        List<OrderDetail> allItems = orderService.getKitchenOrders();

        //Lọc chắc chắn 1 lần nữa để Thymeleaf không bị chết
        allItems = allItems.stream().filter(item -> item.getOrder() != null).collect(Collectors.toList());

        Map<Integer, List<OrderDetail>> groupedByOrder = allItems.stream()
                .collect(Collectors.groupingBy(item -> item.getOrder().getOrderId()));

        return groupedByOrder.entrySet().stream().map(entry -> {
            Order order = entry.getValue().get(0).getOrder();
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("tableName", order.getTable() != null ? order.getTable().getTableName() : "Không rõ");
            orderMap.put("orderId", order.getOrderId());

            List<Map<String, Object>> items = entry.getValue().stream().map(item -> {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("orderDetailId", item.getOrderDetailId());
                itemMap.put("menuItemName", item.getMenuItem().getName());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("status", item.getServedStatus().name());
                itemMap.put("note", item.getNote());
                return itemMap;
            }).collect(Collectors.toList());

            orderMap.put("items", items);
            return orderMap;
        }).collect(Collectors.toList());
    }

    @PostMapping("/api/update-status/{detailId}")
    @ResponseBody
    public String updateStatus(@PathVariable Integer detailId, @RequestParam String status) {
        try {
            ServedStatus newStatus = ServedStatus.valueOf(status);
            orderService.updateItemStatus(detailId, newStatus);
            return "success";
        } catch (IllegalArgumentException e) {
            return "invalid_status";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }
}