# Suled Android App - Build Script
# Quick commands for building the Android app

# Change to the android app directory
Set-Location "D:\Projects\Software\Suled\suled-mobile\android\app"

function Show-Menu {
    Write-Host "`n==================================" -ForegroundColor Cyan
    Write-Host "   Suled Android Build Menu" -ForegroundColor Cyan
    Write-Host "==================================" -ForegroundColor Cyan
    Write-Host "1. Build Debug APK (for testing)"
    Write-Host "2. Build Release APK (signed)"
    Write-Host "3. Build Release AAB (for Play Store)"
    Write-Host "4. Install Debug APK to connected device"
    Write-Host "5. Clean build"
    Write-Host "6. Run tests"
    Write-Host "7. Check keystore setup"
    Write-Host "8. Open output folder"
    Write-Host "Q. Quit"
    Write-Host "==================================" -ForegroundColor Cyan
}

function Build-DebugAPK {
    Write-Host "`n🔨 Building Debug APK..." -ForegroundColor Yellow
    .\gradlew.bat assembleDebug
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Success! APK location:" -ForegroundColor Green
        Write-Host "   app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Cyan
    }
}

function Build-ReleaseAPK {
    Write-Host "`n🔨 Building Release APK..." -ForegroundColor Yellow
    if (-not (Test-Path "keystore.properties")) {
        Write-Host "⚠️  Warning: keystore.properties not found!" -ForegroundColor Red
        Write-Host "   The APK will not be signed. See RELEASE_SETUP.md" -ForegroundColor Yellow
    }
    .\gradlew.bat assembleRelease
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Success! APK location:" -ForegroundColor Green
        Write-Host "   app\build\outputs\apk\release\app-release.apk" -ForegroundColor Cyan
    }
}

function Build-ReleaseAAB {
    Write-Host "`n🔨 Building Release AAB (App Bundle)..." -ForegroundColor Yellow
    if (-not (Test-Path "keystore.properties")) {
        Write-Host "⚠️  Warning: keystore.properties not found!" -ForegroundColor Red
        Write-Host "   The AAB will not be signed. See RELEASE_SETUP.md" -ForegroundColor Yellow
    }
    .\gradlew.bat bundleRelease
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Success! AAB location:" -ForegroundColor Green
        Write-Host "   app\build\outputs\bundle\release\app-release.aab" -ForegroundColor Cyan
    }
}

function Install-DebugAPK {
    Write-Host "`n📱 Installing Debug APK to device..." -ForegroundColor Yellow
    $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
    
    if (-not (Test-Path $apkPath)) {
        Write-Host "❌ APK not found. Building first..." -ForegroundColor Red
        Build-DebugAPK
    }
    
    Write-Host "Checking for connected devices..." -ForegroundColor Gray
    adb devices
    
    Write-Host "`nInstalling..." -ForegroundColor Gray
    adb install -r $apkPath
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ App installed successfully!" -ForegroundColor Green
    }
}

function Clean-Build {
    Write-Host "`n🧹 Cleaning build..." -ForegroundColor Yellow
    .\gradlew.bat clean
    Write-Host "✅ Clean complete!" -ForegroundColor Green
}

function Run-Tests {
    Write-Host "`n🧪 Running tests..." -ForegroundColor Yellow
    .\gradlew.bat test
}

function Check-Keystore {
    Write-Host "`n🔐 Checking keystore setup..." -ForegroundColor Yellow
    
    if (Test-Path "keystore.properties") {
        Write-Host "✅ keystore.properties found" -ForegroundColor Green
        
        $props = Get-Content "keystore.properties" | ForEach-Object {
            if ($_ -match "^([^#].*)=(.*)$") {
                $key = $matches[1].Trim()
                if ($key -eq "storeFile") {
                    $value = $matches[2].Trim()
                    if (Test-Path $value) {
                        Write-Host "✅ Keystore file exists: $value" -ForegroundColor Green
                    } else {
                        Write-Host "❌ Keystore file not found: $value" -ForegroundColor Red
                    }
                }
            }
        }
    } else {
        Write-Host "❌ keystore.properties not found" -ForegroundColor Red
        Write-Host "`n📖 To set up release signing:" -ForegroundColor Yellow
        Write-Host "   1. Copy keystore.properties.template to keystore.properties"
        Write-Host "   2. Generate a keystore (see RELEASE_SETUP.md)"
        Write-Host "   3. Fill in the values in keystore.properties"
    }
}

function Open-OutputFolder {
    $outputPath = "app\build\outputs"
    if (Test-Path $outputPath) {
        Write-Host "`n📂 Opening output folder..." -ForegroundColor Yellow
        Invoke-Item $outputPath
    } else {
        Write-Host "❌ Output folder not found. Build something first!" -ForegroundColor Red
    }
}

# Main loop
do {
    Show-Menu
    $choice = Read-Host "`nEnter your choice"
    
    switch ($choice) {
        '1' { Build-DebugAPK }
        '2' { Build-ReleaseAPK }
        '3' { Build-ReleaseAAB }
        '4' { Install-DebugAPK }
        '5' { Clean-Build }
        '6' { Run-Tests }
        '7' { Check-Keystore }
        '8' { Open-OutputFolder }
        'Q' { 
            Write-Host "`n👋 Goodbye!" -ForegroundColor Cyan
            return 
        }
        default { Write-Host "`n❌ Invalid choice. Please try again." -ForegroundColor Red }
    }
    
    Write-Host "`nPress any key to continue..."
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    Clear-Host
    
} while ($true)
