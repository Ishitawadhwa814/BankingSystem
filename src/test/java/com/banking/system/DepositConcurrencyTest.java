package com.banking.system;


import com.banking.system.entity.Account;
import com.banking.system.repository.AccountRepository;
import com.banking.system.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.aop.support.AopUtils;
import java.math.BigDecimal;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DepositConcurrencyTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void testConcurrentDeposits() throws Exception{

        System.out.println("AccountService is Spring proxy: "+AopUtils.isAopProxy(accountService));
        Account account = accountRepository.findById(4L).orElseThrow(()->new RuntimeException("Account not found"));
        account.setBalance(new BigDecimal("10000"));
        accountRepository.saveAndFlush(account);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Boolean> deposit1 = () -> {
            ready.countDown();
            start.await();

            try {
                accountService.deposit(4L, new BigDecimal("5000"));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        };

        Callable<Boolean> deposit2 = ()-> {
            ready.countDown();
            start.await();

            try{
                accountService.deposit(4L,new BigDecimal("3000"));
                return true;
            }catch(Exception e){
                e.printStackTrace();
                return false;
            }

        };

        try{
            Future<Boolean> res1 = executor.submit(deposit1);
            Future<Boolean> res2 = executor.submit(deposit2);

            ready.await();
            start.countDown();

            boolean success1 = res1.get();
            boolean success2 = res2.get();

            assertTrue(success1,"First deposit should succeed");
            assertTrue(success2, "Second deposit should succeed");

            Account updated = accountRepository.findById(4L).orElseThrow(() -> new RuntimeException("Account not found"));

            BigDecimal expectedBalance = new BigDecimal("18000");

            System.out.println("Deposit 1 success: "+success1);
            System.out.println("Deposit 2 success: "+success2);
            System.out.println("Expected balance: "+expectedBalance);
            System.out.println("Final balance: " + updated.getBalance());

            assertEquals(0,updated.getBalance().compareTo(expectedBalance),"Final balance should be 18000");

        }finally {
            start.countDown();
            executor.shutdown();

            if(!executor.awaitTermination(10,TimeUnit.SECONDS)){
                executor.shutdownNow();
            }
        }
    }
}
