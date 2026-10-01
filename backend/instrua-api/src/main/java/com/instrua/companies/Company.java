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

    @Column(name = "niche", nullable = false)
    private String niche = "GENERAL";

    @Column(name = "vacancy_auction_enabled", nullable = false)
    private boolean vacancyAuctionEnabled = false;

    @Column(name = "vacancy_allow_discount", nullable = false)
    private boolean vacancyAllowDiscount = false;

    @Column(name = "vacancy_default_expiry_minutes", nullable = false)
    private int vacancyDefaultExpiryMinutes = 30;

    @Column(name = "vacancy_default_reservation_minutes", nullable = false)
    private int vacancyDefaultReservationMinutes = 5;

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
        this.niche = "GENERAL";
    }

    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getTimezone() { return timezone; }
    public boolean isActive() { return active; }
    public String getNiche() { return niche; }
    public void setNiche(String niche) { if (niche != null && !niche.isBlank()) this.niche = niche.trim().toUpperCase(); }
    public boolean isVacancyAuctionEnabled() { return vacancyAuctionEnabled; }
    public boolean isVacancyAllowDiscount() { return vacancyAllowDiscount; }
    public int getVacancyDefaultExpiryMinutes() { return vacancyDefaultExpiryMinutes; }
    public int getVacancyDefaultReservationMinutes() { return vacancyDefaultReservationMinutes; }
    public void configureVacancyAuction(boolean enabled, boolean allowDiscount, int expiryMinutes, int reservationMinutes) {
        if (expiryMinutes < 1 || reservationMinutes < 1) throw new IllegalArgumentException("Os tempos da oportunidade devem ser positivos");
        this.vacancyAuctionEnabled = enabled;
        this.vacancyAllowDiscount = allowDiscount && enabled;
        this.vacancyDefaultExpiryMinutes = expiryMinutes;
        this.vacancyDefaultReservationMinutes = reservationMinutes;
    }
    public User getOwner() { return owner; }
}
