<#
.SYNOPSIS
    Carga productos de demo usando solicitud en estado ACTIVA.

.DESCRIPTION
    - GET /solicitudes y usa la primera con estado ACTIVA.
    - Si no hay ninguna (param por defecto): crea solicitud, pasa a APROBADA y activa con pago ONLINE (payment-service).
    - POST /productos en product-service.

.PARAMETER NoBootstrap
    Si se indica, no crea ni activa solicitud: falla si no hay ACTIVA.
#>

[CmdletBinding()]
param(
    [switch]$NoBootstrap
)

$ErrorActionPreference = 'Stop'

$SolicitudBaseUrl = 'http://localhost:9003'
$ProductBaseUrl   = 'http://localhost:9007'

$SolicitudesUri = "$SolicitudBaseUrl/solicitudes"
$ProductosUri   = "$ProductBaseUrl/productos"

function Get-RestFailureDetail {
    param([System.Management.Automation.ErrorRecord]$ErrorRecord)

    $status = 'n/a'
    $parts = New-Object System.Collections.Generic.List[string]

    try {
        $resp = $ErrorRecord.Exception.Response
        if ($null -ne $resp) {
            $status = [int]$resp.StatusCode
        }
    } catch {}

    if ($ErrorRecord.ErrorDetails -and $ErrorRecord.ErrorDetails.Message) {
        [void]$parts.Add($ErrorRecord.ErrorDetails.Message.Trim())
    }

    try {
        $resp2 = $ErrorRecord.Exception.Response
        if ($null -ne $resp2) {
            $stream = $resp2.GetResponseStream()
            if ($null -ne $stream) {
                $reader = New-Object System.IO.StreamReader($stream)
                $body = $reader.ReadToEnd()
                $reader.Close()
                if ($body) { [void]$parts.Add($body.Trim()) }
            }
        }
    } catch {}

    if ($parts.Count -eq 0 -and $ErrorRecord.Exception.Message) {
        [void]$parts.Add($ErrorRecord.Exception.Message.Trim())
    }

    return @{ Status = $status; Text = ($parts -join ' | ') }
}

function Invoke-BootstrapVendedorActiva {
    param([string]$BaseUrl)

    $suffix = [guid]::NewGuid().ToString('N').Substring(0, 12)
    $doc = "S$suffix"
    if ($doc.Length -gt 32) { $doc = $doc.Substring(0, 32) }

    Write-Host "No hay solicitud ACTIVA. Creando solicitud de demo (documento=$doc) y activando con pago ONLINE simulado..."
    Write-Host 'Requisitos: solicitud-service (9003) y payment-service accesible desde el contenedor (compose: INTEGRACION_PAYMENT_BASE_URL).'

    $crearBody = @{
        nombreVendedor       = "Seed catalogo $suffix"
        nombres              = 'Seed'
        apellidos            = "Catalogo $suffix"
        documentoIdentidad   = $doc
        correoElectronico    = "seed+$suffix@marketplace.local"
        paisResidencia       = 'Colombia'
        ciudadResidencia     = 'Bogota'
        telefono             = '3001234567'
        tipoPersona          = 'NATURAL'
        adjuntos             = @(
            @{ tipo = 'CEDULA'; nombreArchivo = 'cedula.pdf' },
            @{ tipo = 'ACEPTACION_CENTRALES_RIESGO'; nombreArchivo = 'centrales-riesgo.pdf' },
            @{ tipo = 'ACEPTACION_DATOS_PERSONALES'; nombreArchivo = 'datos-personales.pdf' }
        )
    }
    $crearJson = $crearBody | ConvertTo-Json -Compress -Depth 6

    try {
        $creada = Invoke-RestMethod -Uri "$BaseUrl/solicitudes" -Method Post -Body $crearJson -ContentType 'application/json; charset=utf-8'
    }
    catch {
        $d = Get-RestFailureDetail -ErrorRecord $_
        Write-Host ("Bootstrap fallo al crear solicitud: status={0} {1}" -f $d.Status, $d.Text)
        throw
    }

    $sid = [long]$creada.id
    Write-Host ("Solicitud creada: id={0} estado={1}" -f $sid, $creada.estado)

    $validBody = "{`"documento`":`"$doc`",`"score`":700}"
    try {
        $trasValidar = Invoke-RestMethod -Uri "$BaseUrl/solicitudes/$sid/validacion-automatica" -Method Post -Body $validBody -ContentType 'application/json; charset=utf-8'
    }
    catch {
        $d = Get-RestFailureDetail -ErrorRecord $_
        Write-Host ("Bootstrap fallo en validacion-automatica: status={0} {1}" -f $d.Status, $d.Text)
        throw
    }

    Write-Host ("Validacion automatica: id={0} estado={1}" -f $sid, $trasValidar.estado)
    if ("$($trasValidar.estado)" -ne 'APROBADA') {
        Write-Host ("Bootstrap: se esperaba APROBADA tras validacion (revise validation-service / score). Estado={0}" -f $trasValidar.estado)
        exit 1
    }

    $activBody = [PSCustomObject]@{
        tipo           = 'ONLINE'
        monto          = [decimal]99.99
        tokenPasarela  = 'tok_seed_demo'
    }
    $activJson = $activBody | ConvertTo-Json -Compress

    try {
        $activa = Invoke-RestMethod -Uri "$BaseUrl/solicitudes/$sid/activacion-vendedor" -Method Post -Body $activJson -ContentType 'application/json; charset=utf-8'
    }
    catch {
        $d = Get-RestFailureDetail -ErrorRecord $_
        Write-Host ("Bootstrap fallo al activar vendedor (pago): status={0} {1}" -f $d.Status, $d.Text)
        throw
    }

    if ("$($activa.estado)" -ne 'ACTIVA') {
        Write-Host ("Bootstrap error: se esperaba ACTIVA, estado={0}" -f $activa.estado)
        exit 1
    }

    return [long]$activa.id
}

