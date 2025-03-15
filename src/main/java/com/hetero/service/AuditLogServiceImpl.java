package com.hetero.service;

import com.hetero.models.AuditLog;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Override
    public AuditLog addLog (AuditLog log) {
        return null;
    }

    @Override
    public List<AuditLog> findByUserId (Long userId) {
        return List.of();
    }

    @Override
    public List<AuditLog> findByUserId (Long userId, Pageable pageable) {
        return List.of();
    }

    @Override
    public AuditLog findById (Long id) {
        return null;
    }

    @Override
    public String deleteById (Long id) {
        return "";
    }

    @Override
    public AuditLog updateLog (AuditLog log) {
        return null;
    }
}
