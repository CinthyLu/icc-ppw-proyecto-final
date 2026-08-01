<#
Verificacion post-despliegue del backend en Render para el rol ADMIN.

Uso:
  .\verify-admin.ps1 -BaseUrl "https://academic-events-api-h1kf.onrender.com/api"
#>

param(
    [string]$BaseUrl = "https://academic-events-api-h1kf.onrender.com/api"
)

$ErrorActionPreference = 'Stop'
$BaseUrl = $BaseUrl.TrimEnd('/')

$script:results = New-Object System.Collections.Generic.List[Object]

function Add-Result {
    param([string]$Step, [bool]$Passed, [string]$Detail)
    $script:results.Add([PSCustomObject]@{ Step = $Step; Passed = $Passed; Detail = $Detail })
    $mark = if ($Passed) { "OK  " } else { "FAIL" }
    Write-Host "[$mark] $Step - $Detail"
}

function Invoke-Api {
    param(
        [string]$Method = 'GET',
        [string]$Path,
        [hashtable]$Headers,
        [object]$Body,
        [int]$TimeoutSec = 30
    )

    $uri = "$BaseUrl$Path"
    $params = @{
        Method          = $Method
        Uri             = $uri
        TimeoutSec      = $TimeoutSec
        UseBasicParsing = $true
    }
    if ($Headers) { $params.Headers = $Headers }
    if ($null -ne $Body) {
        $params.Body = ($Body | ConvertTo-Json)
        $params.ContentType = 'application/json'
    }

    try {
        $resp = Invoke-WebRequest @params
        $rawContent = $resp.Content
        if ($rawContent -is [System.Array]) {
            $rawContent = [System.Text.Encoding]::UTF8.GetString($rawContent)
        }
        return [PSCustomObject]@{
            StatusCode = [int]$resp.StatusCode
            Headers    = $resp.Headers
            Content    = $rawContent
        }
    } catch {
        $ex = $_.Exception
        if ($ex.Response) {
            $statusCode = [int]$ex.Response.StatusCode
            $respHeaders = @{}
            foreach ($key in $ex.Response.Headers.AllKeys) {
                $respHeaders[$key] = $ex.Response.Headers[$key]
            }
            $content = ''
            try {
                $stream = $ex.Response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($stream)
                $content = $reader.ReadToEnd()
            } catch {}
            return [PSCustomObject]@{
                StatusCode = $statusCode
                Headers    = $respHeaders
                Content    = $content
            }
        }
        return [PSCustomObject]@{
            StatusCode = -1
            Headers    = @{}
            Content    = $ex.Message
        }
    }
}

Write-Host "Verificando pruebas de ADMIN en: $BaseUrl"
Write-Host ""

# 1. Health check
Write-Host "Paso 1: GET /actuator/health"
$health = Invoke-Api -Method GET -Path '/actuator/health' -TimeoutSec 100
$healthUp = $false
if ($health.StatusCode -eq 200) {
    try {
        $healthUp = ($health.Content | ConvertFrom-Json).status -eq 'UP'
    } catch {}
}
Add-Result -Step '1. Health check' -Passed ($health.StatusCode -eq 200 -and $healthUp) `
    -Detail "HTTP $($health.StatusCode), status UP: $healthUp"

# 2. Login de Admin
Write-Host ""
Write-Host "Paso 2: POST /auth/login (credenciales admin)"
$loginBody = @{ username = 'admin@ups.edu.ec'; password = 'password123' }
$login = Invoke-Api -Method POST -Path '/auth/login' -Body $loginBody -TimeoutSec 30
$token = $null
if ($login.StatusCode -eq 200) {
    try { $token = ($login.Content | ConvertFrom-Json).token } catch {}
}
Add-Result -Step '2. Login Admin' -Passed ($login.StatusCode -eq 200 -and $token) `
    -Detail "HTTP $($login.StatusCode), token obtenido: $([bool]$token)"

$authHeaders = @{ Authorization = "Bearer $token" }

# 3. GET /registrations (Solo ADMIN)
Write-Host ""
Write-Host "Paso 3: GET /registrations (Acceso ADMIN)"
if ($token) {
    $regs = Invoke-Api -Method GET -Path '/registrations' -Headers $authHeaders -TimeoutSec 30
    Add-Result -Step '3. Listar todas las inscripciones' -Passed ($regs.StatusCode -eq 200) `
        -Detail "HTTP $($regs.StatusCode)"
} else {
    Add-Result -Step '3. Listar todas las inscripciones' -Passed $false -Detail 'Omitido: sin token'
}

# 4. POST /categories (ADMIN crea categoría)
Write-Host ""
Write-Host "Paso 4: POST /categories (Crear categoría)"
$newCategoryId = $null
if ($token) {
    $catBody = @{ name = "Test Admin Category $(Get-Date -Format 'yyyyMMddHHmmss')"; description = "Creada por verify-admin.ps1" }
    $catResp = Invoke-Api -Method POST -Path '/categories' -Headers $authHeaders -Body $catBody -TimeoutSec 30
    if ($catResp.StatusCode -eq 201) {
        try { $newCategoryId = ($catResp.Content | ConvertFrom-Json).id } catch {}
    }
    Add-Result -Step '4. Crear categoría' -Passed ($catResp.StatusCode -eq 201 -and $newCategoryId) `
        -Detail "HTTP $($catResp.StatusCode), ID categoría creada: $newCategoryId"
} else {
    Add-Result -Step '4. Crear categoría' -Passed $false -Detail 'Omitido: sin token'
}

# 5. DELETE /categories/{id} (ADMIN elimina categoría)
Write-Host ""
Write-Host "Paso 5: DELETE /categories/{id} (Eliminar categoría creada)"
if ($token -and $newCategoryId) {
    $delResp = Invoke-Api -Method DELETE -Path "/categories/$newCategoryId" -Headers $authHeaders -TimeoutSec 30
    Add-Result -Step '5. Eliminar categoría' -Passed ($delResp.StatusCode -eq 204) `
        -Detail "HTTP $($delResp.StatusCode)"
} else {
    Add-Result -Step '5. Eliminar categoría' -Passed $false -Detail 'Omitido: sin token o categoría no creada'
}

# Resumen
Write-Host ""
Write-Host "===================== RESUMEN ADMIN ====================="
foreach ($r in $script:results) {
    $mark = if ($r.Passed) { "OK  " } else { "FAIL" }
    Write-Host "[$mark] $($r.Step) - $($r.Detail)"
}
$failed = @($script:results | Where-Object { -not $_.Passed })
Write-Host "========================================================="
if ($failed.Count -eq 0) {
    Write-Host "Todas las pruebas de ADMIN pasaron ($($script:results.Count)/$($script:results.Count))." -ForegroundColor Green
} else {
    Write-Host "$($failed.Count) de $($script:results.Count) pruebas fallaron." -ForegroundColor Red
}
