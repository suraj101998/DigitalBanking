package com.example.pi.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Application user entity.
 *
 * Added fields for account-lockout (doc item #65):
 *  - failedLoginAttempts: incremented on every bad-password attempt
 *  - lockedUntil: when set, login is rejected until this instant passes
 *  - accountNonLocked: convenience flag — true while account is not locked
 *
 * When failedLoginAttempts reaches MAX_FAILED_ATTEMPTS the account is locked for
 * LOCK_DURATION_MINUTES minutes.
 */
@Entity
@Table(name = "users")
public class User {

    public static final int MAX_FAILED_ATTEMPTS  = 5;
    public static final int LOCK_DURATION_MINUTES = 15;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(unique = true, nullable = false)
    private String userName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private String roles;

    @Column
    private Integer customerId;

    /** Number of consecutive failed login attempts. Reset to 0 on success. */
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    /**
     * If non-null, the account is locked until this timestamp.
     * When current time > lockedUntil the lock is automatically released on the next login.
     */
    @Column(name = "locked_until")
    private Instant lockedUntil;

    public User() {
    }

    public User(String userName, String password, String roles) {
        this.userName = userName;
        this.password = password;
        this.roles = roles;
        this.active = true;
    }

    /** Returns true if the account is currently locked (lockedUntil is in the future). */
    public boolean isAccountLocked() {
        return lockedUntil != null && Instant.now().isBefore(lockedUntil);
    }

    /** Increments failed attempts and locks the account if the threshold is reached. */
    public void recordFailedLogin() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= MAX_FAILED_ATTEMPTS) {
            this.lockedUntil = Instant.now().plusSeconds(LOCK_DURATION_MINUTES * 60L);
        }
    }

    /** Resets failed attempt counter and clears any lock after a successful login. */
    public void recordSuccessfulLogin() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public Instant getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(Instant lockedUntil) { this.lockedUntil = lockedUntil; }
}