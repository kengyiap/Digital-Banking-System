package digitalbankingsystem;

/**
 * CurrentAccount extends BankAccount with overdraft and monthly fee logic.
 * Demonstrates Inheritance and Polymorphism.
 * Moduled by: KEE KANG MING
 */
public class CurrentAccount extends BankAccount {

    public static final double MONTHLY_FEE = 5.0;
    private double overdraftLimit;

    public CurrentAccount(String accountNumber, double balance,
                          String accountStatus, String customerId,
                          double overdraftLimit) {
        super(accountNumber, balance, accountStatus, customerId);
        this.overdraftLimit = overdraftLimit;
    }

    // --- Polymorphic implementations ---

    @Override
    public String getAccountType() { return "Current"; }

    @Override
    public double getAvailableBalance() {
        return getBalance() + overdraftLimit;
    }

    /** Overrides to include overdraft headroom. */
    @Override
    protected boolean checkAvailableFunds(double amount) {
        return (getBalance() + overdraftLimit) >= amount;
    }

    public boolean checkOverdraft(double amount) {
        return checkAvailableFunds(amount);
    }

    @Override
    public boolean withdraw(double amount) {
        if (!isAccountActive()) {
            System.out.println("[Error] Withdrawal failed: Account is '" + getAccountStatus() + "'.");
            return false;
        }
        if (amount <= 0) {
            System.out.println("[Error] Withdrawal amount must be greater than zero.");
            return false;
        }
        if (!checkAvailableFunds(amount)) {
            System.out.printf("[Error] Withdrawal failed. Amount exceeds balance + overdraft limit. Available: RM %.2f%n",
                    getAvailableBalance());
            return false;
        }
        setBalance(getBalance() - amount);
        System.out.printf("[Success] RM %.2f withdrawn. New balance: RM %.2f%n", amount, getBalance());
        if (getBalance() < 0) {
            System.out.printf("[Notice] Overdraft in use: RM %.2f of RM %.2f limit used.%n",
                    Math.abs(getBalance()), overdraftLimit);
        }
        return true;
    }

    // --- Current-specific features ---

    public double getOverdraftLimit()         { return overdraftLimit; }
    public void setOverdraftLimit(double lim) { this.overdraftLimit = lim; }

    /**
     * Deducts monthly service fee from balance.
     * Caller is responsible for recording the Transaction.
     * @return the fee amount deducted.
     */
    public double applyMonthlyFee() {
        setBalance(getBalance() - MONTHLY_FEE);
        System.out.printf("[System] Monthly service fee of RM %.2f applied. New balance: RM %.2f%n",
                MONTHLY_FEE, getBalance());
        return MONTHLY_FEE;
    }

    @Override
    public void viewAccountDetails() {
        super.viewAccountDetails();
        System.out.printf("  Overdraft Limit : RM %.2f%n", overdraftLimit);
        System.out.printf("  Monthly Fee     : RM %.2f%n", MONTHLY_FEE);
        System.out.println("  -----------------------------------------------");
    }
}