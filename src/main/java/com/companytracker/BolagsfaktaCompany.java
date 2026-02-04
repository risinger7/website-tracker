package com.companytracker;

/**
 * Company data from Bolagsfakta API.
 * Only stores fields we actually use.
 */
public class BolagsfaktaCompany {
    private String companyName;
    private double antalAnstallda;  // Number of employees

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public double getAntalAnstallda() { return antalAnstallda; }
    public void setAntalAnstallda(double antalAnstallda) { this.antalAnstallda = antalAnstallda; }
}
