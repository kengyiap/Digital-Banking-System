package digitalbankingsystem;

/**
 * BankAccount is the abstract parent class for all account types.
 * Demonstrates Abstraction, Encapsulation, and Polymorphism.
 * Moduled by: KEE KANG MING
 */
public abstract class BankAccount {

    private String accountNumber;
    private double balance;
    private String accountStatus; // Pending / Active / Frozen / Closed
    private String customerId;

    public BankAccount(String accountNumber, double balance,
                       String accountStatus, String customerId) {
        this.accountNumber = accountNumber;
        this.balance       = balance;
        this.accountStatus = accountStatus;
        this.customerId    = customerId;
    }

    // --- Abstract methods (Polymorphism) ---

    /** Returns account type label, e.g. "Savings" or "Current". */
    public abstract String getAccountType();

    /**
     * Returns the amount the customer can actually use.
     * SavingsAccount reserves a minimum balance; CurrentAccount adds overdraft.
     */
    public abstract double getAvailableBalance();

    /**
     * Performs a withdrawal with type-specific rules.
     * @return true on success, false on failure.
     */
    public abstract boolean withdraw(double amount);

    // --- Getters / Setters (Encapsulation) ---

    public String getAccountNumber()  { return accountNumber; }
    public double getBalance()        { return balance; }
    protected void setBalance(double b) { this.balance = b; }

    public String getAccountStatus()  { return accountStatus; }
    public void setAccountStatus(String s) { this.accountStatus = s; }

    public String getCustomerId()     { return customerId; }

    // --- Shared helpers ---

    /** Returns true ONLY when status is exactly "Active". */
    protected boolean isAccountActive() {
        return "Active".equalsIgnoreCase(accountStatus);
    }

    /**
     * Checks whether the account has enough available funds.
     * Subclasses override this to apply type-specific rules (min balance, overdraft).
     */
    protected boolean checkAvailableFunds(double amount) {
        return balance >= amount;
    }

    // --- Shared operations ---

    /**
     * Deposits money. Fails for non-Active accounts.
     * @return true on success, false on failure.
     */
    public boolean deposit(double amount) {
        if (!isAccountActive()) {
            System.out.println("[Error] Deposit failed: Account status is '" + accountStatus + "'.");
            return false;
        }
        if (amount <= 0) {
            System.out.println("[Error] Deposit amount must be greater than zero.");
            return false;
        }
        balance += amount;
        System.out.printf("[Success] RM %.2f deposited. New balance: RM %.2f%n", amount, balance);
        return true;
    }

    /**
     * Transfers funds to a target account.
     * Uses checkAvailableFunds() which subclasses override.
     * @return true on success, false on failure.
     */
    public boolean transfer(BankAccount target, double amount) {
        if (!isAccountActive()) {
            System.out.println("[Error] Transfer failed: Your account is '" + accountStatus + "'.");
            return false;
        }
        if (target == null) {
            System.out.println("[Error] Transfer failed: Target account not found.");
            return false;
        }
        if (!target.isAccountActive()) {
            System.out.println("[Error] Transfer failed: Target account is '" + target.getAccountStatus() + "'.");
            return false;
        }
        if (amount <= 0) {
            System.out.println("[Error] Transfer amount must be greater than zero.");
            return false;
        }
        if (!checkAvailableFunds(amount)) {
            System.out.printf("[Error] Transfer failed: Insufficient available funds. Available: RM %.2f%n",
                    getAvailableBalance());
            return false;
        }
        balance -= amount;
        target.setBalance(target.getBalance() + amount);
        System.out.printf("[Success] RM %.2f transferred to account %s.%n",
                amount, target.getAccountNumber());
        return true;
    }

    /** Prints a summary card for this account. */
    public void viewAccountDetails() {
        System.out.println("  -----------------------------------------------");
        System.out.printf("  Account Number  : %s%n", accountNumber);
        System.out.printf("  Account Type    : %s%n", getAccountType());
        System.out.printf("  Account Status  : %s%n", accountStatus);
        System.out.printf("  Current Balance : RM %.2f%n", balance);
        System.out.printf("  Avail. Balance  : RM %.2f%n", getAvailableBalance());
    }
}