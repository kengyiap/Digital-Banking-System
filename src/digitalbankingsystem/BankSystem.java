package digitalbankingsystem;

import java.util.ArrayList;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * BankSystem is the central data engine.
 * Demonstrates Aggregation: it owns Customer, BankAccount, and Transaction collections.
 */
public class BankSystem {

    private ArrayList<Customer>    customerList;
    private ArrayList<BankAccount> accountList;
    private ArrayList<Transaction> transactionList;

    // Sequential reference ID counter
    private int    txnDailyCounter;
    private String txnLastDate;

    public BankSystem() {
        customerList    = new ArrayList<>();
        accountList     = new ArrayList<>();
        transactionList = new ArrayList<>();
        txnDailyCounter = 0;
        txnLastDate     = "";
    }

    // =====================================================================
    // ID Generation
    // =====================================================================

    public String generateCustomerId() {
        int max = 0;
        for (Customer c : customerList) {
            try { max = Math.max(max, Integer.parseInt(c.getCustomerId().replaceAll("\\D", ""))); }
            catch (NumberFormatException ignored) {}
        }
        return String.format("C%03d", max + 1);
    }

    public String generateAccountNumber(String typePrefix) {
        String prefix  = typePrefix.equalsIgnoreCase("SA") ? "SA-" : "CA-";
        int    baseNum = typePrefix.equalsIgnoreCase("SA") ? 1000  : 2000;
        int    max     = baseNum;
        for (BankAccount a : accountList) {
            if (a.getAccountNumber().startsWith(prefix)) {
                try { max = Math.max(max, Integer.parseInt(a.getAccountNumber().replace(prefix, ""))); }
                catch (NumberFormatException ignored) {}
            }
        }
        return prefix + (max + 1);
    }

