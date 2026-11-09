package digitalbankingsystem;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Main — Console UI entry point for the Digital Banking System.
 * All menus are fully structured; every numeric input is validated.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static BankSystem bankSystem;
    private static Admin systemAdmin;

    // =====================================================================
    // Entry Point
    // =====================================================================

    public static void main(String[] args) {
        bankSystem  = new BankSystem();
        systemAdmin = new Admin("A001", "System Admin", "admin123");
        bankSystem.loadData();

        printBanner();

        while (true) {
            System.out.println("\n=======================================================");
            System.out.println("              DIGITAL BANKING SYSTEM                   ");
            System.out.println("=======================================================");
            System.out.println("  1. Customer Portal");
            System.out.println("  2. Administrator Portal");
            System.out.println("  3. Exit");
            System.out.println("=======================================================");
            String choice = prompt("Enter choice (1-3)");

            if      (choice.equals("1")) customerPreLogin();
            else if (choice.equals("2")) adminLogin();
            else if (choice.equals("3")) { bankSystem.saveData(); System.out.println("\nGoodbye!"); break; }
            else    System.out.println("[Error] Invalid choice. Please enter 1, 2, or 3.");
        }
        scanner.close();
    }

    // =====================================================================
    // Customer — Pre-Login
    // =====================================================================

    private static void customerPreLogin() {
        while (true) {
            System.out.println("\n--- CUSTOMER PORTAL ---");
            System.out.println("  1. Login");
            System.out.println("  2. Register New Customer");
            System.out.println("  3. Back to Main Menu");
            String choice = prompt("Enter choice");
            if      (choice.equals("1")) { Customer c = customerLogin(); if (c != null) customerDashboard(c); }
            else if (choice.equals("2")) customerRegister();
            else if (choice.equals("3")) break;
            else    System.out.println("[Error] Invalid choice.");
        }
    }

    private static Customer customerLogin() {
        System.out.println("\n--- CUSTOMER LOGIN ---");
        String user = prompt("Username");
        String pass = prompt("Password");
        return bankSystem.loginCustomer(user, pass);
    }

    private static void customerRegister() {
        System.out.println("\n--- REGISTER NEW CUSTOMER ---");
        String fullName = prompt("Full Name");
        if (fullName.trim().isEmpty()) { System.out.println("[Error] Name cannot be empty."); return; }

        String ic = prompt("IC Number (e.g. 010101-14-1234)");
        if (bankSystem.isIcTaken(ic)) { System.out.println("[Error] IC already registered."); return; }

        String phone    = prompt("Phone Number");
        String email    = prompt("Email Address");
        String username = prompt("Username");
        if (bankSystem.isUsernameTaken(username)) { System.out.println("[Error] Username is already taken."); return; }

        String password = prompt("Password (min 6 chars)");
        if (password.trim().length() < 6) { System.out.println("[Error] Password too short."); return; }

        String id = bankSystem.generateCustomerId();
        Customer newCustomer = new Customer(id, fullName, ic, phone, email, username, password, "Active");
        bankSystem.addCustomer(newCustomer);
        newCustomer.register();
        bankSystem.saveData();
    }

    // =====================================================================
    // Customer — Dashboard
    // =====================================================================

    private static void customerDashboard(Customer customer) {
        while (true) {
            System.out.println("\n=======================================================");
            System.out.println("  CUSTOMER DASHBOARD — " + customer.getFullName());
            System.out.println("=======================================================");
            System.out.println("  1. View Profile");
            System.out.println("  2. Update Profile");
            System.out.println("  3. Change Password");
            System.out.println("  4. My Accounts");
            System.out.println("  5. Transactions");
            System.out.println("  6. Logout");
            String choice = prompt("Enter choice (1-6)");

            switch (choice) {
                case "1": customer.viewProfile(); break;
                case "2": updateProfile(customer); break;
                case "3": changePassword(customer); break;
                case "4": myAccountsMenu(customer); break;
                case "5": transactionMenu(customer); break;
                case "6": System.out.println("Logged out. Goodbye, " + customer.getFullName() + "!"); return;
                default:  System.out.println("[Error] Invalid choice.");
            }
        }
    }

    // --- Profile Operations ---

    private static void updateProfile(Customer customer) {
        System.out.println("\n--- UPDATE PROFILE ---");
        System.out.println("  Leave blank to keep current value.");
        String newPhone = prompt("New Phone [" + customer.getPhoneNumber() + "]");
        String newEmail = prompt("New Email [" + customer.getEmail() + "]");
        if (newPhone.trim().isEmpty()) newPhone = customer.getPhoneNumber();
        if (newEmail.trim().isEmpty()) newEmail = customer.getEmail();
        customer.updateProfile(newPhone, newEmail);
        bankSystem.saveData();
    }

    private static void changePassword(Customer customer) {
        System.out.println("\n--- CHANGE PASSWORD ---");
        String oldPass = prompt("Current Password");
        String newPass = prompt("New Password (min 6 chars)");
        String confirm = prompt("Confirm New Password");
        if (!newPass.equals(confirm)) { System.out.println("[Error] Passwords do not match."); return; }
        if (customer.changePassword(oldPass, newPass)) bankSystem.saveData();
    }

    // --- My Accounts Menu ---

    private static void myAccountsMenu(Customer customer) {
        while (true) {
            System.out.println("\n--- MY ACCOUNTS ---");
            System.out.println("  1. View All My Accounts");
            System.out.println("  2. View Account Details");
            System.out.println("  3. Open New Account");
            System.out.println("  4. Close an Account");
            System.out.println("  5. Back");
            String choice = prompt("Enter choice");

            switch (choice) {
                case "1": listMyAccounts(customer); break;
                case "2": viewOneAccount(customer); break;
                case "3": openNewAccount(customer); break;
                case "4": closeMyAccount(customer); break;
                case "5": return;
                default:  System.out.println("[Error] Invalid choice.");
            }
        }
    }

    private static void listMyAccounts(Customer customer) {
        ArrayList<BankAccount> accounts = bankSystem.getAccountsByCustomer(customer.getCustomerId());
        System.out.println("\n[ Accounts for " + customer.getFullName() + " ]");
        if (accounts.isEmpty()) { System.out.println("  No accounts found."); return; }
        System.out.printf("  %-12s | %-10s | %-10s | %-10s%n", "Account No.", "Type", "Status", "Balance");
        System.out.println("  " + "-".repeat(52));
        for (BankAccount a : accounts) {
            System.out.printf("  %-12s | %-10s | %-10s | RM %.2f%n",
                    a.getAccountNumber(), a.getAccountType(), a.getAccountStatus(), a.getBalance());
        }
    }

    private static void viewOneAccount(Customer customer) {
        listMyAccounts(customer);
        String accNum = prompt("\nEnter Account Number to view");
        BankAccount acc = bankSystem.searchAccount(accNum);
        if (acc == null || !acc.getCustomerId().equals(customer.getCustomerId())) {
            System.out.println("[Error] Account not found or does not belong to you.");
            return;
        }
        acc.viewAccountDetails();
        if (acc instanceof SavingsAccount) ((SavingsAccount) acc).previewMonthlyInterest();
    }

    private static void openNewAccount(Customer customer) {
        System.out.println("\n--- OPEN NEW ACCOUNT ---");
        System.out.println("  1. Savings Account  (Interest-bearing, min balance RM 10)");
        System.out.println("  2. Current Account  (Overdraft enabled, RM 5/month fee)");
        String choice = prompt("Account type (1-2)");

        if (choice.equals("1")) {
            double initialDeposit = readDouble("Initial deposit (min RM 10.00)");
            if (initialDeposit < 10.0) { System.out.println("[Error] Minimum initial deposit is RM 10.00."); return; }
            String accNum = bankSystem.generateAccountNumber("SA");
            bankSystem.createAccount(new SavingsAccount(accNum, initialDeposit, "Pending", customer.getCustomerId(), 0.03));
            System.out.println("[Success] Savings Account " + accNum + " submitted. Awaiting admin approval.");

        } else if (choice.equals("2")) {
            double initialDeposit = readDouble("Initial deposit (min RM 0.00)");
            if (initialDeposit < 0) { System.out.println("[Error] Initial deposit cannot be negative."); return; }
            String accNum = bankSystem.generateAccountNumber("CA");
            bankSystem.createAccount(new CurrentAccount(accNum, initialDeposit, "Pending", customer.getCustomerId(), 500.0));
            System.out.println("[Success] Current Account " + accNum + " submitted. Awaiting admin approval.");
        } else {
            System.out.println("[Error] Invalid choice."); return;
        }
        bankSystem.saveData();
    }

    private static void closeMyAccount(Customer customer) {
        listMyAccounts(customer);
        String accNum = prompt("\nEnter Account Number to close");
        BankAccount acc = bankSystem.searchAccount(accNum);
        if (acc == null || !acc.getCustomerId().equals(customer.getCustomerId())) {
            System.out.println("[Error] Account not found or does not belong to you."); return;
        }
        System.out.println("[Warning] This will permanently close account " + accNum + ".");
        String confirm = prompt("Type YES to confirm");
        if (!confirm.equalsIgnoreCase("YES")) { System.out.println("Cancelled."); return; }
        if (bankSystem.closeAccount(accNum)) bankSystem.saveData();
    }

    // --- Transaction Menu ---

    private static void transactionMenu(Customer customer) {
        while (true) {
            System.out.println("\n--- TRANSACTIONS ---");
            System.out.println("  1. Deposit");
            System.out.println("  2. Withdraw");
            System.out.println("  3. Transfer Funds");
            System.out.println("  4. Apply Interest (Savings only)");
            System.out.println("  5. Transaction History");
            System.out.println("  6. Monthly Statement");
            System.out.println("  7. Back");
            String choice = prompt("Enter choice");
            switch (choice) {
                case "1": doDeposit(customer);   break;
                case "2": doWithdraw(customer);  break;
                case "3": doTransfer(customer);  break;
                case "4": doInterest(customer);  break;
                case "5": doHistory(customer);   break;
                case "6": doStatement(customer); break;
                case "7": return;
                default:  System.out.println("[Error] Invalid choice.");
            }
        }
    }

    private static BankAccount selectAccount(Customer customer) {
        listMyAccounts(customer);
        String accNum = prompt("\nEnter Account Number");
        BankAccount acc = bankSystem.searchAccount(accNum);
        if (acc == null || !acc.getCustomerId().equals(customer.getCustomerId())) {
            System.out.println("[Error] Account not found or does not belong to you.");
            return null;
        }
        return acc;
    }

    private static void doDeposit(Customer customer) {
        BankAccount acc = selectAccount(customer);
        if (acc == null) return;
        double amount = readDouble("Deposit amount (RM)");
        if (acc.deposit(amount)) {
            String refId = bankSystem.generateReferenceId("DEP");
            bankSystem.addTransaction(new Transaction(refId, "Deposit", amount, null, acc.getAccountNumber()));
            bankSystem.saveData();
        }
    }

    private static void doWithdraw(Customer customer) {
        BankAccount acc = selectAccount(customer);
        if (acc == null) return;
        double amount = readDouble("Withdrawal amount (RM)");
        if (acc.withdraw(amount)) {
            String refId = bankSystem.generateReferenceId("WDR");
            bankSystem.addTransaction(new Transaction(refId, "Withdraw", amount, acc.getAccountNumber(), null));
            bankSystem.saveData();
        }
    }

    private static void doTransfer(Customer customer) {
        System.out.println("\n--- TRANSFER FUNDS ---");
        BankAccount src = selectAccount(customer);
        if (src == null) return;
        String targetNum = prompt("Enter Target Account Number");
        BankAccount target = bankSystem.searchAccount(targetNum);
        if (target == null) { System.out.println("[Error] Target account not found."); return; }
        if (target.getAccountNumber().equals(src.getAccountNumber())) { System.out.println("[Error] Cannot transfer to the same account."); return; }
        double amount = readDouble("Transfer amount (RM)");
        if (src.transfer(target, amount)) {
            String refId = bankSystem.generateReferenceId("TRF");
            bankSystem.addTransaction(new Transaction(refId, "Transfer", amount, src.getAccountNumber(), target.getAccountNumber()));
            bankSystem.saveData();
        }
    }

    private static void doInterest(Customer customer) {
        BankAccount acc = selectAccount(customer);
        if (acc == null) return;
        if (!(acc instanceof SavingsAccount)) { System.out.println("[Error] Interest only applies to Savings Accounts."); return; }
        if (!acc.getAccountStatus().equalsIgnoreCase("Active")) { System.out.println("[Error] Account is not Active."); return; }
        SavingsAccount sa = (SavingsAccount) acc;
        sa.previewMonthlyInterest();
        String confirm = prompt("Apply interest now? (YES/NO)");
        if (!confirm.equalsIgnoreCase("YES")) { System.out.println("Cancelled."); return; }
        double interest = sa.applyInterest();
        String refId = bankSystem.generateReferenceId("INT");
        bankSystem.addTransaction(new Transaction(refId, "Interest", interest, null, acc.getAccountNumber()));
        bankSystem.saveData();
    }

    private static void doHistory(Customer customer) {
        System.out.println("\n--- TRANSACTION HISTORY ---");
        System.out.println("  Filter options:");
        System.out.println("  1. All transactions");
        System.out.println("  2. Filter by account");
        System.out.println("  3. Filter by type (Deposit / Withdraw / Transfer / Interest)");
        String choice = prompt("Filter choice");

        ArrayList<Transaction> all = bankSystem.getAllTransactions();
        ArrayList<BankAccount> myAccounts = bankSystem.getAccountsByCustomer(customer.getCustomerId());

        // Collect account numbers belonging to this customer
        ArrayList<String> myAccNums = new ArrayList<>();
        for (BankAccount a : myAccounts) myAccNums.add(a.getAccountNumber());

        ArrayList<Transaction> filtered = new ArrayList<>();
        for (Transaction t : all) {
            boolean mine = myAccNums.contains(t.getSourceAccount()) || myAccNums.contains(t.getTargetAccount());
            if (mine) filtered.add(t);
        }

        if (choice.equals("2")) {
            listMyAccounts(customer);
            String accNum = prompt("Account Number to filter");
            ArrayList<Transaction> byAcc = new ArrayList<>();
            for (Transaction t : filtered)
                if (accNum.equals(t.getSourceAccount()) || accNum.equals(t.getTargetAccount()))
                    byAcc.add(t);
            filtered = byAcc;
        } else if (choice.equals("3")) {
            String type = prompt("Type (Deposit/Withdraw/Transfer/Interest/Fee)");
            ArrayList<Transaction> byType = new ArrayList<>();
            for (Transaction t : filtered)
                if (t.getTransactionType().equalsIgnoreCase(type)) byType.add(t);
            filtered = byType;
        }

        System.out.printf("%n  %-24s | %-10s | %-14s | %s%n", "Reference ID", "Type", "Amount", "Timestamp");
        System.out.println("  " + "-".repeat(70));
        if (filtered.isEmpty()) {
            System.out.println("  No transactions found.");
        } else {
            for (Transaction t : filtered) {
                // Determine context: use the customer's account that is involved in this transaction.
                // This ensures transfer direction (+/-) is shown from the customer's perspective.
                String ctx = null;
                for (String an : myAccNums) {
                    if (an.equals(t.getSourceAccount()) || an.equals(t.getTargetAccount())) {
                        ctx = an;
                        break;
                    }
                }
                t.displayTransaction(ctx);
            }
        }
    }

    private static void doStatement(Customer customer) {
        BankAccount acc = selectAccount(customer);
        if (acc == null) return;
        String month = prompt("Enter Month (YYYY-MM)");
        if (!month.matches("\\d{4}-\\d{2}")) { System.out.println("[Error] Invalid format. Use YYYY-MM."); return; }
        Statement stmt = new Statement(month, acc.getAccountNumber());
        stmt.generateStatement(bankSystem.getAllTransactions(), acc);
    }

    // =====================================================================
    // Admin — Login & Portal
    // =====================================================================

    private static void adminLogin() {
        System.out.println("\n--- ADMIN LOGIN ---");
        String user = prompt("Admin Username");
        String pass = prompt("Password");
        if (!user.equals("admin") || !pass.equals(systemAdmin.getPassword())) {
            System.out.println("[Login] Admin authentication failed.");
            return;
        }
        System.out.println("[Login] Welcome, " + systemAdmin.getAdminName() + "!");
        adminDashboard();
    }

    private static void adminDashboard() {
        while (true) {
            System.out.println("\n=======================================================");
            System.out.println("              ADMIN CONTROL PANEL                      ");
            System.out.println("=======================================================");
            System.out.println("  1. Pending Account Requests");
            System.out.println("  2. Account Management");
            System.out.println("  3. Customer Management");
            System.out.println("  4. Configure Settings");
            System.out.println("  5. Reports & Monitoring");
            System.out.println("  6. Logout");
            String choice = prompt("Enter choice (1-6)");
            switch (choice) {
                case "1": pendingRequestsMenu();   break;
                case "2": accountMgmtMenu();       break;
                case "3": customerMgmtMenu();      break;
                case "4": configSettingsMenu();    break;
                case "5": reportsMenu();           break;
                case "6": System.out.println("Admin logged out."); return;
                default:  System.out.println("[Error] Invalid choice.");
            }
        }
    }

    // --- Pending Requests ---

    private static void pendingRequestsMenu() {
        ArrayList<BankAccount> pending = bankSystem.getPendingAccounts();
        System.out.println("\n[ Pending Account Requests — " + pending.size() + " item(s) ]");
        if (pending.isEmpty()) { System.out.println("  No pending requests."); return; }

        for (BankAccount a : pending) {
            Customer owner = bankSystem.searchCustomerById(a.getCustomerId());
            String ownerName = owner != null ? owner.getFullName() : "Unknown";
            System.out.printf("  %-12s | %-10s | RM %-10.2f | Customer: %s%n",
                    a.getAccountNumber(), a.getAccountType(), a.getBalance(), ownerName);
        }

        String accNum = prompt("\nEnter Account Number to action (or press Enter to skip)");
        if (accNum.trim().isEmpty()) return;
        System.out.println("  1. Approve    2. Reject");
        String action = prompt("Choice");
        if (action.equals("1"))      systemAdmin.approveAccount(bankSystem, accNum);
        else if (action.equals("2")) systemAdmin.rejectAccount(bankSystem, accNum);
        else System.out.println("[Error] Invalid choice.");
        bankSystem.saveData();
    }

    // --- Account Management ---

    private static void accountMgmtMenu() {
        while (true) {
            System.out.println("\n--- ACCOUNT MANAGEMENT ---");
            System.out.println("  1. Search Account");
            System.out.println("  2. Freeze Account");
            System.out.println("  3. Unfreeze Account");
            System.out.println("  4. Close Account");
            System.out.println("  5. Back");
            String choice = prompt("Enter choice");
            if (choice.equals("5")) return;

            if (choice.equals("1")) {
                systemAdmin.searchAccount(bankSystem, prompt("Enter Account Number or Customer ID"));
                continue;
            }

            String accNum = prompt("Enter Account Number");
            BankAccount acc = bankSystem.searchAccount(accNum);

            switch (choice) {
                case "2": systemAdmin.freezeAccount(acc);   bankSystem.saveData(); break;
                case "3": systemAdmin.unfreezeAccount(acc); bankSystem.saveData(); break;
                case "4":
                    String confirm = prompt("Confirm close account " + accNum + "? (YES/NO)");
                    if (confirm.equalsIgnoreCase("YES") && bankSystem.closeAccount(accNum))
                        bankSystem.saveData();
                    else System.out.println("Cancelled.");
                    break;
                default: System.out.println("[Error] Invalid choice.");
            }
        }
    }

    // --- Customer Management ---

    private static void customerMgmtMenu() {
        while (true) {
            System.out.println("\n--- CUSTOMER MANAGEMENT ---");
            System.out.println("  1. Search Customer");
            System.out.println("  2. Deactivate Customer (Soft Delete)");
            System.out.println("  3. Back");
            String choice = prompt("Enter choice");
            switch (choice) {
                case "1":
                    systemAdmin.searchCustomer(bankSystem, prompt("Enter Customer ID, Name, or Username"));
                    break;
                case "2":
                    String cId = prompt("Enter Customer ID to deactivate");
                    String confirm = prompt("This will close all accounts. Type YES to confirm");
                    if (confirm.equalsIgnoreCase("YES")) { bankSystem.deactivateCustomer(cId); bankSystem.saveData(); }
                    else System.out.println("Cancelled.");
                    break;
                case "3": return;
                default: System.out.println("[Error] Invalid choice.");
            }
        }
    }

    // --- Configure Settings ---

    private static void configSettingsMenu() {
        System.out.println("\n--- CONFIGURE SETTINGS ---");
        String accNum = prompt("Enter Account Number");
        BankAccount acc = bankSystem.searchAccount(accNum);
        if (acc == null) { System.out.println("[Error] Account not found."); return; }

        if (acc instanceof SavingsAccount) {
            double rate = readDouble("New Interest Rate (e.g. 0.05 = 5%)");
            if (rate < 0) { System.out.println("[Error] Rate cannot be negative."); return; }
            systemAdmin.setInterestRate((SavingsAccount) acc, rate);
        } else if (acc instanceof CurrentAccount) {
            double lim = readDouble("New Overdraft Limit (RM)");
            if (lim < 0) { System.out.println("[Error] Limit cannot be negative."); return; }
            systemAdmin.setOverdraftLimit((CurrentAccount) acc, lim);
        }
        bankSystem.saveData();
    }

    // --- Reports & Monitoring ---

    private static void reportsMenu() {
        while (true) {
            System.out.println("\n--- REPORTS & MONITORING ---");
            System.out.println("  1. System Report");
            System.out.println("  2. View All Transactions");
            System.out.println("  3. Monitor Suspicious Activity");
            System.out.println("  4. Back");
            String choice = prompt("Enter choice");
            switch (choice) {
                case "1": systemAdmin.generateReport(bankSystem); break;
                case "2": systemAdmin.viewAllTransactions(bankSystem); break;
                case "3":
                    double threshold = readDouble("Flag transactions above RM");
                    if (threshold <= 0) { System.out.println("[Error] Threshold must be positive."); break; }
                    systemAdmin.monitorSuspiciousActivity(bankSystem, threshold);
                    break;
                case "4": return;
                default: System.out.println("[Error] Invalid choice.");
            }
        }
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private static String prompt(String label) {
        System.out.print("  > " + label + ": ");
        return scanner.nextLine().trim();
    }

    /** Reads a double, returns -1 on invalid input. */
    private static double readDouble(String label) {
        try {
            return Double.parseDouble(prompt(label));
        } catch (NumberFormatException e) {
            System.out.println("[Error] Please enter a valid number.");
            return -1;
        }
    }

    private static void printBanner() {
        System.out.println("\n*******************************************************");
        System.out.println("*                                                     *");
        System.out.println("*         DIGITAL BANKING SYSTEM  v2.0               *");
        System.out.println("*                                                     *");
        System.out.println("*  Customer login : johndoe   / pass123              *");
        System.out.println("*  Admin login    : admin     / admin123             *");
        System.out.println("*                                                     *");
        System.out.println("*******************************************************");
    }
}