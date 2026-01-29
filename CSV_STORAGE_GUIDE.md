# CSV Storage Implementation

## What Changed

The app now uses CSV file storage instead of SQLite database for easier testing and portability.

## Files Created

1. **[StorageService.java](src/main/java/com/companytracker/StorageService.java)** - Interface for storage operations
2. **[CSVService.java](src/main/java/com/companytracker/CSVService.java)** - CSV file implementation
3. **[companies_test.csv](companies_test.csv)** - Sample test data

## Files Modified

1. **[App.java](src/main/java/com/companytracker/App.java)** - Now uses `StorageService` interface with `CSVService`
2. **[DatabaseService.java](src/main/java/com/companytracker/DatabaseService.java)** - Implements `StorageService` interface

## How CSV Storage Works

### File Format
The CSV file (`companies.csv`) uses this structure:
```csv
id,name,website,has_website,created_at
1,Acme Corp,https://acme.com,true,2024-01-15 10:30:00
2,Test Inc,,false,2024-01-16 14:22:00
```

### Features
- **Auto-creation**: CSV file is created automatically if it doesn't exist
- **Auto-increment IDs**: IDs are generated automatically
- **Timestamps**: Creation timestamps are added automatically
- **CSV escaping**: Handles company names with commas, quotes, etc.
- **Case-insensitive search**: Find companies regardless of case

## Switching Between Storage Types

To switch back to database storage, change one line in [App.java](src/main/java/com/companytracker/App.java:14):

**Current (CSV):**
```java
this.storageService = new CSVService();
```

**For Database:**
```java
this.storageService = new DatabaseService();
```

## Testing the App

### Quick Test with Sample Data
1. Rename `companies_test.csv` to `companies.csv` to use test data
2. Run the app
3. Choose option 3 to list all companies

### Start Fresh
1. Delete `companies.csv` if it exists
2. Run the app
3. Add companies and test functionality

## Running the Application

### If you have Maven installed:
```bash
mvn clean compile exec:java -Dexec.mainClass="com.companytracker.App"
```

### Using the compiled classes (if already compiled):
```bash
java -cp "target/classes;path/to/dependencies/*" com.companytracker.App
```

### Building a standalone JAR:
```bash
mvn clean package
java -jar target/company-tracker-1.0-SNAPSHOT.jar
```

## Advantages of CSV Storage

- **No database setup required**
- **Human-readable format**
- **Easy to inspect and edit manually**
- **Portable** - just copy the CSV file
- **Version control friendly**
- **Perfect for testing and development**

## Future: Database Migration

When ready to switch to a real database:
1. Change the initialization in [App.java](src/main/java/com/companytracker/App.java) back to `DatabaseService`
2. Optionally: Create a migration script to import CSV data into the database
3. Both implementations share the same `StorageService` interface, so no other code changes needed

## Todo Items

- [DONE] Test app using CSV file instead of database
- [PENDING] Find companies via external website API (for later)
