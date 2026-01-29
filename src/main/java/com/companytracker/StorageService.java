package com.companytracker;

import java.util.List;

public interface StorageService {
    void addCompany(Company company) throws Exception;
    void updateCompanyWebsite(String companyName, String website, boolean hasWebsite) throws Exception;
    List<Company> getAllCompanies() throws Exception;
    Company getCompanyByName(String name) throws Exception;
    void removeCompany(String companyName) throws Exception;
    void close() throws Exception;
}
