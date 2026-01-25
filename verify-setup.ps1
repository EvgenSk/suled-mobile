# Verify Wear OS Integration Setup
# Run this script to check if all required files are in place

Write-Host "🔍 Verifying Wear OS Integration Setup..." -ForegroundColor Cyan
Write-Host ""

$baseDir = "d:\Projects\Software\Suled\suled-mobile"
$errors = 0
$warnings = 0

function Test-FileExists {
    param($path, $description)
    if (Test-Path $path) {
        Write-Host "  ✅ $description" -ForegroundColor Green
        return $true
    } else {
        Write-Host "  ❌ $description" -ForegroundColor Red
        Write-Host "     Missing: $path" -ForegroundColor DarkGray
        $script:errors++
        return $false
    }
}

function Test-FileContains {
    param($path, $text, $description)
    if (Test-Path $path) {
        $content = Get-Content $path -Raw
        if ($content -match [regex]::Escape($text)) {
            Write-Host "  ✅ $description" -ForegroundColor Green
            return $true
        } else {
            Write-Host "  ⚠️  $description - content not found" -ForegroundColor Yellow
            $script:warnings++
            return $false
        }
    } else {
        Write-Host "  ❌ $description - file missing" -ForegroundColor Red
        $script:errors++
        return $false
    }
}

# Check Gradle files
Write-Host "📦 Checking Gradle Configuration..." -ForegroundColor Yellow
Test-FileExists "$baseDir\android\app\build.gradle.kts" "Root build.gradle.kts"
Test-FileExists "$baseDir\android\app\app\build.gradle.kts" "App build.gradle.kts"
Test-FileExists "$baseDir\android\wear\build.gradle.kts" "Wear build.gradle.kts"
Test-FileExists "$baseDir\android\app\settings.gradle.kts" "settings.gradle.kts"