    /** Generates TXN-YYYYMMDD-XXXX sequential reference IDs. */
    public String generateReferenceId(String typePrefix) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        if (!today.equals(txnLastDate)) {
            txnLastDate     = today;
            txnDailyCounter = 0;
        }
        txnDailyCounter++;
        return typePrefix + "-" + today + "-" + String.format("%04d", txnDailyCounter);
    }

    // =====================================================================
    // Validation
    // =====================================================================

    public boolean isUsernameTaken(String username) {
        for (Customer c : customerList)
            if (c.getUsername().equalsIgnoreCase(username)) return true;
        return false;
    }

    public boolean isIcTaken(String icNumber) {
        for (Customer c : customerList)
            if (c.getIcNumber().equals(icNumber)) return true;
        return false;
    }

    // =====================================================================
    // Customer Operations
    // =====================================================================

    public void addCustomer(Customer c) {
        if (c != null) customerList.add(c);
    }

    public Customer searchCustomerById(String id) {
        for (Customer c : customerList)
            if (c.getCustomerId().equalsIgnoreCase(id)) return c;
        return null;
    }

    /** Validates credentials and customerStatus before granting access. */
    public Customer loginCustomer(String username, String password) {
        for (Customer c : customerList) {
            if (c.getUsername().equals(username) && c.getPassword().equals(password)) {
                if (c.getCustomerStatus().equalsIgnoreCase("Closed")) {
                    System.out.println("[Login] This account is deactivated. Contact support.");
                    return null;
                }
                System.out.println("[Login] Welcome, " + c.getFullName() + "!");
                return c;
            }
        }
        System.out.println("[Login] Invalid username or password.");
        return null;
    }

    /**
     * Soft-deactivates a customer after verifying all linked accounts can be safely closed.
     * Blocks if any CurrentAccount has outstanding overdraft debt (negative balance).
     */
    public void deactivateCustomer(String customerId) {
        Customer c = searchCustomerById(customerId);
        if (c == null) { System.out.println("[Error] Customer not found."); return; }
        if (c.getCustomerStatus().equalsIgnoreCase("Closed")) {
            System.out.println("[Error] Customer " + c.getFullName() + " is already deactivated.");
            return;
        }

        // Pre-check: block if any linked account has outstanding overdraft debt
        ArrayList<BankAccount> linked = getAccountsByCustomer(customerId);
        for (BankAccount acc : linked) {
            if (acc.getAccountStatus().equalsIgnoreCase("Closed")) continue;
            if (acc instanceof CurrentAccount && acc.getBalance() < 0) {
                System.out.printf("[Error] Cannot deactivate: Account %s has outstanding overdraft debt of RM %.2f. " +
                        "Please resolve the debt before deactivating this customer.%n",
                        acc.getAccountNumber(), Math.abs(acc.getBalance()));
                return;
            }
        }

        // All accounts safe to close — proceed
        c.deactivate();
        for (BankAccount acc : linked) {
            if (!acc.getAccountStatus().equalsIgnoreCase("Closed")) {
                acc.setAccountStatus("Closed");
                System.out.println("[System] Account " + acc.getAccountNumber() + " closed.");
            }
        }
        System.out.println("[System] Customer " + c.getFullName() + " has been fully deactivated.");
    }

    public ArrayList<Customer> getAllCustomers() { return customerList; }

    // =====================================================================
    // Account Operations
    // =====================================================================

    public void createAccount(BankAccount acc) {
        if (acc != null) accountList.add(acc);
    }

    public BankAccount searchAccount(String accountNumber) {
        for (BankAccount a : accountList)
            if (a.getAccountNumber().equalsIgnoreCase(accountNumber)) return a;
        return null;
    }

    public ArrayList<BankAccount> getAccountsByCustomer(String customerId) {
        ArrayList<BankAccount> result = new ArrayList<>();
        for (BankAccount a : accountList)
            if (a.getCustomerId().equals(customerId)) result.add(a);
        return result;
    }

    public ArrayList<BankAccount> getPendingAccounts() {
        ArrayList<BankAccount> result = new ArrayList<>();
        for (BankAccount a : accountList)
            if (a.getAccountStatus().equalsIgnoreCase("Pending")) result.add(a);
        return result;
    }

    public void approveAccount(String accountNumber) {
        BankAccount acc = searchAccount(accountNumber);
        if (acc != null && acc.getAccountStatus().equalsIgnoreCase("Pending")) {
            acc.setAccountStatus("Active");
            System.out.println("[Admin] Account " + accountNumber + " APPROVED and set to Active.");
        } else {
            System.out.println("[Error] Account not found or not in Pending status.");
        }
    }

    public void rejectAccount(String accountNumber) {
        BankAccount acc = searchAccount(accountNumber);
        if (acc != null && acc.getAccountStatus().equalsIgnoreCase("Pending")) {
            acc.setAccountStatus("Closed");
            System.out.println("[Admin] Account " + accountNumber + " REJECTED.");
        } else {
            System.out.println("[Error] Account not found or not in Pending status.");
        }
    }

    /** Closes an account with balance-safety checks. */
    public boolean closeAccount(String accountNumber) {
        BankAccount acc = searchAccount(accountNumber);
        if (acc == null) { System.out.println("[Error] Account not found."); return false; }
        if (acc.getAccountStatus().equalsIgnoreCase("Closed")) {
            System.out.println("[Error] Account is already closed."); return false;
        }
        if (acc instanceof SavingsAccount && acc.getBalance() > 10.0) {
            System.out.printf("[Error] Please withdraw balance first (min RM 10.00 reserved). Current: RM %.2f%n",
                    acc.getBalance());
            return false;
        }
        if (acc instanceof CurrentAccount && acc.getBalance() < 0) {
            System.out.printf("[Error] Cannot close: outstanding overdraft debt of RM %.2f.%n",
                    Math.abs(acc.getBalance()));
            return false;
        }
        acc.setAccountStatus("Closed");
        System.out.println("[System] Account " + accountNumber + " has been closed successfully.");
        return true;
    }

    public ArrayList<BankAccount> getAllAccounts() { return accountList; }

    // =====================================================================
    // Transaction Operations
    // =====================================================================

    public void addTransaction(Transaction t) {
        transactionList.add(t);
        t.recordTransaction();
    }

    public ArrayList<Transaction> getAllTransactions() { return transactionList; }

    // =====================================================================
    // CSV Persistence
    // =====================================================================

    private String esc(String s)   { return s == null ? "" : s.replace(",", ";;"); }
    private String unesc(String s) { return s == null ? "" : s.replace(";;", ","); }

    public void saveData() {
        // customers.csv
        try (PrintWriter pw = new PrintWriter(new FileWriter("customers.csv"))) {
            for (Customer c : customerList) {
                pw.println(String.join(",",
                        c.getCustomerId(), esc(c.getFullName()), c.getIcNumber(),
                        c.getPhoneNumber(), c.getEmail(),
                        c.getUsername(), c.getPassword(), c.getCustomerStatus()));
            }
        } catch (IOException e) { System.out.println("[Error] Saving customers: " + e.getMessage()); }

        // accounts.csv
        try (PrintWriter pw = new PrintWriter(new FileWriter("accounts.csv"))) {
            for (BankAccount a : accountList) {
                String param = (a instanceof SavingsAccount)
                        ? String.valueOf(((SavingsAccount) a).getInterestRate())
                        : String.valueOf(((CurrentAccount) a).getOverdraftLimit());
                pw.println(String.join(",",
                        a.getAccountType().equals("Savings") ? "SA" : "CA",
                        a.getAccountNumber(),
                        String.valueOf(a.getBalance()),
                        a.getAccountStatus(),
                        a.getCustomerId(), param));
            }
        } catch (IOException e) { System.out.println("[Error] Saving accounts: " + e.getMessage()); }

        // transactions.csv
        try (PrintWriter pw = new PrintWriter(new FileWriter("transactions.csv"))) {
            for (Transaction t : transactionList) {
                pw.println(String.join(",",
                        t.getReferenceId(), t.getTransactionType(),
                        String.valueOf(t.getAmount()),
                        t.getSourceAccount() == null ? "" : t.getSourceAccount(),
                        t.getTargetAccount() == null ? "" : t.getTargetAccount(),
                        t.getStatus(), t.getTimestamp()));
            }
        } catch (IOException e) { System.out.println("[Error] Saving transactions: " + e.getMessage()); }

        // system_state.csv (TXN counter)
        try (PrintWriter pw = new PrintWriter(new FileWriter("system_state.csv"))) {
            pw.println(txnLastDate + "," + txnDailyCounter);
        } catch (IOException e) { System.out.println("[Error] Saving system state: " + e.getMessage()); }

        System.out.println("[Database] Data saved successfully.");
    }

    public void loadData() {
        System.out.println("[Database] Loading system data...");

        // Restore TXN counter
        try (BufferedReader br = new BufferedReader(new FileReader("system_state.csv"))) {
            String line = br.readLine();
            if (line != null) {
                String[] p = line.split(",", -1);
                txnLastDate     = p[0];
                txnDailyCounter = Integer.parseInt(p[1]);
            }
        } catch (Exception ignored) {}

        File cFile = new File("customers.csv");
        File aFile = new File("accounts.csv");

        if (cFile.exists() && aFile.exists()) {
            // Load customers
            try (BufferedReader br = new BufferedReader(new FileReader(cFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(",", -1);
                    if (p.length >= 8)
                        customerList.add(new Customer(p[0], unesc(p[1]), p[2], p[3], p[4], p[5], p[6], p[7]));
                }
            } catch (IOException e) { System.out.println("[Error] Loading customers: " + e.getMessage()); }

            // Load accounts
            try (BufferedReader br = new BufferedReader(new FileReader(aFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(",", -1);
                    if (p.length >= 6) {
                        if (p[0].equals("SA"))
                            accountList.add(new SavingsAccount(p[1], Double.parseDouble(p[2]), p[3], p[4], Double.parseDouble(p[5])));
                        else if (p[0].equals("CA"))
                            accountList.add(new CurrentAccount(p[1], Double.parseDouble(p[2]), p[3], p[4], Double.parseDouble(p[5])));
                    }
                }
            } catch (IOException e) { System.out.println("[Error] Loading accounts: " + e.getMessage()); }

            // Load transactions
            File tFile = new File("transactions.csv");
            if (tFile.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(tFile))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] p = line.split(",", -1);
                        if (p.length >= 7)
                            transactionList.add(new Transaction(
                                    p[0], p[1], Double.parseDouble(p[2]),
                                    p[3].isEmpty() ? null : p[3],
                                    p[4].isEmpty() ? null : p[4],
                                    p[5], p[6]));
                    }
                } catch (IOException e) { System.out.println("[Error] Loading transactions: " + e.getMessage()); }
            }

            System.out.printf("[Database] Loaded — Customers: %d, Accounts: %d, Transactions: %d%n",
                    customerList.size(), accountList.size(), transactionList.size());
        } else {
            initializeDemoData();
        }
    }

    private void initializeDemoData() {
        System.out.println("[Database] Initializing demo data...");
        Customer c1 = new Customer("C001", "John Doe",   "010101-14-1234", "0123456789", "john@mail.com",  "johndoe",   "pass123", "Active");
        Customer c2 = new Customer("C002", "Jane Smith", "020202-10-5678", "0198765432", "jane@mail.com",  "janesmith", "pass456", "Active");
        addCustomer(c1);
        addCustomer(c2);
        createAccount(new SavingsAccount("SA-1001",  500.0, "Active", "C001", 0.03));
        createAccount(new CurrentAccount("CA-2001", 1000.0, "Active", "C001", 500.0));
        createAccount(new SavingsAccount("SA-1002",  200.0, "Active", "C002", 0.03));
        System.out.println("[Database] Demo data ready. (johndoe/pass123, janesmith/pass456)");
        saveData();
    }
}