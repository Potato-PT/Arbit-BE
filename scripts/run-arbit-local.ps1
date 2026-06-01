$ErrorActionPreference = "Stop"

function Import-DotEnv {
    param([string]$Path)

    if (-not (Test-Path -LiteralPath $Path)) {
        return
    }

    Get-Content -LiteralPath $Path | ForEach-Object {
        $line = $_.Trim()
        if ($line.Length -eq 0 -or $line.StartsWith("#")) {
            return
        }

        $index = $line.IndexOf("=")
        if ($index -lt 1) {
            return
        }

        $name = $line.Substring(0, $index).Trim()
        $value = $line.Substring($index + 1).Trim()
        [Environment]::SetEnvironmentVariable($name, $value, "Process")
    }
}

$projectDir = "D:\Project\Arbit"
Import-DotEnv -Path (Join-Path $projectDir ".env")

$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:SERVER_PORT = "8080"
$env:SPRING_PROFILES_ACTIVE = "local"
$env:APP_DB_URL = $env:LOCAL_DB_URL
$env:APP_DB_USERNAME = $env:LOCAL_DB_USERNAME
$env:APP_DB_PASSWORD = $env:LOCAL_DB_PASSWORD
$env:ARBIT_AI_BASE_URL = "http://127.0.0.1:8081"

Set-Location $projectDir
.\gradlew.bat bootRun --no-daemon
