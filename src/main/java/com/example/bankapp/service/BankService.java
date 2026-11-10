package com.example.bankapp.service;

import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.exception.AccountNotFoundException;
import com.example.bankapp.exception.BankException;
import com.example.bankapp.exception.InsufficientBalanceException;
import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.Customer;
import com.example.bankapp.model.TransactionStatus;
import com.example.bankapp.repository.BankTransactionRepository;
import com.example.bankapp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;

    @Transactional
    public void createCustomer(Customer customer) {
        customerRepository.save(customer);
    }

    @Transactional
    public void addCustomer(Customer customer) {
        createCustomer(customer);
    }

    @Transactional(rollbackFor = Exception.class)
    public BankTransaction transfer(TransferRequest request) {
        String senderAcc = request.getFromAccount();
        String receiverAcc = request.getToAccount();
        BigDecimal amount = request.getAmount();

        if (senderAcc.equals(receiverAcc)) {
            throw new BankException("Sender and receiver must be different accounts");
        }

        if (customerRepository.countAccounts(senderAcc) == 0) {
            throw new AccountNotFoundException("Sender account " + senderAcc + " not found");
        }
        if (customerRepository.countAccounts(receiverAcc) == 0) {
            throw new AccountNotFoundException("Receiver account " + receiverAcc + " not found");
        }

        BigDecimal fee = amount.multiply(new BigDecimal("0.01"));
        BigDecimal totalDeduction = amount.add(fee);

        int updatedRows = customerRepository.debit(senderAcc, totalDeduction);
        if (updatedRows == 0) {
            throw new InsufficientBalanceException("Insufficient balance in account " + senderAcc + " to cover transfer amount and 1% fee");
        }

        if (request.isSimulateFailure()) {
            throw new BankException("Simulated system failure after the withdrawal");
        }

        customerRepository.credit(receiverAcc, amount);

        customerRepository.credit("999999", fee);

        BankTransaction successTx = new BankTransaction();
        successTx.setFromAccount(senderAcc);
        successTx.setToAccount(receiverAcc);
        successTx.setAmount(amount);
        successTx.setStatus(TransactionStatus.SUCCESS);
        successTx.setDescription("Transfer completed with LKR " + fee + " fee");
        successTx.setCreatedAt(LocalDateTime.now());

        return transactionRepository.save(successTx);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAllOrdered();
    }

    public List<Customer> searchCustomers(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return customerRepository.searchByName(keyword);
        }
        return getAllCustomers();
    }

    public Customer getCustomer(String accountNumber) {
        Customer customer = customerRepository.getByAccountNumber(accountNumber);
        if (customer == null) {
            throw new AccountNotFoundException("Account " + accountNumber + " not found");
        }
        return customer;
    }

    public List<BankTransaction> getLatestTransactions() {
        return transactionRepository.findAllLatest();
    }

    public List<BankTransaction> getAccountHistory(String accountNumber) {
        return transactionRepository.findHistory(accountNumber);
    }

    public long getTotalCustomers() {
        return customerRepository.count();
    }

    public BigDecimal getTotalBalance() {
        BigDecimal total = customerRepository.getTotalBalance();
        return total != null ? total : BigDecimal.ZERO;
    }

    public long countTransactions(TransactionStatus status) {
        return transactionRepository.countByStatus(status);
    }

    public BigDecimal getTotalSent(String accountNumber) {
        return transactionRepository.getTotalSent(accountNumber);
    }

    public List<BankTransaction> getSuccessfulTransfersAbove(BigDecimal minAmount) {
        return transactionRepository.findSuccessfulTransfersAbove(minAmount);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deposit(String accountNumber, BigDecimal amount) {
        if (customerRepository.countAccounts(accountNumber) == 0) {
            throw new AccountNotFoundException("Account " + accountNumber + " not found");
        }
        customerRepository.credit(accountNumber, amount);
    }

    @Transactional(rollbackFor = Exception.class)
    public void withdraw(String accountNumber, BigDecimal amount) {
        int updatedRows = customerRepository.debit(accountNumber, amount);
        if (updatedRows == 0) {
            throw new InsufficientBalanceException("Insufficient balance in account " + accountNumber);
        }
    }
}