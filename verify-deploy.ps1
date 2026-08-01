<#
Verificacion post-despliegue del backend en Render.

Uso:
  .\scripts\verify-deploy.ps1 -BaseUrl "https://mi-app.onrender.com/api"
#>

param(
    # [string]$BaseUrl = "https://proyecto-api-2jua.onrender.com/api"
    [string]$BaseUrl = "http://localhost:8080/api"
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

# Envuelve Invoke-WebRequest para no perder ni el status code ni los
# headers cuando la API responde 4xx/5xx (WebCmdlets los trata como
# excepcion terminante).
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
        # Sin respuesta del servidor: timeout, DNS, conexion rechazada, etc.
        return [PSCustomObject]@{
            StatusCode = -1
            Headers    = @{}
            Content    = $ex.Message
        }
    }
}

Write-Host "Verificando despliegue en: $BaseUrl"
Write-Host ""

# 1. Health check, tolerando arranque en frio del plan free de Render.
Write-Host "Paso 1: GET /actuator/health"
Write-Host "  El plan free de Render duerme el servicio tras inactividad;" `
    "el primer request puede tardar hasta 90 segundos. Esperando..."
$health = Invoke-Api -Method GET -Path '/actuator/health' -TimeoutSec 100
$healthUp = $false
if ($health.StatusCode -eq 200) {
    Write-Host "  Contenido del Health Check: $($health.Content)"
    try {
        $healthUp = ($health.Content | ConvertFrom-Json).status -eq 'UP'
    } catch {
        Write-Host "  Error al parsear el JSON de salud: $_"
    }
}
Add-Result -Step '1. Health check' -Passed ($health.StatusCode -eq 200 -and $healthUp) `
    -Detail "HTTP $($health.StatusCode), status UP: $healthUp"

# 2. Login valido, guarda el access token.
Write-Host ""
Write-Host "Paso 2: POST /auth/login (credenciales validas)"
$loginBody = @{ username = 'organizer@ups.edu.ec'; password = 'password123' }
$login = Invoke-Api -Method POST -Path '/auth/login' -Body $loginBody -TimeoutSec 30
$token = $null
if ($login.StatusCode -eq 200) {
    try { $token = ($login.Content | ConvertFrom-Json).token } catch {}
}
Add-Result -Step '2. Login valido' -Passed ($login.StatusCode -eq 200 -and $token) `
    -Detail "HTTP $($login.StatusCode), token obtenido: $([bool]$token)"

if (-not $token) {
    Write-Host ""
    Write-Host "No se obtuvo token; los pasos que lo requieren se omiten." -ForegroundColor Yellow
}
$authHeaders = @{ Authorization = "Bearer $token" }

# 3. Evento existente.
Write-Host ""
Write-Host "Paso 3: GET /events/1"
$event = Invoke-Api -Method GET -Path '/events/1' -TimeoutSec 30
Add-Result -Step '3. GET /events/1' -Passed ($event.StatusCode -eq 200) `
    -Detail "HTTP $($event.StatusCode)"

# 4. Reporte PDF del propio evento del organizador.
Write-Host ""
Write-Host "Paso 4: GET /reports/events/1/registrations.pdf (con token)"
if ($token) {
    $report1 = Invoke-Api -Method GET -Path '/reports/events/1/registrations.pdf' -Headers $authHeaders -TimeoutSec 60
    $contentType = $report1.Headers['Content-Type']
    $contentDisposition = $report1.Headers['Content-Disposition']
    $headersOk = ($contentType -like '*pdf*') -and $contentDisposition
    Add-Result -Step '4. Reporte PDF (propio)' -Passed ($report1.StatusCode -eq 200 -and $headersOk) `
        -Detail "HTTP $($report1.StatusCode), Content-Type: $contentType, Content-Disposition: $contentDisposition"
} else {
    Add-Result -Step '4. Reporte PDF (propio)' -Passed $false -Detail 'Omitido: sin token'
}

# 5. Reporte PDF de un evento de otro organizador: debe rechazarse.
Write-Host ""
Write-Host "Paso 5: GET /reports/events/2/registrations.pdf (evento ajeno, con token)"
if ($token) {
    $report2 = Invoke-Api -Method GET -Path '/reports/events/2/registrations.pdf' -Headers $authHeaders -TimeoutSec 60
    Add-Result -Step '5. Reporte PDF (ajeno)' -Passed ($report2.StatusCode -eq 403) `
        -Detail "HTTP $($report2.StatusCode) (se esperaba 403)"
} else {
    Add-Result -Step '5. Reporte PDF (ajeno)' -Passed $false -Detail 'Omitido: sin token'
}

# 6. Bloqueo por intentos fallidos: 6 logins invalidos, los ultimos deben dar 429.
Write-Host ""
Write-Host "Paso 6: 6x POST /auth/login (credenciales invalidas)"
$badBody = @{ username = 'organizer@ups.edu.ec'; password = 'credencial-invalida' }
$got429 = $false
$retryAfter = $null
for ($i = 1; $i -le 6; $i++) {
    $attempt = Invoke-Api -Method POST -Path '/auth/login' -Body $badBody -TimeoutSec 30
    Write-Host "  Intento $i -> HTTP $($attempt.StatusCode)"
    if ($attempt.StatusCode -eq 429) {
        $got429 = $true
        $retryAfter = $attempt.Headers['Retry-After']
    }
}
Add-Result -Step '6. Bloqueo por intentos fallidos (429)' -Passed $got429 `
    -Detail "429 recibido: $got429, Retry-After: $retryAfter"

# 7. CORS: origen no autorizado no debe recibir Access-Control-Allow-Origin.
Write-Host ""
Write-Host "Paso 7: OPTIONS /auth/login con Origin malicioso"
$corsHeaders = @{
    'Origin'                         = 'http://malicioso.test'
    'Access-Control-Request-Method'  = 'POST'
}
$cors = Invoke-Api -Method OPTIONS -Path '/auth/login' -Headers $corsHeaders -TimeoutSec 30
$allowOrigin = $cors.Headers['Access-Control-Allow-Origin']
Add-Result -Step '7. CORS bloquea origen no autorizado' -Passed (-not $allowOrigin) `
    -Detail "HTTP $($cors.StatusCode), Access-Control-Allow-Origin: $(if ($allowOrigin) { $allowOrigin } else { '(ausente, correcto)' })"

# Resumen
Write-Host ""
Write-Host "===================== RESUMEN ====================="
foreach ($r in $script:results) {
    $mark = if ($r.Passed) { "OK  " } else { "FAIL" }
    Write-Host "[$mark] $($r.Step)"
}
$failed = @($script:results | Where-Object { -not $_.Passed })
Write-Host "====================================================="
if ($failed.Count -eq 0) {
    Write-Host "Todos los pasos pasaron ($($script:results.Count)/$($script:results.Count))." -ForegroundColor Green
} else {
    Write-Host "$($failed.Count) de $($script:results.Count) pasos fallaron." -ForegroundColor Red
}