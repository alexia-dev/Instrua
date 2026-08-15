package com.instrua.companies;

import com.instrua.common.model.BaseEntity;
import com.instrua.users.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "companies")
public class Company extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    private String email;
    private String phone;
    private String timezone = "America/Sao_Paulo";
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    protected Company() { }

    public Company(String name, String slug, String email, String phone, String timezone, User owner) {
        this.name = name;
        this.slug = slug.toLowerCase();
        this.email = email;
        this.phone = phone;
        if (timezone != null && !timezone.isBlank()) this.timezone = timezone;
        this.owner = owner;
    }

    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getTimezone() { return timezone; }
    public boolean isActive() { return active; }
    public User getOwner() { return owner; }
}
