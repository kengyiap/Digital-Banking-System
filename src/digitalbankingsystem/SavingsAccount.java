package digitalbankingsystem;

/**
 * SavingsAccount extends BankAccount with interest rate logic and minimum balance enforcement.
 * Demonstrates Inheritance and Polymorphism.
 * Moduled by: KEE KANG MING
 */
public class SavingsAccount extends BankAccount {

    private static final double MINIMUM_BALANCE = 10.0;
    private double interestRate;

    public SavingsAccount(String accountNumber, double balance,
                          String accountStatus, String customerId,
                          double interestRate) {
        super(accountNumber, balance, accountStatus, customerId);
        this.interestRate = interestRate;
    }

    // --- Polymorphic implementations ---

    @Override
    public String getAccountType() { return "Savings"; }

    @Override
    public double getAvailableBalance() {
        return Math.max(0, getBalance() - MINIMUM_BALANCE);
    }

    /** Overrides to enforce minimum balance rule. */
    @Override
    protected boolean checkAvailableFunds(double amount) {
        return (getBalance() - amount) >= MINIMUM_BALANCE;
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
            System.out.printf("[Error] Insufficient balance. Minimum RM %.2f must remain. Available: RM %.2f%n",
                    MINIMUM_BALANCE, getAvailableBalance());
            return false;
        }
        setBalance(getBalance() - amount);
        System.out.printf("[Success] RM %.2f withdrawn. New balance: RM %.2f%n", amount, getBalance());
        return true;
    }

    // --- Savings-specific features ---

    public double getInterestRate()              { return interestRate; }
    public void setInterestRate(double rate)     { this.interestRate = rate; }

    /**
     * Calculates and applies interest to balance.
     * Caller is responsible for recording the Transaction.
     * @return the interest amount credited.
     */
    public double applyInterest() {
        double interest = getBalance() * interestRate;
        setBalance(getBalance() + interest);
        System.out.printf("[Success] Interest of RM %.2f applied (%.2f%%). New balance: RM %.2f%n",
                interest, interestRate * 100, getBalance());
        return interest;
    }

    /** Prints projected interest without modifying balance. */
    public void previewMonthlyInterest() {
        double projected = getBalance() * interestRate;
        System.out.printf("[Preview] Monthly interest at %.2f%%: RM %.2f%n",
                interestRate * 100, projected);
    }

    @Override
    public void viewAccountDetails() {
        super.viewAccountDetails();
        System.out.printf("  Interest Rate   : %.2f%%%n", interestRate * 100);
        System.out.printf("  Minimum Balance : RM %.2f%n", MINIMUM_BALANCE);
        System.out.println("  -----------------------------------------------");
    }
}