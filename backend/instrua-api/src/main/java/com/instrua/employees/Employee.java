package com.instrua.employees;

import com.instrua.companies.Company;
import com.instrua.common.model.BaseEntity;
import com.instrua.users.Role;
import com.instrua.users.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "employees")
public class Employee extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_user_id")
    private User account;

    @Column(nullable = false)
    private String name;
    private String email;
    private String phone;
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role accessRole = Role.EMPLOYEE;

    @Column(nullable = false)
    private boolean active = true;

    protected Employee() { }

    public Employee(Company company, User account, String name, String email, String phone, String title, Role accessRole) {
        this.company = company;
        this.account = account;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.title = title;
        this.accessRole = accessRole == null ? Role.EMPLOYEE : accessRole;
    }

    public Company getCompany() { return company; }
    public User getAccount() { return account; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getTitle() { return title; }
    public Role getAccessRole() { return accessRole; }
    public boolean isActive() { return active; }
}
