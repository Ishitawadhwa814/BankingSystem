package com.banking.system.repository;

import com.banking.system.entity.FailedTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FailedTransactionRepository extends JpaRepository<FailedTransaction,Long> {

}
