package com.companytracker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class for CSVService functionality, focusing on the removeCompany feature
 */
public class CSVServiceTest {
    private static final String TEST_CSV_FILE = "companies.csv";
    private CSVService csvService;

    @Before
    public void setUp() throws IOException {
        // Clean up any existing test file
        cleanupTestFile();

        // Create a new CSVService instance (will create the CSV file)
        csvService = new CSVService();
    }

    @After
    public void tearDown() {
        // Clean up test file after each test
        cleanupTestFile();
    }

    private void cleanupTestFile() {
        File testFile = new File(TEST_CSV_FILE);
        if (testFile.exists()) {
            testFile.delete();
        }
    }

    @Test
    public void testRemoveCompany_Success() throws Exception {
        // Arrange: Add test companies
        Company company1 = new Company("Acme Corp");
        Company company2 = new Company("Tech Industries");
        Company company3 = new Company("Global Solutions");

        csvService.addCompany(company1);
        csvService.addCompany(company2);
        csvService.addCompany(company3);

        // Verify we have 3 companies
        List<Company> companiesBefore = csvService.getAllCompanies();
        assertEquals("Should have 3 companies before removal", 3, companiesBefore.size());

        // Act: Remove the middle company
        csvService.removeCompany("Tech Industries");

        // Assert: Verify company was removed
        List<Company> companiesAfter = csvService.getAllCompanies();
        assertEquals("Should have 2 companies after removal", 2, companiesAfter.size());

        // Verify the correct company was removed
        boolean techIndustriesExists = companiesAfter.stream()
            .anyMatch(c -> c.getName().equals("Tech Industries"));
        assertFalse("Tech Industries should not exist after removal", techIndustriesExists);

        // Verify other companies still exist
        boolean acmeExists = companiesAfter.stream()
            .anyMatch(c -> c.getName().equals("Acme Corp"));
        assertTrue("Acme Corp should still exist", acmeExists);

        boolean globalExists = companiesAfter.stream()
            .anyMatch(c -> c.getName().equals("Global Solutions"));
        assertTrue("Global Solutions should still exist", globalExists);
    }

    @Test
    public void testRemoveCompany_CaseInsensitive() throws Exception {
        // Arrange: Add a company
        Company company = new Company("Example Company");
        csvService.addCompany(company);

        // Act: Remove using different case
        csvService.removeCompany("EXAMPLE COMPANY");

        // Assert: Company should be removed despite case difference
        List<Company> companies = csvService.getAllCompanies();
        assertEquals("Company should be removed (case-insensitive)", 0, companies.size());
    }

    @Test
    public void testRemoveCompany_NotFound() throws Exception {
        // Arrange: Add a company
        Company company = new Company("Existing Company");
        csvService.addCompany(company);

        // Act & Assert: Try to remove non-existent company
        try {
            csvService.removeCompany("Non-Existent Company");
            fail("Should throw IOException when company not found");
        } catch (IOException e) {
            assertTrue("Error message should mention company not found",
                      e.getMessage().contains("Company not found"));
            assertTrue("Error message should include company name",
                      e.getMessage().contains("Non-Existent Company"));
        }

        // Verify original company still exists
        List<Company> companies = csvService.getAllCompanies();
        assertEquals("Original company should still exist", 1, companies.size());
    }

    @Test
    public void testRemoveCompany_FromEmptyList() throws Exception {
        // Arrange: Verify CSV is empty
        List<Company> companies = csvService.getAllCompanies();
        assertEquals("CSV should be empty initially", 0, companies.size());

        // Act & Assert: Try to remove from empty list
        try {
            csvService.removeCompany("Any Company");
            fail("Should throw IOException when removing from empty list");
        } catch (IOException e) {
            assertTrue("Error message should indicate company not found",
                      e.getMessage().contains("Company not found"));
        }
    }

    @Test
    public void testRemoveCompany_MultipleRemovals() throws Exception {
        // Arrange: Add multiple companies
        csvService.addCompany(new Company("Company A"));
        csvService.addCompany(new Company("Company B"));
        csvService.addCompany(new Company("Company C"));
        csvService.addCompany(new Company("Company D"));

        assertEquals("Should have 4 companies", 4, csvService.getAllCompanies().size());

        // Act: Remove companies one by one
        csvService.removeCompany("Company B");
        assertEquals("Should have 3 companies after first removal",
                    3, csvService.getAllCompanies().size());

        csvService.removeCompany("Company D");
        assertEquals("Should have 2 companies after second removal",
                    2, csvService.getAllCompanies().size());

        csvService.removeCompany("Company A");
        assertEquals("Should have 1 company after third removal",
                    1, csvService.getAllCompanies().size());

        csvService.removeCompany("Company C");
        assertEquals("Should have 0 companies after removing all",
                    0, csvService.getAllCompanies().size());
    }

    @Test
    public void testRemoveCompany_WithWebsiteData() throws Exception {
        // Arrange: Add company and set website
        Company company = new Company("Web Company");
        csvService.addCompany(company);
        csvService.updateCompanyWebsite("Web Company", "https://example.com", true);

        // Verify company has website data
        Company retrieved = csvService.getCompanyByName("Web Company");
        assertNotNull("Company should exist", retrieved);
        assertEquals("Website should be set", "https://example.com", retrieved.getWebsite());
        assertTrue("Should have website flag set", retrieved.isHasWebsite());

        // Act: Remove the company
        csvService.removeCompany("Web Company");

        // Assert: Company and its data should be removed
        List<Company> companies = csvService.getAllCompanies();
        assertEquals("Company should be removed", 0, companies.size());

        Company shouldBeNull = csvService.getCompanyByName("Web Company");
        assertNull("Company should not be found after removal", shouldBeNull);
    }

    @Test
    public void testRemoveCompany_VerifyCSVPersistence() throws Exception {
        // Arrange: Add companies
        csvService.addCompany(new Company("Persistent A"));
        csvService.addCompany(new Company("Persistent B"));
        csvService.addCompany(new Company("Persistent C"));

        // Act: Remove one company
        csvService.removeCompany("Persistent B");

        // Close the service
        csvService.close();

        // Create new service instance (should read from file)
        CSVService newService = new CSVService();

        // Assert: Verify changes were persisted
        List<Company> companies = newService.getAllCompanies();
        assertEquals("Should have 2 companies after reload", 2, companies.size());

        boolean persistentBExists = companies.stream()
            .anyMatch(c -> c.getName().equals("Persistent B"));
        assertFalse("Persistent B should not exist after reload", persistentBExists);

        newService.close();
    }
}
