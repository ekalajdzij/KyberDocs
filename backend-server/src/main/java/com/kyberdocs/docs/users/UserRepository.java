package com.kyberdocs.docs.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.status = 'STATUS_ACTIVE' AND u.lastHeartbeat <: threshold")
    List<User> findUsersDueForWarning(@Param("threshold")Timestamp threshold);

    @Query("SELECT u FROM User u WHERE u.status IN ('STATUS_ACTIVE', 'STATUS_WARNING') AND u.lastHeartbeat < :threshold")
    List<User> findUsersDueForTrigger(@Param("threshold") Timestamp threshold);
}
