package digitalbankingsystem;

/**
 * The Customer class represents a registered bank user.
 * Demonstrates Encapsulation via private attributes and public accessors.
 * Moduled by: SEE MING SHIN
 */
public class Customer {
    private String customerId;
    private String fullName;
    private String icNumber;
    private String phoneNumber;
    private String email;
    private String username;
    private String password;
    private String customerStatus; // "Active" / "Closed"

    public Customer(String customerId, String fullName, String icNumber,
                    String phoneNumber, String email, String username,
                    String password, String customerStatus) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.icNumber = icNumber;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.username = username;
        this.password = password;
        this.customerStatus = customerStatus;
    }

    // --- Getters and Setters ---
    public String getCustomerId()   { return customerId; }
    public String getFullName()     { return fullName; }
    public void   setFullName(String v)  { this.fullName = v; }
    public String getIcNumber()     { return icNumber; }
    public String getPhoneNumber()  { return phoneNumber; }
    public void   setPhoneNumber(String v) { this.phoneNumber = v; }
    public String getEmail()        { return email; }
    public void   setEmail(String v)     { this.email = v; }
    public String getUsername()     { return username; }
    public void   setUsername(String v)  { this.username = v; }
    public String getPassword()     { return password; }
    public void   setPassword(String v)  { this.password = v; }
    public String getCustomerStatus()    { return customerStatus; }
    public void   setCustomerStatus(String v) { this.customerStatus = v; }

    /** Prints a registration confirmation. */
    public void register() {
        System.out.println("[Success] Customer " + fullName + " (ID: " + customerId + ") registered successfully.");
    }

    /** Updates phone and email. */
    public void updateProfile(String newPhone, String newEmail) {
        this.phoneNumber = newPhone;
        this.email       = newEmail;
        System.out.println("[Profile] Contact information updated successfully.");
    }

    /**
     * Changes the password after validating the old one.
     * @return true if successful, false otherwise.
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        if (!this.password.equals(oldPassword)) {
            System.out.println("[Error] Current password is incorrect.");
            return false;
        }
        if (newPassword == null || newPassword.trim().length() < 6) {
            System.out.println("[Error] New password must be at least 6 characters.");
            return false;
        }
        this.password = newPassword;
        System.out.println("[Success] Password changed successfully.");
        return true;
    }

    /** Soft-deactivates this customer profile. */
    public void deactivate() {
        this.customerStatus = "Closed";
        System.out.println("[System] Customer profile '" + fullName + "' has been deactivated.");
    }

    /** Displays full profile (password excluded). */
    public void viewProfile() {
        System.out.println("\n==========================================");
        System.out.println("            CUSTOMER PROFILE              ");
        System.out.println("==========================================");
        System.out.printf("  Customer ID : %s%n", customerId);
        System.out.printf("  Full Name   : %s%n", fullName);
        System.out.printf("  IC Number   : %s%n", icNumber);
        System.out.printf("  Phone       : %s%n", phoneNumber);
        System.out.printf("  Email       : %s%n", email);
        System.out.printf("  Username    : %s%n", username);
        System.out.printf("  Status      : %s%n", customerStatus);
        System.out.println("==========================================");
    }
}