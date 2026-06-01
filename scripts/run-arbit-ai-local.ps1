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

$projectDir = "D:\Project\Arbit-AI"
Import-DotEnv -Path (Join-Path $projectDir ".env")

$env:ARBIT_DB_PROFILE = "local"

Set-Location $projectDir
python -m uvicorn app:app --host 127.0.0.1 --port 8081
