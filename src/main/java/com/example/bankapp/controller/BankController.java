package com.example.bankapp.controller;

/*import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.model.Customer;
import com.example.bankapp.model.TransactionStatus;
import com.example.bankapp.service.BankService;
import com.example.bankapp.service.TransactionAuditService;
import jakarta.validation.Valid;*/


/*import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;

import java.util.List;*/


import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.model.Customer;
import com.example.bankapp.model.TransactionStatus;
import com.example.bankapp.service.BankService;
import com.example.bankapp.service.TransactionAuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;
    private final TransactionAuditService transactionAuditService;

    // 1. Main Dashboard - Table, Forms, and Summaries
    @GetMapping("/")
    public String dashboard(
            @RequestParam(required = false) String keyword,
            Model model) {
                model.addAttribute("customers", bankService.searchCustomers(keyword));
        model.addAttribute("allCustomers", bankService.getAllCustomers());
        model.addAttribute("transactions", bankService.getLatestTransactions());
        model.addAttribute("totalCustomers", bankService.getTotalCustomers());
        model.addAttribute("totalBalance", bankService.getTotalBalance());
        model.addAttribute("successCount", bankService.countTransactions(TransactionStatus.SUCCESS));
        model.addAttribute("failedCount", bankService.countTransactions(TransactionStatus.FAILED));
        model.addAttribute("customer", new Customer());
        model.addAttribute("transferRequest", new TransferRequest());

        return "bank";
    }

    // 2. Add New Customer
    @PostMapping("/customers")
    public String addCustomer(
            @Valid @ModelAttribute("customer") Customer customer,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please check the customer details and try again.");
            return "redirect:/";
        }

        bankService.addCustomer(customer);
        redirectAttributes.addFlashAttribute("success", "Customer added successfully.");
        return "redirect:/";
    }
// 3. Money Transfer with Rollback Audit Handling
    @PostMapping("/transfer")
    public String transfer(
            @Valid @ModelAttribute("transferRequest") TransferRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please enter valid transfer details.");
            return "redirect:/";
        }
        try {
            bankService.transfer(request);
            redirectAttributes.addFlashAttribute("success", "Transfer completed successfully.");
        } catch (RuntimeException exception) {
            transactionAuditService.recordFailure(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    exception.getMessage());

            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/";
    }

    // 4. View Specific Account Transaction History
    @GetMapping("/accounts/{no}/history")
    public String accountHistory(
            @PathVariable("no") String accountNumber,
            Model model) {

        model.addAttribute("customer", bankService.getCustomer(accountNumber));
        model.addAttribute("transactions", bankService.getAccountHistory(accountNumber));
        model.addAttribute("totalSent", bankService.getTotalSent(accountNumber));

        return "history";
    }
    // 5. Deposit Money Page & Handling
    @GetMapping("/deposit")
    public String depositPage(Model model) {
        model.addAttribute("customers", bankService.getAllCustomers());
        return "deposit";
    }

    @PostMapping("/deposit")
    public String deposit(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            RedirectAttributes redirectAttributes) {

        try {
            bankService.deposit(accountNumber, amount);
            redirectAttributes.addFlashAttribute("success", "Deposit completed successfully.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }

        return "redirect:/deposit";
    }
    // 6. Withdraw Money Page & Handling
    @GetMapping("/withdraw")
    public String withdrawPage(Model model) {
        model.addAttribute("customers", bankService.getAllCustomers());
        return "withdraw";
    }

    @PostMapping("/withdraw")
    public String withdraw(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            RedirectAttributes redirectAttributes) {

        try {
            bankService.withdraw(accountNumber, amount);
            redirectAttributes.addFlashAttribute("success", "Withdrawal completed successfully.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/withdraw";
    }

    // 7. Large Transfers Page
    @GetMapping("/large-transfers")
    public String largeTransfers(
            @RequestParam(defaultValue = "10000") BigDecimal min,
            Model model) {
                model.addAttribute("min", min);
        model.addAttribute("transfers", bankService.getSuccessfulTransfersAbove(min));

        return "large-transfers";
    }
}