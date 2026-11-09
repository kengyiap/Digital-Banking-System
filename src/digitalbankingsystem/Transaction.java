package digitalbankingsystem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Transaction records a single successful money movement.
 * Demonstrates Association with BankAccount.
 * Moduled by: CHUA KENG YIAP
 */
public class Transaction {

    private String referenceId;
    private String transactionType; // Deposit / Withdraw / Transfer / Interest / Fee
    private double amount;
    private String sourceAccount;   // null for Deposit
    private String targetAccount;   // null for Withdraw
    private String status;          // Always "Success" (only successes are recorded)
    private String timestamp;       // "yyyy-MM-dd HH:mm:ss"

    /** Constructor for a new live transaction — auto-stamps timestamp. */
    public Transaction(String referenceId, String transactionType, double amount,
                       String sourceAccount, String targetAccount) {
        this.referenceId     = referenceId;
        this.transactionType = transactionType;
        this.amount          = amount;
        this.sourceAccount   = sourceAccount;
        this.targetAccount   = targetAccount;
        this.status          = "Success";
        this.timestamp       = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /** Constructor for reconstructing from CSV — preserves original timestamp. */
    public Transaction(String referenceId, String transactionType, double amount,
                       String sourceAccount, String targetAccount,
                       String status, String timestamp) {
        this.referenceId     = referenceId;
        this.transactionType = transactionType;
        this.amount          = amount;
        this.sourceAccount   = sourceAccount;
        this.targetAccount   = targetAccount;
        this.status          = status;
        this.timestamp       = timestamp;
    }

    // --- Getters ---
    public String getReferenceId()    { return referenceId; }
    public String getTransactionType(){ return transactionType; }
    public double getAmount()         { return amount; }
    public String getSourceAccount()  { return sourceAccount; }
    public String getTargetAccount()  { return targetAccount; }
    public String getStatus()         { return status; }
    public String getTimestamp()      { return timestamp; }

    /** Prints a short confirmation to console. */
    public void recordTransaction() {
        System.out.println("[System] Transaction " + referenceId + " recorded.");
    }

    /**
     * Prints a formatted table row.
     * @param contextAccount the account number whose statement is being viewed,
     *                       used to determine +/- direction for Transfer rows.
     */
    public void displayTransaction(String contextAccount) {
        String amtStr;
        switch (transactionType) {
            case "Deposit":
            case "Interest":
                amtStr = String.format("+%.2f", amount);
                break;
            case "Withdraw":
            case "Fee":
                amtStr = String.format("-%.2f", amount);
                break;
            case "Transfer":
                if (contextAccount != null && contextAccount.equals(targetAccount)) {
                    amtStr = String.format("+%.2f", amount);
                } else {
                    amtStr = String.format("-%.2f", amount);
                }
                break;
            default:
                amtStr = String.format("%.2f", amount);
        }
        System.out.printf("  %-24s | %-10s | RM %-12s | %s%n",
                referenceId, transactionType, amtStr, timestamp);
    }
}