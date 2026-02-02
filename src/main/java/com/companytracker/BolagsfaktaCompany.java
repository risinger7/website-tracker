package com.companytracker;

public class BolagsfaktaCompany {
    private String orgNr;
    private String postNr;
    private int companyCode;
    private String companyName;
    private String companyPopularName;
    private String address;
    private String companyUrl;
    private String sniText;
    private int responsibleCount;
    private double omsattning;
    private double antalAnstallda;
    private String bolagsform;
    private String postOrt;

    public String getOrgNr() {
        return orgNr;
    }

    public void setOrgNr(String orgNr) {
        this.orgNr = orgNr;
    }

    public String getPostNr() {
        return postNr;
    }

    public void setPostNr(String postNr) {
        this.postNr = postNr;
    }

    public int getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(int companyCode) {
        this.companyCode = companyCode;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyPopularName() {
        return companyPopularName;
    }

    public void setCompanyPopularName(String companyPopularName) {
        this.companyPopularName = companyPopularName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCompanyUrl() {
        return companyUrl;
    }

    public void setCompanyUrl(String companyUrl) {
        this.companyUrl = companyUrl;
    }

    public String getSniText() {
        return sniText;
    }

    public void setSniText(String sniText) {
        this.sniText = sniText;
    }

    public int getResponsibleCount() {
        return responsibleCount;
    }

    public void setResponsibleCount(int responsibleCount) {
        this.responsibleCount = responsibleCount;
    }

    public double getOmsattning() {
        return omsattning;
    }

    public void setOmsattning(double omsattning) {
        this.omsattning = omsattning;
    }

    public double getAntalAnstallda() {
        return antalAnstallda;
    }

    public void setAntalAnstallda(double antalAnstallda) {
        this.antalAnstallda = antalAnstallda;
    }

    public String getBolagsform() {
        return bolagsform;
    }

    public void setBolagsform(String bolagsform) {
        this.bolagsform = bolagsform;
    }

    public String getPostOrt() {
        return postOrt;
    }

    public void setPostOrt(String postOrt) {
        this.postOrt = postOrt;
    }

    @Override
    public String toString() {
        return "BolagsfaktaCompany{" +
                "orgNr='" + orgNr + '\'' +
                ", companyName='" + companyName + '\'' +
                ", address='" + address + '\'' +
                ", sniText='" + sniText + '\'' +
                ", antalAnstallda=" + antalAnstallda +
                '}';
    }
}
