package com.companytracker;

import java.util.List;

/**
 * Interface for company data storage operations.
 * Handles persistence of companies and their website search status.
 */
public interface StorageService {

    /** Add a company with name and employee count */
    void addCompany(String name, double employees) throws Exception;

    /** Get all companies */
    List<Company> getAllCompanies() throws Exception;

    /** Get companies that haven't been checked for websites yet */
    List<Company> getUncheckedCompanies() throws Exception;

    /** Update a company's website info and mark as checked */
    void updateWebsite(int companyId, String website, boolean hasWebsite) throws Exception;

    /** Reset all companies to unchecked state (clear website data) */
    void resetAllCompanies() throws Exception;

    /** Delete all companies from the database */
    void deleteAllCompanies() throws Exception;

    /** Close the storage connection */
    void close() throws Exception;
}
