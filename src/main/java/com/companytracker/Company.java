package com.companytracker;

/**
 * Represents a company with website tracking information.
 * Stores data from Bolagsfakta API and website search results.
 */
public class Company {
    private int id;              // Database primary key
    private String name;         // Company name from Bolagsfakta
    private double employees;    // Number of employees
    private boolean isChecked;   // True if we've searched for this company's website
    private boolean hasWebsite;  // True if a matching website was found
    private String website;      // The matched website URL (null if not found)

    public Company() {
    }

    public Company(String name, double employees) {
        this.name = name;
        this.employees = employees;
        this.isChecked = false;
        this.hasWebsite = false;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getEmployees() { return employees; }
    public void setEmployees(double employees) { this.employees = employees; }

    public boolean isChecked() { return isChecked; }
    public void setChecked(boolean checked) { this.isChecked = checked; }

    public boolean hasWebsite() { return hasWebsite; }
    public void setHasWebsite(boolean hasWebsite) { this.hasWebsite = hasWebsite; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    @Override
    public String toString() {
        return String.format("Company[id=%d, name='%s', employees=%.0f, checked=%s, website=%s]",
                id, name, employees, isChecked, hasWebsite ? website : "none");
    }
}
