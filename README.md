# Database Metadata Analyzer

A professional Spring Boot console application for extracting comprehensive metadata from Oracle databases and generating various output formats including CSV, JSON, XML, and DDL statements.

## 🚀 Features

- **Comprehensive Metadata Extraction**: Extract table structures, column definitions, data types, constraints, and relationships
- **Multiple Export Formats**: Support for CSV, JSON, XML, and DDL output formats
- **Flexible Configuration**: Command-line arguments and configuration file support
- **Advanced Filtering**: Wildcard pattern matching for table selection with include/exclude patterns
- **DDL Generation**: Generate syntactically correct CREATE TABLE statements for Oracle
- **Professional Error Handling**: Graceful error handling with meaningful messages and proper exit codes
- **Batch Processing**: Automation-friendly with quiet and verbose modes
- **Oracle Optimization**: Optimized for Oracle databases with proper data type handling

## 📋 Requirements

- **Java 21** or higher
- **Oracle Database** (tested with Oracle 11g+)
- **Gradle 8.0+** for building
- **Network access** to Oracle database

## 🛠️ Installation

### Clone the Repository
```bash
git clone <repository-url>
cd database-metadata-analyzer
```

### Build the Application
```bash
./gradlew build
```

### Run Tests
```bash
./gradlew test
```

## 🔧 Configuration

### Database Connection

Configure your Oracle database connection in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:oracle:thin:@//hostname:port/service_name
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
```

### Environment Variables

You can also use environment variables for sensitive information:

```bash
export DB_URL=jdbc:oracle:thin:@//hostname:port/service_name
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
```

## 🚀 Usage

### Basic Usage

Extract metadata from a schema and output to CSV:
```bash
java -jar build/libs/database-metadata-analyzer.jar --schema=REPORTUSER
```

### Command Line Options

| Option | Description | Default |
|--------|-------------|---------|
| `--schema` | Database schema name | Required |
| `--format` | Output format (csv, json, xml, ddl) | csv |
| `--output` | Output file path | stdout |
| `--include` | Include table patterns (comma-separated) | All tables |
| `--exclude` | Exclude table patterns (comma-separated) | None |
| `--verbose` | Enable verbose output | false |
| `--quiet` | Minimize console output | false |
| `--help` | Show help message | - |

### Examples

#### Export to JSON file
```bash
java -jar build/libs/database-metadata-analyzer.jar \
  --schema=REPORTUSER \
  --format=json \
  --output=metadata.json
```

#### Generate DDL for specific tables
```bash
java -jar build/libs/database-metadata-analyzer.jar \
  --schema=REPORTUSER \
  --format=ddl \
  --include="RV_*,CATALOG*" \
  --output=schema.sql
```

#### Export with filtering
```bash
java -jar build/libs/database-metadata-analyzer.jar \
  --schema=REPORTUSER \
  --format=csv \
  --include="RV_ARTICULOS*" \
  --exclude="*_TEMP" \
  --output=articles_metadata.csv
```

#### Verbose mode for debugging
```bash
java -jar build/libs/database-metadata-analyzer.jar \
  --schema=REPORTUSER \
  --verbose \
  --format=xml \
  --output=metadata.xml
```

## 📊 Output Formats

### CSV Format
```csv
table_name,column_name,data_type,data_length,nullable,data_default
CATALOGOS,CADENA,NUMBER,22,Y,1
CATALOGOS,MODULO,VARCHAR2,50,N,
```

### JSON Format
```json
{
  "schema": "REPORTUSER",
  "extractedAt": "2025-01-08T10:30:00Z",
  "tables": [
    {
      "tableName": "CATALOGOS",
      "columns": [
        {
          "columnName": "CADENA",
          "dataType": "NUMBER",
          "dataLength": 22,
          "nullable": true,
          "defaultValue": "1"
        }
      ]
    }
  ]
}
```

### XML Format
```xml
<?xml version="1.0" encoding="UTF-8"?>
<schema name="REPORTUSER" extractedAt="2025-01-08T10:30:00Z">
  <table name="CATALOGOS">
    <column name="CADENA" dataType="NUMBER" length="22" nullable="true" defaultValue="1"/>
  </table>
