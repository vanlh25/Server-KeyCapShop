package com.vn.keycap_server.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vn.keycap_server.modal.User;
import com.vn.keycap_server.utils.ERole;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Fetch avatarMedia cùng user để response login không truy cập quan hệ lazy sau khi repository đóng.
    @EntityGraph(attributePaths = "avatarMedia")
    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = "avatarMedia")
    Optional<User> findProfileById(Long id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<User> findByIdAndRole(Long id, ERole role);

    @EntityGraph(attributePaths = "avatarMedia")
    Page<User> findByRole(ERole role, Pageable pageable);

    @EntityGraph(attributePaths = "avatarMedia")
    @Query("""
            SELECT u
            FROM User u
            WHERE u.role = :role
              AND (
                    LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(COALESCE(u.fullName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR COALESCE(u.phone, '') LIKE CONCAT('%', :keyword, '%')
              )
            """)
    Page<User> searchByRoleAndKeyword(
            @Param("role") ERole role,
            @Param("keyword") String keyword,
            Pageable pageable);
}
