package com.banking.system;

import com.banking.system.entity.Account;
import com.banking.system.repository.AccountRepository;
import com.banking.system.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class TransferConcurrencyTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    private final Long account1 = 4L;
    private final Long account2 = 5L;

    @BeforeEach
    void resetBalances() {
        Account a = accountRepository.findById(account1).orElseThrow();
        Account b = accountRepository.findById(account2).orElseThrow();

        a.setBalance(new BigDecimal("10000"));
        b.setBalance(new BigDecimal("10000"));

        accountRepository.saveAndFlush(a);
        accountRepository.saveAndFlush(b);
    }

    @Test
    void testConcurrentOppositeTransfers() throws Exception {

        String transactionId1 = "TC-001-" + UUID.randomUUID();
        String transactionId2 = "TC-002-" + UUID.randomUUID();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);

       try {
           Callable<Boolean> transfer1To2 = () -> {
               latch.await();
               accountService.transfer(transactionId1, account1, account2, new BigDecimal("1000"));
               return true;


           };
           Callable<Boolean> transfer2To1 = () -> {
               latch.await();
               accountService.transfer(transactionId2, account2, account1, new BigDecimal("2000"));
               return true;
           };

           Future<Boolean> f1 = executor.submit(transfer1To2);
           Future<Boolean> f2 = executor.submit(transfer2To1);

           latch.countDown();

           assertTrue(f1.get(15, TimeUnit.SECONDS));
           assertTrue(f2.get(15, TimeUnit.SECONDS));

           Account updatedA = accountRepository.findById(account1).orElseThrow();
           Account updatedB = accountRepository.findById(account2).orElseThrow();

           assertEquals(0, new BigDecimal("11000").compareTo(updatedA.getBalance()));

           assertEquals(0, new BigDecimal("9000").compareTo(updatedB.getBalance()));

           System.out.println("Final Account A balance: " + updatedA.getBalance());
           System.out.println("Final Account B balance: " + updatedB.getBalance());

       }finally{
           executor.shutdown();
    }
}


}
