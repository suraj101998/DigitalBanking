package com.example.pi.dto.response;

import com.example.pi.entity.enums.AccountStatus;
import java.time.LocalDate;

/**
 * Customer response DTO — no JPA entity ever returned directly from REST endpoints (doc item #12).
 */
public class CustomerResponse {

    private Integer customerId;
    private String name;
    private String accountNumber;
    private String email;
    private String phone;
    private String address;
    private String sex;
    private LocalDate dob;
    private String idType;
    private String idNum;
    private AccountStatus accountStatus;

    public CustomerResponse() {}

    public CustomerResponse(Integer customerId, String name, String accountNumber, String email,
                             String phone, String address, String sex, LocalDate dob,
                             String idType, String idNum, AccountStatus accountStatus) {
        this.customerId    = customerId;
        this.name          = name;
        this.accountNumber = accountNumber;
        this.email         = email;
        this.phone         = phone;
        this.address       = address;
        this.sex           = sex;
        this.dob           = dob;
        this.idType        = idType;
        this.idNum         = idNum;
        this.accountStatus = accountStatus;
    }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getIdType() { return idType; }
    public void setIdType(String idType) { this.idType = idType; }

    public String getIdNum() { return idNum; }
    public void setIdNum(String idNum) { this.idNum = idNum; }

    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }
}
