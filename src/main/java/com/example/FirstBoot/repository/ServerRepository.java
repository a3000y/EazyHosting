package com.example.FirstBoot.repository;

import com.example.FirstBoot.model.Server;
import com.example.FirstBoot.model.ServiceTier;
import com.example.FirstBoot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServerRepository extends JpaRepository<Server, Long> {



    Optional<Server> findByUniqueServerCode(String serverCode);

    List<Server> findByStatus(String running);

    // เมธอดที่ถูกต้อง (ใช้ 'Owner' ตามชื่อ Field ใน Server Entity)


    // Change the HQL to compare the ID of the owner
    @Query("SELECT s FROM Server s WHERE s.owner.userId = :ownerId AND s.tier = :tier AND s.status <> :status")
    Optional<Server> findActiveFreeServer(
            @Param("ownerId") Long ownerId, // Parameter must now be Long (User ID)
            @Param("tier") ServiceTier tier,
            @Param("status") String status
    );

    @Query("SELECT COUNT(s) FROM Server s WHERE s.owner.userId = :ownerId AND s.status IN ('Running', 'Provisioning')")
    long countActiveServersByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT s FROM Server s LEFT JOIN FETCH s.owner LEFT JOIN FETCH s.usages WHERE s.serverId = :serverId")
    Optional<Server> findByIdWithDetails(@Param("serverId") Long serverId);

    @Query("SELECT s FROM Server s LEFT JOIN FETCH s.owner WHERE s.status = :status AND s.owner.userId = :userId")
    List<Server> findServersByStatusAndOwnerIdWithDetails(
            @Param("status") String status,
            @Param("userId") Long userId
    );

    // 2. เมธอดสำหรับ GET /status-for-renewal (โหลด Owner)
    // แก้ปัญหา Lazy Load สำหรับการดึงรายการ Server ทั้งหมดของ User
    @Query("SELECT s FROM Server s LEFT JOIN FETCH s.owner WHERE s.owner.userId = :userId ORDER BY s.createdAt DESC")
    List<Server> findAllByOwnerIdWithDetails(@Param("userId") Long userId);

    List<Server> findByOwnerUserId(Long userId);
}