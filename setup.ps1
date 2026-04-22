# ======================================================================
# Complete Backend & Database Docker Setup
# ======================================================================

Write-Host "=== Backend & PostgreSQL Docker Setup ===" -ForegroundColor Green

# Configuration
$containerName = "postgres"
$user = "user"
$password = "password"
$db = "test"
$port = "5432"
$backendImage = "hexfields-dominion-api"
$backendPort = "8080"

# Function to check Docker
function Test-Docker {
    try {
        docker info | Out-Null
        return $true
    } catch {
        return $false
    }
}

# Function to create .env file
function Create-EnvFile {
    $envContent = @"
spring.datasource.url=jdbc:postgresql://host.docker.internal:5432/test
spring.datasource.username=user
spring.datasource.password=password
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=create-drop

app.jwt.secretKey=b289de9559b9fe14ef8a5d7dcd91dc94343fce127c15945d21a388f800c28099

app.frontend.host=http://localhost:5173
"@
    
    $envContent | Out-File -FilePath ".env" -Encoding UTF8
    Write-Host "Created .env file" -ForegroundColor Green
}

# Function to build backend image
function Build-Backend {
    Write-Host "Building backend Docker image..." -ForegroundColor Green
    docker build -t $backendImage .
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Failed to build backend image" -ForegroundColor Red
        exit 1
    }
}

# Function to setup PostgreSQL
function Setup-PostgreSQL {
    Write-Host "=== PostgreSQL Setup ===" -ForegroundColor Cyan
    
    # Check existing container
    $existingContainer = docker ps -a --format "{{.Names}}" | Where-Object { $_ -eq $containerName }
    
    if ($existingContainer) {
        Write-Host "Starting existing PostgreSQL container..." -ForegroundColor Yellow
        docker start $containerName 2>$null
    } else {
        Write-Host "Pulling PostgreSQL 17..." -ForegroundColor Green
        docker pull postgres:17 2>$null
        
        Write-Host "Creating PostgreSQL container..." -ForegroundColor Green
        docker run -d `
          --name $containerName `
          -e POSTGRES_PASSWORD=$password `
          -e POSTGRES_USER=$user `
          -e POSTGRES_DB=$db `
          -p "${port}:5432" `
          -v "${containerName}_data:/var/lib/postgresql/data" `
          --restart unless-stopped `
          postgres:17 2>$null
    }
    
    # Wait for PostgreSQL to be ready
    Write-Host "Waiting for PostgreSQL to start..." -ForegroundColor Green
    Start-Sleep -Seconds 5
    
    # Test connection
    $maxRetries = 10
    $retryCount = 0
    $isReady = $false
    
    while ($retryCount -lt $maxRetries -and -not $isReady) {
        try {
            $test = docker exec $containerName pg_isready -U $user
            if ($test -like "*accepting connections*") {
                $isReady = $true
                Write-Host "PostgreSQL is ready!" -ForegroundColor Green
            }
        } catch {
            # Connection failed, will retry
        }
        
        if (-not $isReady) {
            $retryCount++
            Start-Sleep -Seconds 2
        }
    }
    
    if (-not $isReady) {
        Write-Host "WARNING: PostgreSQL may not be fully ready" -ForegroundColor Yellow
    }
}

# Function to start backend
function Start-Backend {
    Write-Host "=== Backend Application Setup ===" -ForegroundColor Cyan
    
    # Check if .env exists, create if not
    if (-not (Test-Path ".env")) {
        Create-EnvFile
    }
    
    # Check if image exists
    $imageExists = docker images --format "{{.Repository}}" | Where-Object { $_ -eq $backendImage }
    
    if (-not $imageExists) {
        Build-Backend
    }
    
    # Stop existing backend container if running
    $existingBackend = docker ps --format "{{.Names}}" | Where-Object { $_ -eq $backendImage }
    if ($existingBackend) {
        Write-Host "Stopping existing backend container..." -ForegroundColor Yellow
        docker stop $backendImage 2>$null
        docker rm $backendImage 2>$null
    }
    
    # Start backend
    Write-Host "Starting backend on port $backendPort..." -ForegroundColor Green
    $containerId = docker run -d `
        --name $backendImage `
        -p "${backendPort}:8080" `
        --env-file ".env" `
        $backendImage 2>&1
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Failed to start backend container" -ForegroundColor Red
        Write-Host "Error details: $containerId" -ForegroundColor Red
        exit 1
    }
    
    # Wait for backend to start
    Write-Host "Waiting for backend to initialize..." -ForegroundColor Green
    Start-Sleep -Seconds 8
    
    # Test backend health
    try {
        $health = Invoke-RestMethod -Uri "http://localhost:${backendPort}/actuator/health" -TimeoutSec 5
        if ($health.status -eq "UP") {
            Write-Host "Backend is healthy and running!" -ForegroundColor Green
        }
    } catch {
        # Try alternative health endpoint
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:${backendPort}/" -Method Head -TimeoutSec 3
            if ($response.StatusCode -eq 200) {
                Write-Host "Backend is responding!" -ForegroundColor Green
            }
        } catch {
            Write-Host "WARNING: Backend may take a moment to fully start" -ForegroundColor Yellow
        }
    }
}

# Function to display status
function Show-Status {
    Write-Host "`n=== SETUP COMPLETE ===" -ForegroundColor Green
    Write-Host "Both services are now running in Docker!" -ForegroundColor Green
    
    Write-Host "`n=== Service Status ===" -ForegroundColor Cyan
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | Out-Host
    
    Write-Host "`n=== Access URLs ===" -ForegroundColor Magenta
    Write-Host "PostgreSQL: localhost:$port"
    Write-Host "Backend API: http://localhost:$backendPort"
    
    Write-Host "`n=== Connection Details ===" -ForegroundColor Yellow
    Write-Host "Database: $db"
    Write-Host "Username: $user"
    Write-Host "Password: $password"
    
    Write-Host "`n=== Docker Commands ===" -ForegroundColor Cyan
    Write-Host "View all containers:    docker ps -a"
    Write-Host "View backend logs:      docker logs $backendImage"
    Write-Host "View PostgreSQL logs:   docker logs $containerName"
    Write-Host "Stop all:               docker stop $backendImage $containerName"
    Write-Host "Stop backend only:      docker stop $backendImage"
    Write-Host "Stop PostgreSQL only:   docker stop $containerName"
    Write-Host "`nTo rebuild backend:     docker build -t $backendImage ."
}

# ======================================================================
# MAIN EXECUTION
# ======================================================================

# Check if Docker is running
if (-not (Test-Docker)) {
    Write-Host "ERROR: Docker is not running. Start Docker Desktop first." -ForegroundColor Red
    Write-Host "Press any key to exit..." -ForegroundColor Yellow
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 1
}

try {
    # Setup PostgreSQL
    Setup-PostgreSQL
    
    # Build and start backend
    Start-Backend
    
    # Show final status
    Show-Status
    
    # Exit immediately (unless there's an error above)
    Write-Host "`nScript completed successfully. Closing in 3 seconds..." -ForegroundColor Green
    Start-Sleep -Seconds 3
    exit 0
    
} catch {
    Write-Host "`nERROR: Script failed with error:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host "`nThe script will remain open for debugging." -ForegroundColor Yellow
    Write-Host "Press any key to exit..." -ForegroundColor Yellow
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 1
}