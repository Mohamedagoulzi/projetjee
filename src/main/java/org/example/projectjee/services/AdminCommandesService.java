package org.example.projectjee.services;

import lombok.RequiredArgsConstructor;
import org.example.projectjee.dto.AdminCartRowDTO;
import org.example.projectjee.dto.AdminOrderRowDTO;
import org.example.projectjee.dto.AdminCommandeKpisDTO; // ton DTO kpis si tu l'as
import org.example.projectjee.repository.CartItemRepository;
import org.example.projectjee.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCommandesService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;

    public List<AdminOrderRowDTO> getConfirmedOrders() {
        return orderRepository.findAllAdminRowsNativeRaw()
                .stream()
                .map(r -> AdminOrderRowDTO.builder()
                        .id(toLong(r[0]))
                        .utilisateurId(toLong(r[1]))
                        .utilisateurNom((String) r[2])
                        .dateCreation(toLocalDateTime(r[3]))
                        .montantTotal(toDouble(r[4]))
                        .build()
                )
                .toList();
    }

    public List<AdminCartRowDTO> getPendingCarts() {
        return cartItemRepository.findPendingCartsRaw()
                .stream()
                .map(r -> AdminCartRowDTO.builder()
                        .id(toLong(r[0]))                 // = utilisateur_id (id panier)
                        .utilisateurId(toLong(r[1]))      // = utilisateur_id
                        .utilisateurNom((String) r[2])
                        .updatedAt(toLocalDateTime(r[3])) // null
                        .total(toDouble(r[4]))
                        .build()
                )
                .toList();
    }

    public AdminCommandeKpisDTO getKpis() {
        return AdminCommandeKpisDTO.builder()
                .confirmedOrders(orderRepository.countOrdersNative())
                .pendingCarts(cartItemRepository.countDistinctUsersHavingCartNative())
                .confirmedTotal(orderRepository.sumOrdersTotalNative())
                .pendingTotal(cartItemRepository.sumCartValueNative())
                .build();
    }

    // ===== helpers safe =====
    private Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        return Long.valueOf(o.toString());
    }

    private Double toDouble(Object o) {
        if (o == null) return 0.0;
        if (o instanceof Number n) return n.doubleValue();
        return Double.valueOf(o.toString());
    }

    private LocalDateTime toLocalDateTime(Object o) {
        if (o == null) return null;
        if (o instanceof LocalDateTime ldt) return ldt;
        if (o instanceof Timestamp ts) return ts.toLocalDateTime();
        // si MySQL renvoie String
        return LocalDateTime.parse(o.toString().replace(" ", "T"));
    }
}
