<#
Verificacion post-despliegue del backend en Render para el rol PARTICIPANT.

Uso:
  .\verify-participant.ps1 -BaseUrl "https://academic-events-api-h1kf.onrender.com/api"
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

Write-Host "Verificando pruebas de PARTICIPANT en: $BaseUrl"
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

# 2. Login de Participante
Write-Host ""
Write-Host "Paso 2: POST /auth/login (credenciales student)"
$loginBody = @{ username = 'student@ups.edu.ec'; password = 'password123' }
$login = Invoke-Api -Method POST -Path '/auth/login' -Body $loginBody -TimeoutSec 30
$token = $null
if ($login.StatusCode -eq 200) {
    try { $token = ($login.Content | ConvertFrom-Json).token } catch {}
}
Add-Result -Step '2. Login Participante' -Passed ($login.StatusCode -eq 200 -and $token) `
    -Detail "HTTP $($login.StatusCode), token obtenido: $([bool]$token)"

$authHeaders = @{ Authorization = "Bearer $token" }

# 3. GET /events (Buscar eventos - Público/Autenticado)
Write-Host ""
Write-Host "Paso 3: GET /events"
$events = Invoke-Api -Method GET -Path '/events' -TimeoutSec 30
Add-Result -Step '3. Buscar eventos' -Passed ($events.StatusCode -eq 200) `
    -Detail "HTTP $($events.StatusCode)"

# 4. GET /registrations/1/certificate.pdf (Su certificado propio - OK)
Write-Host ""
Write-Host "Paso 4: GET /registrations/1/certificate.pdf"
if ($token) {
    $cert = Invoke-Api -Method GET -Path '/registrations/1/certificate.pdf' -Headers $authHeaders -TimeoutSec 60
    $contentType = $cert.Headers['Content-Type']
    $contentDisposition = $cert.Headers['Content-Disposition']
    $headersOk = ($contentType -like '*pdf*') -and $contentDisposition
    Add-Result -Step '4. Descargar certificado propio' -Passed ($cert.StatusCode -eq 200 -and $headersOk) `
        -Detail "HTTP $($cert.StatusCode), Content-Type: $contentType"
} else {
    Add-Result -Step '4. Descargar certificado propio' -Passed $false -Detail 'Omitido: sin token'
}

# 5. GET /reports/events/1/registrations.pdf (Denegado para participante - 403)
Write-Host ""
Write-Host "Paso 5: GET /reports/events/1/registrations.pdf (Rol Admin/Organizer Requerido)"
if ($token) {
    $report = Invoke-Api -Method GET -Path '/reports/events/1/registrations.pdf' -Headers $authHeaders -TimeoutSec 60
    Add-Result -Step '5. Reporte PDF de inscritos (Denegado)' -Passed ($report.StatusCode -eq 403) `
        -Detail "HTTP $($report.StatusCode) (se esperaba 403)"
} else {
    Add-Result -Step '5. Reporte PDF de inscritos (Denegado)' -Passed $false -Detail 'Omitido: sin token'
}

# 6. POST /registrations/events/1 (Inscripción repetida - 400 Bad Request esperado)
Write-Host ""
Write-Host "Paso 6: POST /registrations/events/1 (Registro duplicado)"
if ($token) {
    $regDup = Invoke-Api -Method POST -Path '/registrations/events/1' -Headers $authHeaders -TimeoutSec 30
    # Esperamos 400 o 409 ya que 'student@ups.edu.ec' ya está inscrito al evento 1.
    Add-Result -Step '6. Inscripción duplicada (Rechazada)' -Passed ($regDup.StatusCode -eq 400 -or $regDup.StatusCode -eq 409) `
        -Detail "HTTP $($regDup.StatusCode) (se esperaba 400 o 409)"
} else {
    Add-Result -Step '6. Inscripción duplicada (Rechazada)' -Passed $false -Detail 'Omitido: sin token'
}

# Resumen
Write-Host ""
Write-Host "===================== RESUMEN PARTICIPANT ====================="
foreach ($r in $script:results) {
    $mark = if ($r.Passed) { "OK  " } else { "FAIL" }
    Write-Host "[$mark] $($r.Step) - $($r.Detail)"
}
$failed = @($script:results | Where-Object { -not $_.Passed })
Write-Host "==============================================================="
if ($failed.Count -eq 0) {
    Write-Host "Todas las pruebas de PARTICIPANT pasaron ($($script:results.Count)/$($script:results.Count))." -ForegroundColor Green
} else {
    Write-Host "$($failed.Count) de $($script:results.Count) pruebas fallaron." -ForegroundColor Red
}
