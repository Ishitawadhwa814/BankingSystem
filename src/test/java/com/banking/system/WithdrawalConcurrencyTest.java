//package com.banking.system;
//
//import com.banking.system.service.AccountService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import com.banking.system.entity.Account;
//import com.banking.system.repository.AccountRepository;
//import java.util.stream.Stream;
//import java.math.BigDecimal;
//import java.util.concurrent.*;
//
//@SpringBootTest
//public class WithdrawalConcurrencyTest {
//
//    @Autowired
//    private AccountService accountService;
//
//    @Autowired
//    private AccountRepository accountRepository;
//
//    @Test
//    void testConcurrentWithdrawals() throws Exception{
//        ExecutorService executor = Executors.newFixedThreadPool(2);
//
//        CountDownLatch ready = new CountDownLatch(2);
//        CountDownLatch start = new CountDownLatch(1);
//
//        Callable<String> withdrawal1 = ()->{
//            ready.countDown();
//            start.await();
//
//            try{
//                accountService.withdrawal(4L,new BigDecimal("7000"));
//                return "7000 withdrawal SUCCESS";
//            }catch(Exception e){
//                return "7000 withdrawal FAILED";
//            }
//        };
//
//        Callable<String> withdrawal2 = ()->{
//            ready.countDown();
//            start.await();
//
//            try{
//                accountService.withdrawal(4L,new BigDecimal("5000"));
//                return "5000 withdrawal SUCCESS";
//            }catch(Exception e){
//                return "5000 withdrawal FAILED";
//            }
//        };
//
//        Future<String> res1 = executor.submit(withdrawal1);
//        Future<String> res2 = executor.submit(withdrawal2);
//
//        ready.await();
//        start.countDown();
////
////        System.out.println(res1.get());
////        System.out.println(res2.get());
//        String result1 = res1.get();
//        String result2 = res2.get();
//
//        System.out.println(result1);
//        System.out.println(result2);
//
//        long successCount = Stream.of(result1,result2).filter(result->result.endsWith("SUCCESS")).count();
//        assertEquals(1,successCount,"Exactly one withdrawal should succeed");
//        Account account = accountRepository.findById(4L).orElseThrow(() -> new RuntimeException("Account not found"));
//
//        BigDecimal expectedBalance = result1.endsWith("SUCCESS") ? new BigDecimal("3000") : new BigDecimal("5000");
//
//        assertEquals(0, account.getBalance().compareTo(expectedBalance), "Final account balance is incorrect");
//        executor.shutdown();
//        if(!executor.awaitTermination(10,TimeUnit.SECONDS)){
//            executor.shutdownNow();
//        }
//    }
//
//}


package com.banking.system;

import com.banking.system.entity.Account;
import com.banking.system.repository.AccountRepository;
import com.banking.system.service.AccountService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class WithdrawalConcurrencyTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void testConcurrentWithdrawals() throws Exception {
        runConcurrentWithdrawals(
                new BigDecimal("10000"),
                new BigDecimal("7000"),
                new BigDecimal("5000"),
                1
        );
        runConcurrentWithdrawals(
                new BigDecimal("10000"),
                new BigDecimal("6000"),
                new BigDecimal("4000"),
                2
        );

        runConcurrentWithdrawals(
                new BigDecimal("10000"),
                new BigDecimal("8000"),
                new BigDecimal("8000"),
                1
        );

        runConcurrentWithdrawals(
                new BigDecimal("10000"),
                new BigDecimal("5000"),
                new BigDecimal("5000"),
                2
        );
    }

    void runConcurrentWithdrawals(
            BigDecimal initialBalance,
            BigDecimal amount1,
            BigDecimal amount2,
            int expectedSuccessCount
    ) throws Exception {

        Account account = accountRepository.findById(4L)
                .orElseThrow(() ->
                        new RuntimeException("Account not found"));

        account.setBalance(initialBalance);
        accountRepository.saveAndFlush(account);

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Boolean> withdrawal1 = () -> {
            ready.countDown();
            start.await();

            try {
                accountService.withdrawal(4L, amount1);
                return true;
            } catch (Exception e) {
                return false;
            }
        };

        Callable<Boolean> withdrawal2 = () -> {
            ready.countDown();
            start.await();

            try {
                accountService.withdrawal(4L, amount2);
                return true;
            } catch (Exception e) {
                return false;
            }
        };

        try {
            Future<Boolean> result1 =
                    executor.submit(withdrawal1);

            Future<Boolean> result2 =
                    executor.submit(withdrawal2);

            ready.await();

            start.countDown();

            boolean success1 = result1.get();
            boolean success2 = result2.get();

            int successCount = (success1 ? 1 : 0) + (success2 ? 1 : 0);

            System.out.println("Initial Balance: " + initialBalance);
            System.out.println("Withdrawal 1: " + amount1 + " | Success: " + success1);
            System.out.println("Withdrawal 2: " + amount2 + " | Success: " + success2);
            System.out.println("Successful Withdrawals: " + successCount);

            assertEquals(expectedSuccessCount, successCount, "Unexpected number of successful withdrawals"
            );
            BigDecimal expectedBalance = initialBalance;

            if (success1) {
                expectedBalance = expectedBalance.subtract(amount1);
            }

            if (success2) {
                expectedBalance = expectedBalance.subtract(amount2);
            }
            Account updatedAccount =
                    accountRepository.findById(4L)
                            .orElseThrow(() ->
                                    new RuntimeException("Account not found"));

            assertEquals(0, updatedAccount.getBalance().compareTo(expectedBalance), "Final account balance is incorrect"
            );

            System.out.println("Final Balance: " + updatedAccount.getBalance());

            System.out.println("Scenario Passed!");

        } finally {
            start.countDown();

            executor.shutdown();

            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }
}

