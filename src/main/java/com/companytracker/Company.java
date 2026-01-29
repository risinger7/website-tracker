package com.companytracker;

public class Company {
    private int id;
    private String name;
    private String website;
    private boolean hasWebsite;
    private String createdAt;

    public Company() {
    }

    public Company(String name) {
        this.name = name;
        this.hasWebsite = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public boolean isHasWebsite() {
        return hasWebsite;
    }

    public void setHasWebsite(boolean hasWebsite) {
        this.hasWebsite = hasWebsite;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Company{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", website='" + website + '\'' +
                ", hasWebsite=" + hasWebsite +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
