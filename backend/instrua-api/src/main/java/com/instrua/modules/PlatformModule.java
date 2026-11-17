package com.instrua.modules;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "platform_modules")
public class PlatformModule {
    @Id
    private String code;
    @Column(nullable = false) private String name;
    @Column(nullable = false, length = 1000) private String description;
    @Column(nullable = false) private String category;
    @Column(name = "required_module", nullable = false) private boolean requiredModule;
    @Column(nullable = false) private boolean active;
    @Column(name = "display_order", nullable = false) private int displayOrder;

    protected PlatformModule() { }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public boolean isRequiredModule() { return requiredModule; }
    public boolean isActive() { return active; }
    public int getDisplayOrder() { return displayOrder; }
}
