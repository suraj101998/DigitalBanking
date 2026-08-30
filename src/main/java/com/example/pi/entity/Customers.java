package com.example.pi.entity;

import com.example.pi.entity.enums.AccountStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Customer entity.
 * Field names follow standard Java camelCase (doc item #31).
 * Unique constraints enforced at DB level (doc item #13).
 */
@Entity
@Table(
    name = "customers",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_customers_email",           columnNames = "email"),
        @UniqueConstraint(name = "uk_customers_identity_number", columnNames = "identity_number")
    }
)
public class Customers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Integer customerId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "customer_name", nullable = false, length = 100)
    private String name;

    /**
     * Still stored for backward compatibility with the legacy schema.
     * The authoritative balance lives in BankAccount.balance.
     */
    @Column(name = "account_number", length = 20)
    private String accountNumber;

    @Column(name = "identity_type", length = 30)
    private String idType;

    @Column(name = "identity_number", length = 50)
    private String idNum;

    @Column(name = "date_of_birth")
    private LocalDate dob;

    @Pattern(regexp = "\\d{10}")
    @Column(name = "mobile_number", length = 15)
    private String phone;

    @Email
    @Size(max = 150)
    @Column(name = "email", length = 150)
    private String email;

    @Size(max = 300)
    @Column(name = "address", length = 300)
    private String address;

    @Column(name = "sex", length = 10)
    private String sex;

    @Column(name = "user_id")
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", length = 20)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    /**
     * Soft-delete flag (doc item #76).
     * Physical deletion is never performed on customer records.
     * Use this flag to logically remove a customer while retaining financial history.
     */
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private java.time.Instant deletedAt;

    public Customers() {
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    /** Kept for code that still calls getCustomer_Id() — delegates to the corrected getter. */
    public Integer getCustomer_Id() { return customerId; }
    public void setCustomer_Id(Integer customer_Id) { this.customerId = customer_Id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    /** Backward-compat accessor used by legacy code that treats accountNumber as a long. */
    public long getAccount() {
        try { return accountNumber != null ? Long.parseLong(accountNumber) : 0L; } catch (NumberFormatException e) { return 0L; }
    }
    public void setAccount(long account) { this.accountNumber = String.valueOf(account); }

    public String getIdType() { return idType; }
    public void setIdType(String idType) { this.idType = idType; }

    public String getId_type() { return idType; }
    public void setId_type(String id_type) { this.idType = id_type; }

    public String getIdNum() { return idNum; }
    public void setIdNum(String idNum) { this.idNum = idNum; }

    public String getId_num() { return idNum; }
    public void setId_num(String id_num) { this.idNum = id_num; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public java.time.Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(java.time.Instant deletedAt) { this.deletedAt = deletedAt; }

    @Override
    public String toString() {
        return "Customers{customerId=" + customerId +
               ", name='" + name + '\'' +
               ", email='" + email + '\'' +
               ", accountStatus=" + accountStatus + '}';
    }
}
