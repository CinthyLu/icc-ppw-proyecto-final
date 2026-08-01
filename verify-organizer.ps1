<#
Verificacion post-despliegue del backend en Render para el rol ORGANIZER.

Uso:
  .\verify-organizer.ps1 -BaseUrl "https://academic-events-api-h1kf.onrender.com/api"
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

Write-Host "Verificando pruebas de ORGANIZER en: $BaseUrl"
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

# 2. Login de Organizador
Write-Host ""
Write-Host "Paso 2: POST /auth/login (credenciales organizer)"
$loginBody = @{ username = 'organizer@ups.edu.ec'; password = 'password123' }
$login = Invoke-Api -Method POST -Path '/auth/login' -Body $loginBody -TimeoutSec 30
$token = $null
if ($login.StatusCode -eq 200) {
    try { $token = ($login.Content | ConvertFrom-Json).token } catch {}
}
Add-Result -Step '2. Login Organizer' -Passed ($login.StatusCode -eq 200 -and $token) `
    -Detail "HTTP $($login.StatusCode), token obtenido: $([bool]$token)"

$authHeaders = @{ Authorization = "Bearer $token" }

# 3. GET /reports/events/1/registrations.pdf (Reporte de su propio evento - OK)
Write-Host ""
Write-Host "Paso 3: GET /reports/events/1/registrations.pdf (Evento propio)"
if ($token) {
    $report1 = Invoke-Api -Method GET -Path '/reports/events/1/registrations.pdf' -Headers $authHeaders -TimeoutSec 60
    $contentType = $report1.Headers['Content-Type']
    $contentDisposition = $report1.Headers['Content-Disposition']
    $headersOk = ($contentType -like '*pdf*') -and $contentDisposition
    Add-Result -Step '3. Reporte PDF propio' -Passed ($report1.StatusCode -eq 200 -and $headersOk) `
        -Detail "HTTP $($report1.StatusCode), Content-Type: $contentType"
} else {
    Add-Result -Step '3. Reporte PDF propio' -Passed $false -Detail 'Omitido: sin token'
}

# 4. GET /reports/events/2/registrations.pdf (Reporte de evento ajeno - 403 Forbidden)
# Nota: Si el evento 2 no existe, puede dar 404, pero el control de propiedad debe evaluar 403 antes si es ajeno.
# Si en tus datos iniciales el evento 2 no existe, verificamos que no dé 200.
Write-Host ""
Write-Host "Paso 4: GET /reports/events/2/registrations.pdf (Evento ajeno)"
if ($token) {
    $report2 = Invoke-Api -Method GET -Path '/reports/events/2/registrations.pdf' -Headers $authHeaders -TimeoutSec 60
    Add-Result -Step '4. Reporte PDF ajeno (Rechazado)' -Passed ($report2.StatusCode -eq 403 -or $report2.StatusCode -eq 404) `
        -Detail "HTTP $($report2.StatusCode) (se esperaba 403 Forbidden)"
} else {
    Add-Result -Step '4. Reporte PDF ajeno (Rechazado)' -Passed $false -Detail 'Omitido: sin token'
}

# 5. GET /registrations (Acceso prohibido para Organizador - 403 Forbidden)
Write-Host ""
Write-Host "Paso 5: GET /registrations (Rol Admin Requerido)"
if ($token) {
    $regs = Invoke-Api -Method GET -Path '/registrations' -Headers $authHeaders -TimeoutSec 30
    Add-Result -Step '5. Listado de todas las inscripciones (Denegado)' -Passed ($regs.StatusCode -eq 403) `
        -Detail "HTTP $($regs.StatusCode) (se esperaba 403)"
} else {
    Add-Result -Step '5. Listado de todas las inscripciones (Denegado)' -Passed $false -Detail 'Omitido: sin token'
}

# Resumen
Write-Host ""
Write-Host "===================== RESUMEN ORGANIZER ====================="
foreach ($r in $script:results) {
    $mark = if ($r.Passed) { "OK  " } else { "FAIL" }
    Write-Host "[$mark] $($r.Step) - $($r.Detail)"
}
$failed = @($script:results | Where-Object { -not $_.Passed })
Write-Host "============================================================="
if ($failed.Count -eq 0) {
    Write-Host "Todas las pruebas de ORGANIZER pasaron ($($script:results.Count)/$($script:results.Count))." -ForegroundColor Green
} else {
    Write-Host "$($failed.Count) de $($script:results.Count) pruebas fallaron." -ForegroundColor Red
}