Write-Host 'Consultando solicitudes de vendedores...'

try {
    $todas = Invoke-RestMethod -Uri $SolicitudesUri -Method Get
}
catch {
    $d = Get-RestFailureDetail -ErrorRecord $_
    Write-Host ("Error detallado: status={0} {1}" -f $d.Status, $d.Text)
    exit 1
}

$solicitudes = @($todas)
if ($solicitudes.Count -eq 0) {
    Write-Host 'GET /solicitudes: lista vacia (0 registros).'
}

$activas = @($solicitudes | Where-Object { "$($_.estado)" -eq 'ACTIVA' })

$vendedorSolicitudId = $null

if ($activas.Count -gt 0) {
    $vendedorSolicitudId = [long]$activas[0].id
}
elseif (-not $NoBootstrap) {
    try {
        $vendedorSolicitudId = Invoke-BootstrapVendedorActiva -BaseUrl $SolicitudBaseUrl
    }
    catch {
        exit 1
    }
}
else {
    if ($solicitudes.Count -eq 0) {
        Write-Host 'No hay vendedores en estado ACTIVA. Cree uno antes de cargar productos.'
    }
    else {
        Write-Host ("Hay {0} solicitud(es), ninguna ACTIVA. Estados: {1}" -f $solicitudes.Count, (($solicitudes | ForEach-Object { "$($_.id):$($_.estado)" }) -join ', '))
        Write-Host 'No hay vendedores en estado ACTIVA. Cree uno antes de cargar productos.'
        Write-Host 'Sugerencia: ejecute sin -NoBootstrap para crear y activar una solicitud de demo (requiere payment-service).'
    }
    exit 1
}

Write-Host ("Vendedor activo encontrado: ID = {0}" -f $vendedorSolicitudId)