</schema>
```

### DDL Format
```sql
CREATE TABLE REPORTUSER.CATALOGOS (
  CADENA NUMBER(22) DEFAULT 1,
  MODULO VARCHAR2(50) NOT NULL,
  CAMPO VARCHAR2(20) NOT NULL,
  CONSTRAINT pk_catalogos PRIMARY KEY (CADENA, MODULO, CAMPO)
);
```

## 🏗️ Architecture

The application follows clean architecture principles:

- **Presentation Layer**: Command-line interface and argument parsing
- **Application Layer**: Main application logic and orchestration  
- **Service Layer**: Business logic for metadata extraction, export, and DDL generation
- **Data Access Layer**: Database connectivity and query execution
- **Infrastructure Layer**: File I/O, configuration management, and external dependencies

### Key Components

- `ConfigurationService`: Manages application configuration
- `MetadataExtractionService`: Extracts database metadata
- `ExportService`: Handles multiple export formats
- `DDLGeneratorService`: Generates DDL statements

## 🧪 Testing

### Run All Tests
```bash
./gradlew test
```

### Run Integration Tests
```bash
./gradlew integrationTest
```

### Test Coverage
```bash
./gradlew jacocoTestReport
```

## 🔍 Troubleshooting

### Common Issues

#### Database Connection Failed
```
Error: Unable to connect to database
Solution: Verify connection parameters, network connectivity, and credentials
```

#### Schema Not Found
```
Error: Schema 'INVALID_SCHEMA' does not exist
Solution: Check schema name spelling and user permissions
```

#### Permission Denied
```
Error: Insufficient privileges to access system views
Solution: Grant SELECT privileges on ALL_TABLES and ALL_TAB_COLUMNS views
```

### Required Oracle Privileges

The database user needs the following privileges:
```sql
GRANT SELECT ON ALL_TABLES TO your_username;
GRANT SELECT ON ALL_TAB_COLUMNS TO your_username;
GRANT SELECT ON ALL_CONSTRAINTS TO your_username;
GRANT SELECT ON ALL_CONS_COLUMNS TO your_username;
```

## 📈 Performance

### Optimization Tips

- Use table filtering to reduce processing time for large schemas
- Enable connection pooling for multiple operations
- Use batch processing mode for automated workflows
- Monitor memory usage with large datasets

### Benchmarks

| Schema Size | Tables | Processing Time | Memory Usage |
|-------------|--------|----------------|--------------|
| Small | <50 | <5 seconds | <100MB |
| Medium | 50-200 | 5-20 seconds | 100-300MB |
| Large | 200+ | 20+ seconds | 300MB+ |

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Setup

```bash
# Clone the repository
git clone <repository-url>
cd database-metadata-analyzer

# Build and test
./gradlew build test

# Run the application in development
./gradlew bootRun --args="--schema=REPORTUSER --verbose"
```

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Documentation**: Check the [Wiki](wiki-url) for detailed documentation
- **Issues**: Report bugs and request features in [Issues](issues-url)
- **Discussions**: Join the community in [Discussions](discussions-url)

## 🗺️ Roadmap

- [ ] Support for additional database types (PostgreSQL, MySQL, SQL Server)
- [ ] Web interface for interactive metadata exploration
- [ ] Data lineage and relationship visualization
- [ ] Integration with popular data catalog tools
- [ ] REST API for programmatic access
- [ ] Docker containerization
- [ ] Kubernetes deployment support

## 📊 Project Status

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Coverage](https://img.shields.io/badge/coverage-85%25-green)
![Version](https://img.shields.io/badge/version-1.0.0-blue)
![License](https://img.shields.io/badge/license-MIT-blue)

---

**Made with ❤️ by Alex Kreaker**
