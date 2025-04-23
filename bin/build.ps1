# Script to build the Todo List Application for Windows environments
# Stop on any error
$ErrorActionPreference = "Stop"

# Directory setup
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$ROOT_DIR = (Get-Item $scriptPath).Parent.FullName
Set-Location "$ROOT_DIR\MtdrSpring"

$FRONTEND_DIR = "front"
$BACKEND_DIR = "backend"
$FRONTEND_DIST_DIR = "$BACKEND_DIR\target\frontend"

Write-Host "Building Todo List Application..."

# Build frontend
Write-Host "Building frontend..."
Set-Location $FRONTEND_DIR
npm ci
npm run build

# Create frontend distribution directory
Set-Location ..
if (-not (Test-Path $FRONTEND_DIST_DIR)) {
    New-Item -ItemType Directory -Path $FRONTEND_DIST_DIR -Force | Out-Null
}

# Copy build files to backend
Write-Host "Copying frontend build to backend..."
Copy-Item "$FRONTEND_DIR\dist\*" $FRONTEND_DIST_DIR -Recurse -Force

# Load environment variables
Write-Host "Loading environment variables..."
$envFile = "$ROOT_DIR\.env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        if (-not $_.StartsWith("#") -and $_.Length -gt 0) {
            $key, $value = $_ -split '=', 2
            Set-Item -Path "Env:$key" -Value $value
        }
    }
}

# Build and run backend
Write-Host "Building and running backend..."
Set-Location $BACKEND_DIR

# Pass environment variables to Maven
$jvmArgs = @(
    "-Dlogging.level.root=debug",
    "-Dtelegram.bot.token=$env:telegram_token",
    "-Dtelegram.bot.name=$env:telegram_name",
    "-Dspring.datasource.url=jdbc:oracle:thin:@$($env:db_tns_name)?TNS_ADMIN=.\wallet",
    "-Dspring.datasource.username=$env:db_user",
    "-Dspring.datasource.password=$env:dbpassword",
        "-Dspring.datasource.driver-class-name=$env:driver_class_name"
) -join " "

Write-Host $jvmArgs

& .\mvnw spring-boot:run "-Dspring-boot.run.jvmArguments=$jvmArgs"

Set-Location ..\..

Write-Host "Done!"
