package com.banking.system.repository;

import com.banking.system.entity.TransactionAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionAuditRepository extends JpaRepository<TransactionAudit,Long> {
}
