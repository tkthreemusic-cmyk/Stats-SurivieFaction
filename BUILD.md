# Build Instructions

## Prerequisites
- Java 21+
- Maven 3.6+

## Build Commands

```bash
# Navigate to project directory
cd PlayerStats

# Clean and package
mvn clean package

# Or with verbose output
mvn clean package -X
```

## Output
The compiled JAR will be located at:
```
target/PlayerStats-1.0.0.jar
```

## Installing Build Tools

### Ubuntu/Debian
```bash
sudo apt update
sudo apt install openjdk-21-jdk maven
```

### Windows (using Chocolatey)
```powershell
choco install openjdk21 maven
```

### macOS
```bash
brew install openjdk@21 maven
```
