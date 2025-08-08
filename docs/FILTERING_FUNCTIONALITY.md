# Table Filtering and Pattern Matching Functionality

## Overview

The Database Metadata Analyzer includes comprehensive table filtering and pattern matching functionality that allows users to selectively extract metadata from specific tables based on configurable patterns.

## Features Implemented

### 1. Wildcard Pattern Matching
- **Asterisk (`*`)**: Matches zero or more characters
- **Question Mark (`?`)**: Matches exactly one character
- Examples:
  - `USER_*` matches `USER_ACCOUNTS`, `USER_PROFILES`, `USER_DATA`
  - `USER?_ACCOUNTS` matches `USER1_ACCOUNTS`, `USERA_ACCOUNTS` but not `USER12_ACCOUNTS`

### 2. Include/Exclude Pattern Logic
- **Include Patterns**: Only tables matching at least one include pattern are processed
- **Exclude Patterns**: Tables matching any exclude pattern are skipped
- **Precedence**: Exclude patterns take precedence over include patterns
- **Default Behavior**: If no patterns are specified, all tables are included

### 3. Case Sensitivity Options
- **Case-Insensitive (Default)**: `USER_*` matches both `USER_ACCOUNTS` and `user_accounts`
- **Case-Sensitive**: `USER_*` matches only `USER_ACCOUNTS`, not `user_accounts`

### 4. Regular Expression Support
- **Wildcard Mode (Default)**: Uses simple wildcard patterns (`*`, `?`)
- **Regex Mode**: Supports full regular expression syntax
- Examples:
  - `USER\\d+_ACCOUNTS` matches `USER123_ACCOUNTS` but not `USER_ACCOUNTS`
  - `.*_TEMP$` matches any table ending with `_TEMP`

### 5. Predefined System Table Exclusion
- Built-in filter to exclude common Oracle system tables
- Excludes patterns like `SYS_*`, `SYSTEM_*`, `APEX_*`, `FLOWS_*`, etc.

## Command-Line Usage

### Basic Include/Exclude Patterns
```bash
# Include only USER and CUSTOMER tables
java -jar vtda.jar --include "USER_*" --include "CUSTOMER_*"

# Exclude temporary and backup tables
java -jar vtda.jar --exclude "*_TEMP" --exclude "*_BAK"

# Combine include and exclude
java -jar vtda.jar --include "USER_*" --exclude "*_TEMP"
```

### Case Sensitivity
```bash
# Case-sensitive matching
java -jar vtda.jar --include "USER_*" --case-sensitive true

# Case-insensitive matching (default)
java -jar vtda.jar --include "USER_*" --case-sensitive false
```

### Regular Expression Mode
```bash
# Use regex patterns
java -jar vtda.jar --include "USER\\d+_.*" --regex true

# Complex regex with case-insensitive matching
java -jar vtda.jar --include "^(USER|CUSTOMER)_.*(?<!_TEMP)$" --regex true --case-sensitive false
```

## Configuration File Usage

### YAML Configuration
```yaml
filter:
  include:
    - "USER_*"
    - "CUSTOMER_*"
  exclude:
    - "*_TEMP"
    - "*_BAK"
  case-sensitive: false
  regex: false
```

### Properties Configuration
```properties
filter.include=USER_*,CUSTOMER_*
filter.exclude=*_TEMP,*_BAK
filter.case-sensitive=false
filter.regex=false
```

## Implementation Details

### FilterConfig Class
The `FilterConfig` class provides the core filtering functionality:

```java
FilterConfig filter = FilterConfig.builder()
    .includePattern("USER_*")
    .includePattern("CUSTOMER_*")
    .excludePattern("*_TEMP")
    .caseSensitive(false)
    .useRegex(false)
    .build();

// Test if a table matches the filter
boolean matches = filter.matches("USER_ACCOUNTS"); // true
boolean matches = filter.matches("USER_TEMP");     // false (excluded)
boolean matches = filter.matches("PRODUCT_DATA");  // false (not included)
```

### Integration with MetadataExtractionService
The filtering is automatically applied during metadata extraction:

```java
List<TableMetadata> tables = metadataService.extractTableMetadata("SCHEMA_NAME", filterConfig);
```

## Pattern Matching Examples

### Wildcard Patterns
| Pattern | Matches | Doesn't Match |
|---------|---------|---------------|
| `USER_*` | `USER_ACCOUNTS`, `USER_PROFILES`, `USER_` | `CUSTOMER_ACCOUNTS`, `USER` |
| `*_TEMP` | `USER_TEMP`, `DATA_TEMP`, `_TEMP` | `TEMP_USER`, `TEMPORARY` |
| `USER?_DATA` | `USER1_DATA`, `USERA_DATA` | `USER12_DATA`, `USER_DATA` |
| `*USER*` | `MY_USER_DATA`, `USER_ACCOUNTS`, `CUSTOMER_USER` | `ACCOUNT_DATA` |

### Regular Expression Patterns
| Pattern | Matches | Doesn't Match |
|---------|---------|---------------|
| `USER\\d+` | `USER123`, `USER1` | `USER`, `USERA` |
| `^USER_.*(?<!_TEMP)$` | `USER_ACCOUNTS`, `USER_DATA` | `USER_ACCOUNTS_TEMP` |
| `(USER\|CUSTOMER)_.*` | `USER_ACCOUNTS`, `CUSTOMER_ORDERS` | `PRODUCT_DATA` |

### System Table Exclusion
The predefined system table filter excludes:
- `SYS_*` - Oracle system tables
- `SYSTEM_*` - System schema tables
- `APEX_*` - Oracle APEX tables
- `FLOWS_*` - Oracle APEX flows
- `MDSYS_*` - Spatial data system tables
- `CTXSYS_*` - Context system tables
- `XDB_*` - XML DB tables
- `WMSYS_*` - Workspace Manager tables

## Error Handling

### Pattern Validation
- Empty or null patterns are rejected
- Invalid regex patterns throw `IllegalStateException`
- Pattern validation occurs during configuration building

### Graceful Degradation
- If no tables match the filter criteria, an empty list is returned
- Invalid patterns in configuration files are reported with clear error messages
- The application continues processing even if some patterns are invalid

## Performance Considerations

### Efficient Filtering
- Filtering is applied at the database query level when possible
- Pattern matching uses optimized Java regex engine
- Case-insensitive matching is handled efficiently

### Memory Usage
- Patterns are compiled once and reused
- Large result sets are processed incrementally
- No unnecessary table metadata is loaded for filtered-out tables

## Testing

The filtering functionality is thoroughly tested with:
- Unit tests for `FilterConfig` class covering all pattern types
- Integration tests for `MetadataExtractionService` filtering
- Edge case testing for invalid patterns and empty results
- Performance testing with large schemas

## Future Enhancements

Potential future improvements:
- Schema-level filtering patterns
- Column-level filtering within tables
- Date-based filtering (created/modified dates)
- Size-based filtering (table row counts, storage size)
- Custom filter plugins