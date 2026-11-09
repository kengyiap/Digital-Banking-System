package digitalbankingsystem;

import java.util.ArrayList;

/**
 * Statement generates a monthly account summary from the global transaction list.
 * Demonstrates Composition: a Statement is built from Transaction objects.
 * Moduled by: CHUA KENG YIAP
 */
public class Statement {

    private String statementMonth; // "yyyy-MM"
    private String accountNumber;

    public Statement(String statementMonth, String accountNumber) {
        this.statementMonth = statementMonth;
        this.accountNumber  = accountNumber;
    }

    /**
     * Computes opening balance from transaction history and prints the full statement.
     * Opening Balance = currentBalance - net effect of transactions IN this month for this account.
     */
    public void generateStatement(ArrayList<Transaction> allTransactions, BankAccount account) {

        // --- Collect this-month transactions for this account ---
        ArrayList<Transaction> monthly = new ArrayList<>();
        for (Transaction t : allTransactions) {
            boolean isRelated = accountNumber.equals(t.getSourceAccount())
                             || accountNumber.equals(t.getTargetAccount());
            if (isRelated && t.getTimestamp().startsWith(statementMonth)) {
                monthly.add(t);
            }
        }

        // --- Compute net flow this month AND post-month to get accurate opening balance ---
        double netFlow         = 0;
        double netFlowPostMonth= 0;
        double totalDeposits   = 0;
        double totalWithdraw   = 0;
        double totalTransferIn = 0;
        double totalTransferOut= 0;
        double totalInterest   = 0;
        double totalFees       = 0;

        for (Transaction t : allTransactions) {
            boolean isRelated = accountNumber.equals(t.getSourceAccount())
                             || accountNumber.equals(t.getTargetAccount());
            if (!isRelated) continue;

            double amt = t.getAmount();
            String txnMonth = t.getTimestamp().length() >= 7 ? t.getTimestamp().substring(0, 7) : "";

            // Compute effect on this account's balance (positive = money in, negative = money out)
            double effect = 0;
            switch (t.getTransactionType()) {
                case "Deposit": case "Interest":
                    effect = +amt; break;
                case "Withdraw": case "Fee":
                    effect = -amt; break;
                case "Transfer":
                    effect = accountNumber.equals(t.getTargetAccount()) ? +amt : -amt; break;
            }

            if (txnMonth.equals(statementMonth)) {
                // This month's transactions — accumulate for totals display
                netFlow += effect;
                switch (t.getTransactionType()) {
                    case "Deposit":   totalDeposits   += amt; break;
                    case "Withdraw":  totalWithdraw   += amt; break;
                    case "Interest":  totalInterest   += amt; break;
                    case "Fee":       totalFees       += amt; break;
                    case "Transfer":
                        if (accountNumber.equals(t.getTargetAccount())) totalTransferIn  += amt;
                        else                                             totalTransferOut += amt;
                        break;
                }
            } else if (txnMonth.compareTo(statementMonth) > 0) {
                // Transactions AFTER the statement month — subtract from current balance to find closing
                netFlowPostMonth += effect;
            }
        }

        // closingBalance of statement month = currentBalance minus everything that happened after
        double closingBalance = account.getBalance() - netFlowPostMonth;
        double openingBalance = closingBalance - netFlow;


        // --- Print ---
        System.out.println("\n================================================================");
        System.out.println("                  MONTHLY ACCOUNT STATEMENT                    ");
        System.out.println("================================================================");
        System.out.printf("  Account Number  : %s%n", accountNumber);
        System.out.printf("  Account Type    : %s%n", account.getAccountType());
        System.out.printf("  Statement Month : %s%n", statementMonth);
        System.out.printf("  Opening Balance : RM %.2f%n", openingBalance);
        System.out.println("----------------------------------------------------------------");
        System.out.printf("  %-24s | %-10s | %-14s | %s%n",
                "Reference ID", "Type", "Amount", "Timestamp");
        System.out.println("----------------------------------------------------------------");

        if (monthly.isEmpty()) {
            System.out.println("  No transactions found for this month.");
        } else {
            for (Transaction t : monthly) {
                t.displayTransaction(accountNumber);
            }
        }

        System.out.println("----------------------------------------------------------------");
        System.out.printf("  Total Deposits        : RM %.2f%n", totalDeposits);
        if (totalInterest > 0)
            System.out.printf("  Total Interest        : RM %.2f%n", totalInterest);
        if (totalTransferIn > 0)
            System.out.printf("  Total Transfers In    : RM %.2f%n", totalTransferIn);
        System.out.printf("  Total Withdrawals Out : RM %.2f%n", totalWithdraw);
        if (totalTransferOut > 0)
            System.out.printf("  Total Transfers Out   : RM %.2f%n", totalTransferOut);
        if (totalFees > 0)
            System.out.printf("  Total Fees            : RM %.2f%n", totalFees);
        System.out.printf("  Closing Balance       : RM %.2f%n", closingBalance);
        System.out.println("================================================================");
    }
}