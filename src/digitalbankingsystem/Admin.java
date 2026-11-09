package digitalbankingsystem;

import java.util.ArrayList;

/**
 * Admin handles all administrative operations.
 * Demonstrates Association with BankSystem (passed as parameter to keep coupling loose).
 * Moduled by: ADRIAN CHONG
 */
public class Admin {

    private String adminId;
    private String adminName;
    private String password;

    public Admin(String adminId, String adminName, String password) {
        this.adminId   = adminId;
        this.adminName = adminName;
        this.password  = password;
    }

    // --- Getters ---
    public String getAdminId()   { return adminId; }
    public String getAdminName() { return adminName; }
    public String getPassword()  { return password; }

    // --- Account Approval ---

    public void approveAccount(BankSystem bs, String accountNumber) {
        bs.approveAccount(accountNumber);
    }

    public void rejectAccount(BankSystem bs, String accountNumber) {
        bs.rejectAccount(accountNumber);
    }

    // --- Account Controls ---

    public void freezeAccount(BankAccount account) {
        if (account == null) { System.out.println("Account not found."); return; }
        if (!account.getAccountStatus().equalsIgnoreCase("Active")) {
            System.out.println("[Admin] Cannot freeze: Account " + account.getAccountNumber()
                    + " is '" + account.getAccountStatus() + "'. Only Active accounts can be frozen.");
            return;
        }
        account.setAccountStatus("Frozen");
        System.out.println("[Admin] Account " + account.getAccountNumber() + " has been FROZEN.");
    }

    public void unfreezeAccount(BankAccount account) {
        if (account == null) { System.out.println("Account not found."); return; }
        if (!account.getAccountStatus().equalsIgnoreCase("Frozen")) {
            System.out.println("[Admin] Account " + account.getAccountNumber() + " is not currently Frozen.");
            return;
        }
        account.setAccountStatus("Active");
        System.out.println("[Admin] Account " + account.getAccountNumber() + " has been UNFROZEN (Active).");
    }

    // --- Rate / Limit Settings ---

    public void setInterestRate(SavingsAccount account, double rate) {
        if (account == null || rate < 0) {
            System.out.println("[Error] Invalid account or rate.");
            return;
        }
        account.setInterestRate(rate);
        System.out.printf("[Admin] Interest rate for %s set to %.2f%%%n",
                account.getAccountNumber(), rate * 100);
    }

    public void setOverdraftLimit(CurrentAccount account, double limit) {
        if (account == null || limit < 0) {
            System.out.println("[Error] Invalid account or limit.");
            return;
        }
        account.setOverdraftLimit(limit);
        System.out.printf("[Admin] Overdraft limit for %s set to RM %.2f%n",
                account.getAccountNumber(), limit);
    }

    // --- Search ---

    public void searchCustomer(BankSystem bs, String query) {
        System.out.println("\n[ Search Results — Customer: \"" + query + "\" ]");
        boolean found = false;
        for (Customer c : bs.getAllCustomers()) {
            if (c.getCustomerId().equalsIgnoreCase(query)
                    || c.getFullName().toLowerCase().contains(query.toLowerCase())
                    || c.getUsername().equalsIgnoreCase(query)) {
                c.viewProfile();
                found = true;
            }
        }
        if (!found) System.out.println("  No matching customers found.");
    }

    public void searchAccount(BankSystem bs, String query) {
        System.out.println("\n[ Search Results — Account: \"" + query + "\" ]");
        boolean found = false;
        for (BankAccount acc : bs.getAllAccounts()) {
            if (acc.getAccountNumber().equalsIgnoreCase(query)
                    || acc.getCustomerId().equalsIgnoreCase(query)) {
                acc.viewAccountDetails();
                found = true;
            }
        }
        if (!found) System.out.println("  No matching accounts found.");
    }

    // --- Reports & Monitoring ---

    public void viewAllTransactions(BankSystem bs) {
        ArrayList<Transaction> list = bs.getAllTransactions();
        System.out.println("\n[ All Transactions — " + list.size() + " records ]");
        System.out.printf("  %-24s | %-10s | %-14s | %s%n",
                "Reference ID", "Type", "Amount", "Timestamp");
        System.out.println("  " + "-".repeat(72));
        if (list.isEmpty()) {
            System.out.println("  No transactions recorded.");
        } else {
            for (Transaction t : list) {
                t.displayTransaction(null);
            }
        }
    }

    /**
     * Flags any single transaction where amount > threshold.
     */
    public void monitorSuspiciousActivity(BankSystem bs, double threshold) {
        System.out.printf("%n[ Suspicious Activity Monitor — threshold: RM %.2f ]%n", threshold);
        boolean any = false;
        for (Transaction t : bs.getAllTransactions()) {
            if (t.getAmount() > threshold) {
                System.out.printf("  [!] FLAGGED  %s | %s | RM %.2f | %s%n",
                        t.getReferenceId(), t.getTransactionType(),
                        t.getAmount(), t.getTimestamp());
                any = true;
            }
        }
        if (!any) System.out.println("  No suspicious transactions detected.");
    }

    /**
     * Prints a system-wide administrative summary report.
     */
    public void generateReport(BankSystem bs) {
        String today = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));

        long totalCustomers  = bs.getAllCustomers().size();
        long activeCustomers = bs.getAllCustomers().stream()
                .filter(c -> c.getCustomerStatus().equalsIgnoreCase("Active")).count();
        long closedCustomers = totalCustomers - activeCustomers;

        long totalAccounts   = bs.getAllAccounts().size();
        long pendingAccounts = bs.getAllAccounts().stream()
                .filter(a -> a.getAccountStatus().equalsIgnoreCase("Pending")).count();
        long activeAccounts  = bs.getAllAccounts().stream()
                .filter(a -> a.getAccountStatus().equalsIgnoreCase("Active")).count();
        long frozenAccounts  = bs.getAllAccounts().stream()
                .filter(a -> a.getAccountStatus().equalsIgnoreCase("Frozen")).count();
        long closedAccounts  = bs.getAllAccounts().stream()
                .filter(a -> a.getAccountStatus().equalsIgnoreCase("Closed")).count();

        long monthlyTxnCount = bs.getAllTransactions().stream()
                .filter(t -> t.getTimestamp().startsWith(today)).count();
        double monthlyVolume = bs.getAllTransactions().stream()
                .filter(t -> t.getTimestamp().startsWith(today))
                .mapToDouble(Transaction::getAmount).sum();

        System.out.println("\n================================================================");
        System.out.println("                 ADMINISTRATIVE REPORT                         ");
        System.out.println("================================================================");
        System.out.printf("  Report Date        : %s%n", today);
        System.out.println("  --- Customers ---");
        System.out.printf("  Total              : %d  (Active: %d | Closed: %d)%n",
                totalCustomers, activeCustomers, closedCustomers);
        System.out.println("  --- Accounts ---");
        System.out.printf("  Total              : %d%n", totalAccounts);
        System.out.printf("  Pending            : %d%n", pendingAccounts);
        System.out.printf("  Active             : %d%n", activeAccounts);
        System.out.printf("  Frozen             : %d%n", frozenAccounts);
        System.out.printf("  Closed             : %d%n", closedAccounts);
        System.out.println("  --- This Month Transactions ---");
        System.out.printf("  Count              : %d%n", monthlyTxnCount);
        System.out.printf("  Total Volume       : RM %.2f%n", monthlyVolume);
        System.out.println("================================================================");
    }
}