$productos = @(
    @{ nombre = 'Laptop Pro'; precio = 1200; descripcion = 'Ultrabook para productividad.'; categorias = @('Tecnologia', 'Computadores', 'Portatiles') }
    @{ nombre = 'Laptop Gamer'; precio = 1800; descripcion = 'Alto rendimiento para juegos.'; categorias = @('Tecnologia', 'Computadores', 'Portatiles') }
    @{ nombre = 'Smartphone X'; precio = 800; descripcion = 'Pantalla OLED y triple camara.'; categorias = @('Tecnologia', 'Moviles', 'Smartphones') }
    @{ nombre = 'Smartphone Lite'; precio = 500; descripcion = 'Buena autonomia y precio.'; categorias = @('Tecnologia', 'Moviles', 'Smartphones') }
    @{ nombre = 'Audifonos Pro'; precio = 150; descripcion = 'Cancelacion de ruido.'; categorias = @('Tecnologia', 'Audio', 'Auriculares') }
    @{ nombre = 'Audifonos Inalambricos'; precio = 200; descripcion = 'Bluetooth multipunto.'; categorias = @('Tecnologia', 'Audio', 'Auriculares') }
    @{ nombre = 'Smartwatch'; precio = 300; descripcion = 'Sensores de salud y GPS.'; categorias = @('Tecnologia', 'Wearables') }
    @{ nombre = 'Camara HD'; precio = 500; descripcion = 'Ideal para vlogging.'; categorias = @('Tecnologia', 'Fotografia', 'Compactas') }
    @{ nombre = 'Camara Profesional'; precio = 1500; descripcion = 'Sensor full frame.'; categorias = @('Tecnologia', 'Fotografia', 'Profesional') }
    @{ nombre = 'Tablet Pro'; precio = 700; descripcion = 'Lapiz optico incluido.'; categorias = @('Tecnologia', 'Tablets') }
    @{ nombre = 'Teclado Mecanico'; precio = 120; descripcion = 'Switches tactiles RGB.'; categorias = @('Tecnologia', 'Perifericos', 'Teclados') }
    @{ nombre = 'Mouse Gamer'; precio = 80; descripcion = 'Sensor alta DPI.'; categorias = @('Tecnologia', 'Perifericos', 'Ratones') }
    @{ nombre = 'Monitor 4K'; precio = 900; descripcion = 'IPS 27 pulgadas HDR400.'; categorias = @('Tecnologia', 'Monitores') }
    @{ nombre = 'Monitor Curvo'; precio = 600; descripcion = '144 Hz curvatura 1500R.'; categorias = @('Tecnologia', 'Monitores') }
    @{ nombre = 'Consola Gaming'; precio = 1000; descripcion = 'SSD rapido next-gen.'; categorias = @('Tecnologia', 'Gaming', 'Consolas') }
    @{ nombre = 'Parlantes Bluetooth'; precio = 180; descripcion = 'Estereo portatil IPX7.'; categorias = @('Tecnologia', 'Audio', 'Parlantes') }
    @{ nombre = 'Disco SSD'; precio = 220; descripcion = 'NVMe Gen4 1 TB.'; categorias = @('Tecnologia', 'Almacenamiento', 'SSD') }
    @{ nombre = 'Disco HDD'; precio = 100; descripcion = '3.5 pulgadas 2 TB.'; categorias = @('Tecnologia', 'Almacenamiento', 'HDD') }
    @{ nombre = 'Router WiFi'; precio = 200; descripcion = 'Wi-Fi 6 MU-MIMO.'; categorias = @('Tecnologia', 'Redes') }
    @{ nombre = 'Microfono USB'; precio = 130; descripcion = 'Patron cardioide podcast.'; categorias = @('Tecnologia', 'Audio', 'Microfonos') }
)

foreach ($p in $productos) {
    Write-Host ("Creando producto: {0}" -f $p.nombre)

    $payload = [PSCustomObject]@{
        vendedorSolicitudId = $vendedorSolicitudId
        nombre              = $p.nombre
        precio              = [decimal]$p.precio
        descripcion         = $p.descripcion
        categorias          = [string[]]@($p.categorias)
    }

    $jsonBody = $payload | ConvertTo-Json -Compress -Depth 5

    try {
        $null = Invoke-RestMethod -Uri $ProductosUri -Method Post -Body $jsonBody -ContentType 'application/json; charset=utf-8'
        Write-Host 'Producto creado correctamente'
    }
    catch {
        $d = Get-RestFailureDetail -ErrorRecord $_
        Write-Host ("Error detallado: status={0} {1}" -f $d.Status, $d.Text)
        exit 1
    }
}

Write-Host 'Productos cargados correctamente'
