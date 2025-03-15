package com.hetero.repository;

import com.hetero.models.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;

@Repository
public interface AuditLogDao extends JpaRepository<AuditLog, Long> {
}
