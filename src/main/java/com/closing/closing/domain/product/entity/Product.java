package com.closing.closing.domain.product.entity;

import com.closing.closing.domain.user.entity.User;
import com.closing.closing.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusinessCategory businessCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory productCategory;

    @Column(nullable = false)
    private int price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> imageUrls;

    @Column(nullable = false)
    private boolean isDeliveryAvailable = false;

    @Column(nullable = false)
    private boolean isDirectAvailable = false;

    private String tradeLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.SELLING;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private LocalDate purchasedAt;

    @Builder
    public Product(User seller,
                   String title,
                   BusinessCategory businessCategory,
                   ProductCategory productCategory,
                   int price,
                   String description,
                   List<String> imageUrls,
                   boolean isDeliveryAvailable,
                   boolean isDirectAvailable,
                   String tradeLocation,
                   BigDecimal latitude,
                   BigDecimal longitude,
                   LocalDate purchasedAt) {
        this.seller = seller;
        this.title = title;
        this.businessCategory = businessCategory;
        this.productCategory = productCategory;
        this.price = price;
        this.description = description;
        this.imageUrls = imageUrls;
        this.isDeliveryAvailable = isDeliveryAvailable;
        this.isDirectAvailable = isDirectAvailable;
        this.tradeLocation = tradeLocation;
        this.latitude = latitude;
        this.longitude = longitude;
        this.purchasedAt = purchasedAt;
    }

    public List<TradeMethod> getTradeMethods() {
        List<TradeMethod> tradeMethods = new ArrayList<>();
        if (this.isDirectAvailable) { tradeMethods.add(TradeMethod.DIRECT); }
        if (this.isDeliveryAvailable) { tradeMethods.add(TradeMethod.DELIVERY); }

        return tradeMethods;
    }

    // 소프트 삭제
    public void delete() {
        this.status = ProductStatus.DELETED;
    }

    // 상품 상태 수정
    public void updateStatus(ProductStatus productStatus) {
        this.status = productStatus;
    }
}
