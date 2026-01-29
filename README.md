# Company Website Checker

A Java application that helps you track companies and determine if they have websites using search engine APIs.

## Features

- Store company names in a local SQLite database
- Check if companies have websites using search engine APIs
- View all tracked companies and their website status
- Batch check all companies at once

## Project Structure

```
java/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── companytracker/
│                   ├── App.java              # Main application
│                   ├── Company.java          # Company data model
│                   ├── DatabaseService.java  # SQLite database operations
│                   └── SearchService.java    # Search engine API integration
├── pom.xml                                   # Maven dependencies
├── mvnw.cmd                                  # Maven Wrapper (Windows)
└── README.md                                 # This file
```

## Prerequisites

- Java 8 or higher (you have Java 8 installed)
- No Maven installation needed (Maven Wrapper included)

## Getting Started

### 1. Build the project

On Windows (Command Prompt or PowerShell):

```bash
mvnw.cmd clean package
```

On Linux/Mac:

```bash
./mvnw clean package
```

This will:

- Download Maven automatically (first time only)
- Download all dependencies (OkHttp, Gson, SQLite JDBC)
- Compile your code
- Create an executable JAR file

### 2. Run the application

```bash
java -jar target/company-website-checker-1.0-SNAPSHOT.jar
```

Or use the Maven wrapper:

```bash
mvnw.cmd exec:java -Dexec.mainClass="com.companytracker.App"
```

## Setting Up Search Engine API

Currently, the app uses mock search results. To use a real search engine API:

### Option 1: Google Custom Search API (Recommended)

1. Get an API key from [Google Cloud Console](https://console.cloud.google.com/)
2. Create a Custom Search Engine at [Programmable Search Engine](https://programmablesearchengine.google.com/)
3. Update [SearchService.java](src/main/java/com/companytracker/SearchService.java):
    ```java
    private String apiKey = "YOUR_API_KEY";
    private String searchEngineId = "YOUR_SEARCH_ENGINE_ID";
    ```

### Option 2: Other Search APIs

You can integrate with:

- Bing Search API
- DuckDuckGo API
- SerpApi
- Custom web scraping (be mindful of rate limits and terms of service)

## Usage

When you run the application, you'll see a menu:

```
=== Company Website Checker ===
Choose an option:
1. Add a company
2. Check if a company has a website
3. List all companies
4. Check all companies for websites
5. Exit
```

### Example Workflow

1. Add companies to track
2. Use option 2 or 4 to check their websites
3. View results with option 3

## Development in VSCode

### Recommended Extensions

1. **Extension Pack for Java** (Microsoft)
    - Includes Language Support for Java, Debugger, Test Runner, Maven, and Project Manager

2. **Spring Boot Extension Pack** (optional, if you expand to Spring Boot later)

### Running in VSCode

1. Open the Command Palette (Ctrl+Shift+P / Cmd+Shift+P)
2. Type "Java: Run Java Application"
3. Select `App.java`

### Debugging

1. Set breakpoints in your code by clicking left of line numbers
2. Press F5 or go to Run and Debug panel
3. Select "Java" and click "Run"

## Database

The application creates a local SQLite database file called `companies.db` in the project root. This file persists your data between runs.

### Database Schema

```sql
CREATE TABLE companies (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    website TEXT,
    has_website INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Dependencies

- **OkHttp 4.12.0**: Modern HTTP client for API calls
- **Gson 2.10.1**: JSON parsing library
- **SQLite JDBC 3.45.0.0**: SQLite database driver
- **JUnit 4.13.2**: Testing framework

All dependencies are managed by Maven and will be downloaded automatically.

## Next Steps

Here are some ideas to extend this project:

- Add a web interface using Spring Boot
- Implement multiple search engine providers
- Add email validation and social media checks
- Export data to CSV/Excel
- Add a REST API for external integrations
- Implement company categorization/tagging
- Add automated scheduled checks
- Integrate with CRM systems

## Troubleshooting

### "mvnw.cmd is not recognized"

Make sure you're in the project root directory where mvnw.cmd is located.

### Java version errors

Check your Java version with `java -version`. The project is configured for Java 8.

### Database locked errors

Make sure only one instance of the application is running at a time.

### API rate limits

If using a real search API, be mindful of rate limits. The app includes a 500ms delay between batch checks.

## Learning Resources

- [Java Documentation](https://docs.oracle.com/javase/8/docs/)
- [Maven Getting Started](https://maven.apache.org/guides/getting-started/)
- [OkHttp Documentation](https://square.github.io/okhttp/)
- [Gson User Guide](https://github.com/google/gson/blob/master/UserGuide.md)
- [SQLite Tutorial](https://www.sqlitetutorial.net/)

## License

This project is for educational purposes.
