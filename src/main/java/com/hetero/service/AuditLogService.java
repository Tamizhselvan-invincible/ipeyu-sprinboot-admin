package com.hetero.service;


import com.hetero.models.AuditLog;
import com.hetero.models.User;

import java.awt.print.Pageable;
import java.util.List;

public interface AuditLogService {

    AuditLog addLog(AuditLog log);
    List<AuditLog> findByUserId(Long userId);
    List<AuditLog> findByUserId(Long userId, Pageable pageable);
    AuditLog findById(Long id);
    String deleteById(Long id);
    AuditLog updateLog(AuditLog log);

}
