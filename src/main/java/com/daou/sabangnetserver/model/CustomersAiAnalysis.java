package com.daou.sabangnetserver.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "customers_ai_analysis")
public class CustomersAiAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @ElementCollection
    @CollectionTable(name = "purchase_info", joinColumns = @JoinColumn(name = "customers_id"))
    private List<PurchaseInfo> purchaseInfo;

    @Column(name = "frequent_orders")
    private List<String> frequentOrders;

    @Column(name = "personalized_recommendations")
    private List<String> personalizedRecommendations;

    @Column(name = "customer_segments")
    private String customerSegments;

    @Column(name = "personalized_recommendations_reason")
    private String personalizedRecommendationsReason;

    @Column(name = "analyzed_time")
    private LocalDateTime analyzedTime;



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Embeddable
    public static class PurchaseInfo {
        private String ordNo;
        private String productName;
        private String optionValue;

    }


}
