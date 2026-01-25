# Quick Diagnostic Script
# Run this to verify everything is configured correctly

Write-Host "=== Android Build Configuration Diagnostic ===" -ForegroundColor Cyan
Write-Host ""

# Check keystore file
Write-Host "1. Checking keystore file..." -ForegroundColor Yellow
$keystorePath = "D:\Projects\Software\Suled\suled-mobile\android\app\suled-release-key.jks"
if (Test-Path $keystorePath) {
    $keystore = Get-Item $keystorePath
    Write-Host "   ✅ Keystore exists: $($keystore.FullName)" -ForegroundColor Green
    Write-Host "   Size: $($keystore.Length) bytes" -ForegroundColor Gray
    Write-Host "   Modified: $($keystore.LastWriteTime)" -ForegroundColor Gray
} else {
    Write-Host "   ❌ Keystore NOT FOUND at: $keystorePath" -ForegroundColor Red
}
Write-Host ""

# Check keystore.properties
Write-Host "2. Checking keystore.properties..." -ForegroundColor Yellow
$propsPath = "D:\Projects\Software\Suled\suled-mobile\android\app\keystore.properties"
if (Test-Path $propsPath) {
    Write-Host "   ✅ keystore.properties exists" -ForegroundColor Green
    $content = Get-Content $propsPath -Raw

    # Check for proper escaping
    if ($content -match "storePassword=.*\\\\.*") {
        Write-Host "   ✅ Password has proper escaping (\\\\)" -ForegroundColor Green
    } elseif ($content -match "storePassword=.*\\[^\\].*") {
        Write-Host "   ⚠️  WARNING: Password has single backslash! Needs double (\\\\)" -ForegroundColor Red
        Write-Host "   Fix: Change \I to \\I in keystore.properties" -ForegroundColor Yellow
    } else {
        Write-Host "   ✅ Password appears OK" -ForegroundColor Green
    }

    # Check storeFile path
    if ($content -match "storeFile=\.\./suled-release-key\.jks") {
        Write-Host "   ✅ storeFile path is correct: ../suled-release-key.jks" -ForegroundColor Green
    } else {
        Write-Host "   ⚠️  storeFile path might be wrong" -ForegroundColor Yellow
    }
} else {
    Write-Host "   ❌ keystore.properties NOT FOUND" -ForegroundColor Red
}
Write-Host ""

# Test Gradle build
Write-Host "3. Testing Gradle build from command line..." -ForegroundColor Yellow
Set-Location "D:\Projects\Software\Suled\suled-mobile\android\app"

Write-Host "   Running: .\gradlew.bat :app:assembleRelease --quiet" -ForegroundColor Gray
$output = .\gradlew.bat :app:assembleRelease --quiet 2>&1 | Out-String

if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ Gradle build SUCCESSFUL from command line" -ForegroundColor Green

    # Check if APK exists
    $apkPath = "app\build\outputs\apk\release\app-release.apk"
    if (Test-Path $apkPath) {
        $apk = Get-Item $apkPath
        Write-Host "   ✅ Release APK created: $($apk.Length) bytes" -ForegroundColor Green
    }
} else {
    Write-Host "   ❌ Gradle build FAILED from command line" -ForegroundColor Red
    Write-Host "   Error output:" -ForegroundColor Red
    Write-Host $output -ForegroundColor Red
}
Write-Host ""

# Check Gradle daemon
Write-Host "4. Checking Gradle daemon status..." -ForegroundColor Yellow
$daemonStatus = .\gradlew.bat --status 2>&1 | Out-String
Write-Host $daemonStatus -ForegroundColor Gray
Write-Host ""

# Summary and recommendations
Write-Host "=== Summary ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "If Gradle command line works but Android Studio fails:" -ForegroundColor Yellow
Write-Host "1. In Android Studio: File → Invalidate Caches... → Invalidate and Restart" -ForegroundColor White
Write-Host "2. In Android Studio: File → Sync Project with Gradle Files" -ForegroundColor White
Write-Host "3. In Android Studio: Build → Clean Project" -ForegroundColor White
Write-Host "4. Try building again" -ForegroundColor White
Write-Host ""
Write-Host "See ANDROID_STUDIO_BUILD_FIX.md for detailed instructions" -ForegroundColor Cyan

