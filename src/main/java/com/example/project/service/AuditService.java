package com.example.project.service;

import com.example.project.model.AuditLog;
import com.example.project.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository repo;

    @Autowired
    public AuditService(AuditLogRepository repo) {
        this.repo = repo;
    }

    // Using async to avoid blocking the request thread.
    // Alternatively use propagation = REQUIRES_NEW in a transactional method
    @Async
    public void saveAsync(AuditLog log) {
        repo.save(log);
    }

    @Transactional // if you prefer sync with new transaction, you can change propagation
    public void saveSync(AuditLog log) {
        repo.save(log);
    }

    public Page<AuditLog> findAll(Pageable pageable) {
        return repo.findAll(pageable);
    }
}
