# Preview Android Vector Drawables in Browser
# Converts XML vector drawables to SVG and opens in browser

param(
    [string]$xmlFile = "d:\Projects\Software\Suled\suled-mobile\android\wear\src\main\res\drawable\ic_court.xml"
)

function Convert-AndroidVectorToSvg {
    param([string]$xmlPath)
    
    [xml]$xml = Get-Content $xmlPath
    $vector = $xml.vector
    
    $width = $vector.width -replace 'dp', ''
    $height = $vector.height -replace 'dp', ''
    $viewBox = "$($vector.viewportWidth) $($vector.viewportHeight)"
    
    $svg = @"
<svg width="$width" height="$height" viewBox="0 0 $viewBox" xmlns="http://www.w3.org/2000/svg">
"@
    
    foreach ($path in $vector.path) {
        $fill = $path.fillColor
        $stroke = $path.strokeColor
        $strokeWidth = $path.strokeWidth
        $pathData = $path.pathData
        
        $svg += "`n  <path"
        if ($fill) { $svg += " fill=`"$fill`"" }
        if ($stroke) { $svg += " stroke=`"$stroke`"" }
        if ($strokeWidth) { $svg += " stroke-width=`"$strokeWidth`"" }
        $svg += " d=`"$pathData`" />"
    }
    
    $svg += "`n</svg>"
    return $svg
}

# Preview ic_court.xml
$courtSvg = Convert-AndroidVectorToSvg "d:\Projects\Software\Suled\suled-mobile\android\wear\src\main\res\drawable\ic_court.xml"

# Preview tile_preview.xml
$tileSvg = Convert-AndroidVectorToSvg "d:\Projects\Software\Suled\suled-mobile\android\wear\src\main\res\drawable\tile_preview.xml"

# Preview ic_shuttlecock.xml
$shuttleSvg = Convert-AndroidVectorToSvg "d:\Projects\Software\Suled\suled-mobile\android\wear\src\main\res\drawable\ic_shuttlecock.xml"

# Create HTML preview
$html = @"
<!DOCTYPE html>
<html>
<head>
    <title>Badminton Icons Preview</title>
    <style>
        body { font-family: Arial, sans-serif; padding: 40px; background: #f5f5f5; }
        .preview { background: white; padding: 30px; margin: 20px 0; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
        h2 { color: #333; margin-top: 0; }
        .icon-container { display: inline-block; padding: 20px; margin: 10px; }
        .dark { background: #1E1E1E; border-radius: 8px; }
        .light { background: #E0E0E0; border-radius: 8px; }
        .label { margin-top: 10px; color: #666; font-size: 14px; }
    </style>
</head>
<body>
    <h1>🏸 Badminton Court Icons Preview</h1>
    
    <div class="preview">
        <h2>Court Icon (ic_court.xml)</h2>
        <p>Used for complications and app icon</p>
        
        <div class="icon-container dark">
            $courtSvg
            <div class="label">On Dark Background</div>
        </div>
        
        <div class="icon-container light">
            $courtSvg
            <div class="label">On Light Background</div>
        </div>
        
        <div class="icon-container dark">
            <svg width="48" height="48" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                $($courtSvg -replace '<svg[^>]*>', '' -replace '</svg>', '')
            </svg>
            <div class="label">2x Size</div>
        </div>
    </div>
    
    <div class="preview">
        <h2>Shuttlecock Icon (ic_shuttlecock.xml)</h2>
        <p>Alternative icon design - shuttlecock</p>
        
        <div class="icon-container dark">
            $shuttleSvg
            <div class="label">On Dark Background</div>
        </div>
        
        <div class="icon-container light">
            $shuttleSvg
            <div class="label">On Light Background</div>
        </div>
        
        <div class="icon-container dark">
            <svg width="48" height="48" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                $($shuttleSvg -replace '<svg[^>]*>', '' -replace '</svg>', '')
            </svg>
            <div class="label">2x Size</div>
        </div>
    </div>
    
    <div class="preview">
        <h2>Tile Preview (tile_preview.xml)</h2>
        <p>Preview image for watch tile</p>
        
        <div class="icon-container">
            $tileSvg
        </div>
    </div>
    
</body>
</html>
"@

$tempFile = "$env:TEMP\badminton_icons_preview.html"
$html | Out-File $tempFile -Encoding UTF8

Write-Host "✅ Opening preview in browser..." -ForegroundColor Green
Start-Process $tempFile

Write-Host ""
Write-Host "📁 Preview file: $tempFile" -ForegroundColor Cyan