Test-FileContains "$baseDir\android\app\app\build.gradle.kts" "kotlinx-serialization-json" "Serialization dependency in app"
Test-FileContains "$baseDir\android\app\app\build.gradle.kts" "play-services-wearable" "Wearable dependency in app"
Test-FileContains "$baseDir\android\wear\build.gradle.kts" "androidx.wear:wear" "Wear dependency in wear"
Test-FileContains "$baseDir\android\app\settings.gradle.kts" "include(`":wear`")" "Wear module included"

Write-Host ""

# Check Manifests
Write-Host "📋 Checking Android Manifests..." -ForegroundColor Yellow
Test-FileExists "$baseDir\android\app\app\src\main\AndroidManifest.xml" "App AndroidManifest.xml"
Test-FileExists "$baseDir\android\wear\src\main\AndroidManifest.xml" "Wear AndroidManifest.xml"

Test-FileContains "$baseDir\android\wear\src\main\AndroidManifest.xml" "WearDataListenerService" "WearDataListenerService in manifest"
Test-FileContains "$baseDir\android\wear\src\main\AndroidManifest.xml" "NextGameComplication" "NextGameComplication in manifest"
Test-FileContains "$baseDir\android\wear\src\main\AndroidManifest.xml" "NextGameTileService" "NextGameTileService in manifest"

Write-Host ""

# Check Kotlin files - Shared
Write-Host "📝 Checking Shared Kotlin Files..." -ForegroundColor Yellow
Test-FileExists "$baseDir\shared\commonMain\kotlin\com\suled\models\TrackingModels.kt" "TrackingModels.kt"
Test-FileExists "$baseDir\shared\commonMain\kotlin\com\suled\data\LocalStorageService.kt" "LocalStorageService.kt"

Write-Host ""

# Check Kotlin files - Android App
Write-Host "📝 Checking Android App Kotlin Files..." -ForegroundColor Yellow
Test-FileExists "$baseDir\android\app\src\main\kotlin\com\suled\data\AndroidLocalStorage.kt" "AndroidLocalStorage.kt"
Test-FileExists "$baseDir\android\app\src\main\kotlin\com\suled\wear\WearDataSyncService.kt" "WearDataSyncService.kt"

Write-Host ""

# Check Kotlin files - Wear
Write-Host "📝 Checking Wear Kotlin Files..." -ForegroundColor Yellow
Test-FileExists "$baseDir\android\wear\src\main\kotlin\com\suled\data\WatchLocalStorage.kt" "WatchLocalStorage.kt"
Test-FileExists "$baseDir\android\wear\src\main\kotlin\com\suled\wear\WearDataListenerService.kt" "WearDataListenerService.kt"
Test-FileExists "$baseDir\android\wear\src\main\kotlin\com\suled\wear\NextGameComplication.kt" "NextGameComplication.kt"
Test-FileExists "$baseDir\android\wear\src\main\kotlin\com\suled\wear\NextGameTileService.kt" "NextGameTileService.kt"
Test-FileExists "$baseDir\android\wear\src\main\kotlin\com\suled\wear\MainActivity.kt" "MainActivity.kt"

Write-Host ""

# Check Resources
Write-Host "🎨 Checking Resources..." -ForegroundColor Yellow
Test-FileExists "$baseDir\android\wear\src\main\res\drawable\ic_court.xml" "ic_court.xml drawable"
Test-FileExists "$baseDir\android\wear\src\main\res\drawable\tile_preview.xml" "tile_preview.xml drawable"
Test-FileExists "$baseDir\android\wear\src\main\res\values\strings.xml" "strings.xml"
Test-FileExists "$baseDir\android\wear\src\main\res\values\colors.xml" "colors.xml"
Test-FileExists "$baseDir\android\wear\src\main\res\mipmap-anydpi-v26\ic_launcher.xml" "ic_launcher.xml"

Write-Host ""

# Check ProGuard
Write-Host "🛡️ Checking ProGuard Rules..." -ForegroundColor Yellow
Test-FileExists "$baseDir\android\wear\proguard-rules.pro" "proguard-rules.pro"

Write-Host ""

# Check Documentation
Write-Host "📚 Checking Documentation..." -ForegroundColor Yellow
Test-FileExists "$baseDir\QUICK_START.md" "QUICK_START.md"
Test-FileExists "$baseDir\INTEGRATION_COMPLETE.md" "INTEGRATION_COMPLETE.md"
Test-FileExists "$baseDir\MOBILE_INTEGRATION_GUIDE.md" "MOBILE_INTEGRATION_GUIDE.md"
Test-FileExists "$baseDir\IMPLEMENTATION_SUMMARY.md" "IMPLEMENTATION_SUMMARY.md"

Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host ""

# Summary
if ($errors -eq 0 -and $warnings -eq 0) {
    Write-Host "✅ ALL CHECKS PASSED!" -ForegroundColor Green
    Write-Host ""
    Write-Host "🚀 You're ready to:" -ForegroundColor Cyan
    Write-Host "   1. Run: .\copy-tracking-files.ps1" -ForegroundColor White
    Write-Host "   2. Open Android Studio" -ForegroundColor White
    Write-Host "   3. Sync Gradle" -ForegroundColor White
    Write-Host "   4. Build & Run" -ForegroundColor White
    Write-Host ""
    Write-Host "📖 See QUICK_START.md for detailed instructions" -ForegroundColor Cyan
} elseif ($errors -eq 0) {
    Write-Host "⚠️  CHECKS PASSED WITH WARNINGS" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Found $warnings warning(s)" -ForegroundColor Yellow
    Write-Host "Review the warnings above and fix if needed." -ForegroundColor White
} else {
    Write-Host "❌ CHECKS FAILED" -ForegroundColor Red
    Write-Host ""
    Write-Host "Found $errors error(s) and $warnings warning(s)" -ForegroundColor Red
    Write-Host "Fix the errors above before proceeding." -ForegroundColor White
    Write-Host ""
    Write-Host "Need help? Check INTEGRATION_COMPLETE.md" -ForegroundColor Cyan
}

Write-Host ""
