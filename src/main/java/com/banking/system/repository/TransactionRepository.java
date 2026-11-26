package com.banking.system.repository;

import com.banking.system.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction,Long>{

    List<Transaction> findByAccount_IdOrReceiverAccount_Id(Long accountId,Long receiverId);

    List<Transaction> findByAccount_IdAndTypeIgnoreCaseOrReceiverAccount_IdAndTypeIgnoreCase(Long senderId,String senderType,Long receiverId,String receiverType);

}
