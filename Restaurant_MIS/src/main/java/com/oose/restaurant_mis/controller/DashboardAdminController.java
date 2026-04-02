package com.oose.restaurant_mis.controller;

import com.oose.restaurant_mis.entity.Order;
import com.oose.restaurant_mis.entity.OrderDetail;
import com.oose.restaurant_mis.enums.OrderStatus;
import com.oose.restaurant_mis.enums.ReservationStatus;
import com.oose.restaurant_mis.enums.TableStatus;
import com.oose.restaurant_mis.repository.DiningTableRepository;
import com.oose.restaurant_mis.repository.OrderDetailRepository;
import com.oose.restaurant_mis.repository.OrderRepository;
import com.oose.restaurant_mis.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/api/dashboard")
public class DashboardAdminController {

    @Autowired private OrderRepository orderRepository;
    @Autowired private DiningTableRepository tableRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private OrderDetailRepository detailRepository;

    @GetMapping("/content")
    public String getDashboardContent(
            @RequestParam(required = false) String panelDate,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {

        LocalDate pDate = (panelDate != null && !panelDate.isEmpty()) ? LocalDate.parse(panelDate) : LocalDate.now();
        LocalDateTime startOfPanel = pDate.atTime(LocalTime.MIN);
        LocalDateTime endOfPanel = pDate.atTime(LocalTime.MAX);

        List<Order> panelOrders = orderRepository.findByCreatedAtBetween(startOfPanel, endOfPanel);
        double panelRevenue = 0;
        int paidOrdersCount = 0;
        for (Order order : panelOrders) {
            if (order.getStatus() == OrderStatus.PAID) {
                panelRevenue += order.getFinalAmount();
                paidOrdersCount++;
            }
        }

        // Bàn ăn: Bắt buộc là trạng thái hiện tại (Real-time)
        long emptyTables = tableRepository.findAll().stream().filter(t -> t.getStatus() == TableStatus.EMPTY).count();
        long occupiedTables = tableRepository.findAll().stream().filter(t -> t.getStatus() == TableStatus.OCCUPIED).count();

        // Đặt bàn: Lọc theo ngày hẹn (reservationTime) trùng với panelDate
        long pendingReservations = reservationRepository.findAll().stream()
                .filter(r -> r.getReservationTime() != null
                        && r.getReservationTime().toLocalDate().equals(pDate)
                        && r.getStatus() == ReservationStatus.PENDING)
                .count();

        LocalDate sDate = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate) : LocalDate.now().minusDays(6);
        LocalDate eDate = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : LocalDate.now();

        List<String> chartLabels = new ArrayList<>();
        List<Double> chartData = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        LocalDate currentDate = sDate;
        // Giới hạn vòng lặp tối đa 60 ngày để tránh treo server nếu chọn khoảng quá rộng
        int dayLimit = 0;
        while (!currentDate.isAfter(eDate) && dayLimit < 60) {
            LocalDateTime s = currentDate.atTime(LocalTime.MIN);
            LocalDateTime e = currentDate.atTime(LocalTime.MAX);
            List<Order> dailyOrders = orderRepository.findByCreatedAtBetween(s, e);
            double dailyTotal = dailyOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.PAID)
                    .mapToDouble(Order::getFinalAmount)
                    .sum();

            chartLabels.add(currentDate.format(formatter));
            chartData.add(dailyTotal);
            currentDate = currentDate.plusDays(1);
            dayLimit++;
        }

        LocalDateTime sChart = sDate.atTime(LocalTime.MIN);
        LocalDateTime eChart = eDate.atTime(LocalTime.MAX);
        List<Order> rangeOrders = orderRepository.findByCreatedAtBetween(sChart, eChart);
        List<Integer> paidOrderIds = rangeOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID)
                .map(Order::getOrderId)
                .collect(Collectors.toList());

        Map<String, Integer> itemSales = new HashMap<>();
        if (!paidOrderIds.isEmpty()) {
            List<OrderDetail> allDetails = detailRepository.findByOrder_OrderIdIn(paidOrderIds);
            for (OrderDetail d : allDetails) {
                String name = d.getMenuItem().getName();
                itemSales.put(name, itemSales.getOrDefault(name, 0) + d.getQuantity());
            }
        }

        // Sắp xếp giảm dần và lấy Top 5
        List<Map.Entry<String, Integer>> sortedSales = new ArrayList<>(itemSales.entrySet());
        sortedSales.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        List<String> pieLabels = new ArrayList<>();
        List<Integer> pieData = new ArrayList<>();
        int otherSales = 0;

        for (int i = 0; i < sortedSales.size(); i++) {
            if (i < 5) {
                pieLabels.add(sortedSales.get(i).getKey());
                pieData.add(sortedSales.get(i).getValue());
            } else {
                otherSales += sortedSales.get(i).getValue();
            }
        }
        if (otherSales > 0) {
            pieLabels.add("Khác");
            pieData.add(otherSales);
        }

        // Đẩy toàn bộ ra View
        model.addAttribute("panelDate", pDate.toString());
        model.addAttribute("startDate", sDate.toString());
        model.addAttribute("endDate", eDate.toString());

        model.addAttribute("panelRevenue", panelRevenue);
        model.addAttribute("totalOrdersToday", panelOrders.size());
        model.addAttribute("paidOrdersCount", paidOrdersCount);
        model.addAttribute("emptyTables", emptyTables);
        model.addAttribute("occupiedTables", occupiedTables);
        model.addAttribute("pendingReservations", pendingReservations);

        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartData", chartData);
        model.addAttribute("pieLabels", pieLabels);
        model.addAttribute("pieData", pieData);

        return "admin/dashboard :: content";
    }
}