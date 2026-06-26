package com.example.library.repository;

import com.example.library.entity.SysUser;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SysUserRepository extends JpaRepository<SysUser, Long> {
    Optional<SysUser> findByUsername(String username);

    boolean existsByUsername(String username);

    Page<SysUser> findByUsernameContainingOrRealNameContainingOrderByUserIdDesc(String username, String realName, Pageable pageable);

    Page<SysUser> findAllByOrderByUserIdDesc(Pageable pageable);

    long countByRole(Integer role);

    @Query("select count(u) from SysUser u where u.status = 1 and u.role in (1, 2)")
    long countActiveReaders();
}
