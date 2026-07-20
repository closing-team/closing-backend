package com.closing.closing.domain.product.repository;

import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.entity.ProductStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndStatusNot(Long productId, ProductStatus status);

    @Query("""
            SELECT p
            FROM Product p
            WHERE p.seller.id = :userId
              AND p.status <> :deletedStatus
              AND (:status IS NULL OR p.status = :status)
              AND (:cursor IS NULL OR p.id < :cursor)
            ORDER BY p.id DESC
            """)
    List<Product> findMyProducts(
            @Param("userId") Long userId,
            @Param("deletedStatus") ProductStatus deletedStatus,
            @Param("status") ProductStatus status,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    // 최신순 조회 기본 쿼리
    @Query(
            value = """
                SELECT p.*
                FROM products p
                WHERE p.status <> CAST(:deletedStatus AS varchar)

                  AND (
                      CAST(:keyword AS varchar) IS NULL
                      OR LOWER(p.title)
                         LIKE LOWER(CONCAT(
                             '%',
                             CAST(:keyword AS varchar),
                             '%'
                         ))
                  )

                  AND (
                      CAST(:businessCategory AS varchar) IS NULL
                      OR p.business_category =
                         CAST(:businessCategory AS varchar)
                  )

                  AND (
                      CAST(:productCategory AS varchar) IS NULL
                      OR p.product_category =
                         CAST(:productCategory AS varchar)
                  )

                  AND (
                      :requireDirect = false
                      OR p.is_direct_available = true
                  )

                  AND (
                      :requireDelivery = false
                      OR p.is_delivery_available = true
                  )

                  AND (
                      :nearby = false
                      OR (
                          p.latitude IS NOT NULL
                          AND p.longitude IS NOT NULL
                          AND (
                              6371.0088 * 2 * ASIN(
                                  LEAST(
                                      1.0,
                                      SQRT(
                                          POWER(
                                              SIN(
                                                  RADIANS(
                                                      CAST(p.latitude AS double precision)
                                                      - CAST(:latitude AS double precision)
                                                  ) / 2
                                              ),
                                              2
                                          )
                                          +
                                          COS(
                                              RADIANS(
                                                  CAST(:latitude AS double precision)
                                              )
                                          )
                                          *
                                          COS(
                                              RADIANS(
                                                  CAST(p.latitude AS double precision)
                                              )
                                          )
                                          *
                                          POWER(
                                              SIN(
                                                  RADIANS(
                                                      CAST(p.longitude AS double precision)
                                                      - CAST(:longitude AS double precision)
                                                  ) / 2
                                              ),
                                              2
                                          )
                                      )
                                  )
                              )
                          ) <= :radiusKm
                      )
                  )

                  AND (
                      CAST(:cursorId AS bigint) IS NULL
                      OR p.product_id < CAST(:cursorId AS bigint)
                  )

                ORDER BY p.product_id DESC
                """,
            nativeQuery = true
    )
    List<Product> findLatestProducts(
            @Param("deletedStatus") String deletedStatus,
            @Param("keyword") String keyword,
            @Param("businessCategory") String businessCategory,
            @Param("productCategory") String productCategory,
            @Param("requireDirect") boolean requireDirect,
            @Param("requireDelivery") boolean requireDelivery,
            @Param("nearby") boolean nearby,
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusKm") double radiusKm,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT p.*
                    FROM products p
                    WHERE p.status <> CAST(:deletedStatus AS varchar)
                      AND (
                          CAST(:keyword AS varchar) IS NULL
                          OR LOWER(p.title) LIKE LOWER(
                              CONCAT('%', CAST(:keyword AS varchar), '%')
                          )
                      )
                      AND (
                          CAST(:businessCategory AS varchar) IS NULL
                          OR p.business_category = CAST(:businessCategory AS varchar)
                      )
                      AND (
                          CAST(:productCategory AS varchar) IS NULL
                          OR p.product_category = CAST(:productCategory AS varchar)
                      )
                      AND (:requireDirect = false OR p.is_direct_available = true)
                      AND (:requireDelivery = false OR p.is_delivery_available = true)
                      AND (
                          :nearby = false
                          OR (
                              p.latitude IS NOT NULL
                              AND p.longitude IS NOT NULL
                              AND (
                                  6371.0088 * 2 * ASIN(
                                      LEAST(
                                          1.0,
                                          SQRT(
                                              POWER(
                                                  SIN(
                                                      RADIANS(
                                                          CAST(p.latitude AS double precision)
                                                          - CAST(:latitude AS double precision)
                                                      ) / 2
                                                  ), 2
                                              )
                                              + COS(RADIANS(CAST(:latitude AS double precision)))
                                              * COS(RADIANS(CAST(p.latitude AS double precision)))
                                              * POWER(
                                                  SIN(
                                                      RADIANS(
                                                          CAST(p.longitude AS double precision)
                                                          - CAST(:longitude AS double precision)
                                                      ) / 2
                                                  ), 2
                                              )
                                          )
                                      )
                                  )
                              ) <= :radiusKm
                          )
                      )
                      AND (
                          CAST(:cursorPrice AS integer) IS NULL
                          OR (
                              :cheapest = true
                              AND (
                                  p.price > CAST(:cursorPrice AS integer)
                                  OR (
                                      p.price = CAST(:cursorPrice AS integer)
                                      AND p.product_id < CAST(:cursorId AS bigint)
                                  )
                              )
                          )
                          OR (
                              :cheapest = false
                              AND (
                                  p.price < CAST(:cursorPrice AS integer)
                                  OR (
                                      p.price = CAST(:cursorPrice AS integer)
                                      AND p.product_id < CAST(:cursorId AS bigint)
                                  )
                              )
                          )
                      )
                    ORDER BY
                        CASE WHEN :cheapest = true THEN p.price END ASC,
                        CASE WHEN :cheapest = false THEN p.price END DESC,
                        p.product_id DESC
                    """,
            nativeQuery = true
    )
    List<Product> findProductsByPrice(
            @Param("deletedStatus") String deletedStatus,
            @Param("keyword") String keyword,
            @Param("businessCategory") String businessCategory,
            @Param("productCategory") String productCategory,
            @Param("requireDirect") boolean requireDirect,
            @Param("requireDelivery") boolean requireDelivery,
            @Param("nearby") boolean nearby,
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusKm") double radiusKm,
            @Param("cheapest") boolean cheapest,
            @Param("cursorPrice") Integer cursorPrice,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT p.*
                    FROM products p
                    LEFT JOIN product_bookmarks pb
                      ON pb.product_id = p.product_id
                    WHERE p.status <> CAST(:deletedStatus AS varchar)
                      AND (
                          CAST(:keyword AS varchar) IS NULL
                          OR LOWER(p.title) LIKE LOWER(
                              CONCAT('%', CAST(:keyword AS varchar), '%')
                          )
                      )
                      AND (
                          CAST(:businessCategory AS varchar) IS NULL
                          OR p.business_category = CAST(:businessCategory AS varchar)
                      )
                      AND (
                          CAST(:productCategory AS varchar) IS NULL
                          OR p.product_category = CAST(:productCategory AS varchar)
                      )
                      AND (:requireDirect = false OR p.is_direct_available = true)
                      AND (:requireDelivery = false OR p.is_delivery_available = true)
                      AND (
                          :nearby = false
                          OR (
                              p.latitude IS NOT NULL
                              AND p.longitude IS NOT NULL
                              AND (
                                  6371.0088 * 2 * ASIN(
                                      LEAST(
                                          1.0,
                                          SQRT(
                                              POWER(
                                                  SIN(
                                                      RADIANS(
                                                          CAST(p.latitude AS double precision)
                                                          - CAST(:latitude AS double precision)
                                                      ) / 2
                                                  ), 2
                                              )
                                              + COS(RADIANS(CAST(:latitude AS double precision)))
                                              * COS(RADIANS(CAST(p.latitude AS double precision)))
                                              * POWER(
                                                  SIN(
                                                      RADIANS(
                                                          CAST(p.longitude AS double precision)
                                                          - CAST(:longitude AS double precision)
                                                      ) / 2
                                                  ), 2
                                              )
                                          )
                                      )
                                  )
                              ) <= :radiusKm
                          )
                      )
                    GROUP BY p.product_id
                    HAVING (
                        CAST(:cursorBookmarkCount AS bigint) IS NULL
                        OR COUNT(pb.bookmark_id) < CAST(:cursorBookmarkCount AS bigint)
                        OR (
                            COUNT(pb.bookmark_id) = CAST(:cursorBookmarkCount AS bigint)
                            AND p.product_id < CAST(:cursorId AS bigint)
                        )
                    )
                    ORDER BY COUNT(pb.bookmark_id) DESC, p.product_id DESC
                    """,
            nativeQuery = true
    )
    List<Product> findPopularProducts(
            @Param("deletedStatus") String deletedStatus,
            @Param("keyword") String keyword,
            @Param("businessCategory") String businessCategory,
            @Param("productCategory") String productCategory,
            @Param("requireDirect") boolean requireDirect,
            @Param("requireDelivery") boolean requireDelivery,
            @Param("nearby") boolean nearby,
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusKm") double radiusKm,
            @Param("cursorBookmarkCount") Long cursorBookmarkCount,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT
                        p.product_id AS "productId",
                        calculated_distance.distance_km AS "distanceKm"
                    FROM products p
                    CROSS JOIN LATERAL (
                        SELECT
                            6371.0088 * 2 * ASIN(
                                LEAST(
                                    1.0,
                                    SQRT(
                                        POWER(
                                            SIN(
                                                RADIANS(
                                                    CAST(p.latitude AS double precision)
                                                    - CAST(:latitude AS double precision)
                                                ) / 2
                                            ), 2
                                        )
                                        + COS(RADIANS(CAST(:latitude AS double precision)))
                                        * COS(RADIANS(CAST(p.latitude AS double precision)))
                                        * POWER(
                                            SIN(
                                                RADIANS(
                                                    CAST(p.longitude AS double precision)
                                                    - CAST(:longitude AS double precision)
                                                ) / 2
                                            ), 2
                                        )
                                    )
                                )
                            ) AS distance_km
                    ) calculated_distance
                    WHERE p.status <> CAST(:deletedStatus AS varchar)
                      AND p.is_direct_available = true
                      AND p.latitude IS NOT NULL
                      AND p.longitude IS NOT NULL
                      AND (
                          CAST(:keyword AS varchar) IS NULL
                          OR LOWER(p.title) LIKE LOWER(
                              CONCAT('%', CAST(:keyword AS varchar), '%')
                          )
                      )
                      AND (
                          CAST(:businessCategory AS varchar) IS NULL
                          OR p.business_category = CAST(:businessCategory AS varchar)
                      )
                      AND (
                          CAST(:productCategory AS varchar) IS NULL
                          OR p.product_category = CAST(:productCategory AS varchar)
                      )
                      AND (:requireDirect = false OR p.is_direct_available = true)
                      AND (:requireDelivery = false OR p.is_delivery_available = true)
                      AND (
                          :nearby = false
                          OR calculated_distance.distance_km <= :radiusKm
                      )
                      AND (
                          CAST(:cursorDistance AS double precision) IS NULL
                          OR calculated_distance.distance_km
                             > CAST(:cursorDistance AS double precision)
                          OR (
                              calculated_distance.distance_km
                              = CAST(:cursorDistance AS double precision)
                              AND p.product_id < CAST(:cursorId AS bigint)
                          )
                      )
                    ORDER BY calculated_distance.distance_km ASC,
                             p.product_id DESC
                    """,
            nativeQuery = true
    )
    List<ProductDistanceProjection> findNearestProducts(
            @Param("deletedStatus") String deletedStatus,
            @Param("keyword") String keyword,
            @Param("businessCategory") String businessCategory,
            @Param("productCategory") String productCategory,
            @Param("requireDirect") boolean requireDirect,
            @Param("requireDelivery") boolean requireDelivery,
            @Param("nearby") boolean nearby,
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusKm") double radiusKm,
            @Param("cursorDistance") Double cursorDistance,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
