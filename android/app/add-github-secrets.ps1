# Add GitHub Secrets for Android Release using GitHub CLI
# This script uploads your keystore and credentials to GitHub Secrets

Write-Host "`n==================================" -ForegroundColor Cyan
Write-Host "  GitHub Secrets Setup (gh CLI)" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan

# Check if gh CLI is installed
try {
    gh --version | Out-Null
} catch {
    Write-Host "`n❌ GitHub CLI (gh) is not installed!" -ForegroundColor Red
    Write-Host "`nPlease install it from: https://cli.github.com/" -ForegroundColor Yellow
    Write-Host "Or using winget:" -ForegroundColor Yellow
    Write-Host "  winget install --id GitHub.cli" -ForegroundColor Gray
    exit 1
}

Write-Host "`n✅ GitHub CLI is installed" -ForegroundColor Green

# Check if authenticated
Write-Host "`n🔐 Checking authentication..." -ForegroundColor Yellow
try {
    $authStatus = gh auth status 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "`n❌ Not authenticated with GitHub!" -ForegroundColor Red
        Write-Host "`nPlease run: gh auth login" -ForegroundColor Yellow
        exit 1
    }
    Write-Host "✅ Authenticated with GitHub" -ForegroundColor Green
} catch {
    Write-Host "`n❌ Authentication check failed!" -ForegroundColor Red
    Write-Host "`nPlease run: gh auth login" -ForegroundColor Yellow
    exit 1
}

# Check if keystore exists
$keystoreFile = "suled-release-key.jks"
$keystoreFullPath = Join-Path $PSScriptRoot $keystoreFile

if (-not (Test-Path $keystoreFullPath)) {
    Write-Host "`n❌ Keystore file not found: $keystoreFullPath" -ForegroundColor Red
    Write-Host "`nPlease generate your keystore first!" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Found keystore: $keystoreFile" -ForegroundColor Green

# Check if keystore.properties exists
$keystorePropsPath = Join-Path $PSScriptRoot "keystore.properties"
if (-not (Test-Path $keystorePropsPath)) {
    Write-Host "`n❌ keystore.properties not found!" -ForegroundColor Red
    Write-Host "`nPlease create keystore.properties with your passwords first!" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Found keystore.properties" -ForegroundColor Green

# Read keystore.properties
Write-Host "`n📖 Reading keystore.properties..." -ForegroundColor Yellow
$keystoreProps = @{}
Get-Content $keystorePropsPath | ForEach-Object {
    if ($_ -match "^\s*([^#][^=]*?)\s*=\s*(.*)$") {
        $key = $matches[1].Trim()
        $value = $matches[2].Trim()
        $keystoreProps[$key] = $value
    }
}

# Validate required properties
$requiredProps = @('storePassword', 'keyAlias', 'keyPassword')
$missingProps = @()

foreach ($prop in $requiredProps) {
    if (-not $keystoreProps.ContainsKey($prop) -or [string]::IsNullOrWhiteSpace($keystoreProps[$prop])) {
        $missingProps += $prop
    }
}

if ($missingProps.Count -gt 0) {
    Write-Host "`n❌ Missing required properties in keystore.properties:" -ForegroundColor Red
    $missingProps | ForEach-Object { Write-Host "   - $_" -ForegroundColor Yellow }
    exit 1
}

Write-Host "✅ All required properties found" -ForegroundColor Green

# Show what will be uploaded
Write-Host "`n==================================" -ForegroundColor Cyan
Write-Host "  Ready to Upload These Secrets:" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host "Repository: " -NoNewline
Write-Host "EvgenSk/suled-mobile" -ForegroundColor Yellow

Write-Host "`n1. KEYSTORE_BASE64" -ForegroundColor White
Write-Host "   Source: $keystoreFile (encoded)" -ForegroundColor Gray

Write-Host "`n2. KEYSTORE_PASSWORD" -ForegroundColor White
$maskedStorePass = $keystoreProps['storePassword'][0] + ("*" * ($keystoreProps['storePassword'].Length - 2)) + $keystoreProps['storePassword'][-1]
Write-Host "   Value: $maskedStorePass" -ForegroundColor Gray

Write-Host "`n3. KEY_ALIAS" -ForegroundColor White
Write-Host "   Value: $($keystoreProps['keyAlias'])" -ForegroundColor Gray

Write-Host "`n4. KEY_PASSWORD" -ForegroundColor White
$maskedKeyPass = $keystoreProps['keyPassword'][0] + ("*" * ($keystoreProps['keyPassword'].Length - 2)) + $keystoreProps['keyPassword'][-1]
Write-Host "   Value: $maskedKeyPass" -ForegroundColor Gray

Write-Host "`n==================================" -ForegroundColor Cyan

# Confirm
Write-Host "`n⚠️  This will upload sensitive data to GitHub Secrets." -ForegroundColor Yellow
$confirm = Read-Host "`nDo you want to continue? (yes/no)"

if ($confirm -ne "yes") {
    Write-Host "`n❌ Cancelled by user." -ForegroundColor Red
    exit 0
}

Write-Host "`n🚀 Uploading secrets to GitHub..." -ForegroundColor Yellow

# Convert keystore to base64
Write-Host "`n1/4 Converting keystore to base64..." -ForegroundColor Cyan
try {
    $bytes = [System.IO.File]::ReadAllBytes($keystoreFullPath)
    $base64 = [Convert]::ToBase64String($bytes)
    Write-Host "✅ Keystore converted" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to convert keystore: $_" -ForegroundColor Red
    exit 1
}

# Upload KEYSTORE_BASE64
Write-Host "`n2/4 Uploading KEYSTORE_BASE64..." -ForegroundColor Cyan
try {
    $base64 | gh secret set KEYSTORE_BASE64 --repo EvgenSk/suled-mobile
    Write-Host "✅ KEYSTORE_BASE64 uploaded" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to upload KEYSTORE_BASE64: $_" -ForegroundColor Red
    exit 1
}

# Upload KEYSTORE_PASSWORD
Write-Host "`n3/4 Uploading KEYSTORE_PASSWORD..." -ForegroundColor Cyan
try {
    $keystoreProps['storePassword'] | gh secret set KEYSTORE_PASSWORD --repo EvgenSk/suled-mobile
    Write-Host "✅ KEYSTORE_PASSWORD uploaded" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to upload KEYSTORE_PASSWORD: $_" -ForegroundColor Red
    exit 1
}

# Upload KEY_ALIAS
Write-Host "`n4/4 Uploading KEY_ALIAS..." -ForegroundColor Cyan
try {
    $keystoreProps['keyAlias'] | gh secret set KEY_ALIAS --repo EvgenSk/suled-mobile
    Write-Host "✅ KEY_ALIAS uploaded" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to upload KEY_ALIAS: $_" -ForegroundColor Red
    exit 1
}

# Upload KEY_PASSWORD
Write-Host "`n5/5 Uploading KEY_PASSWORD..." -ForegroundColor Cyan
try {
    $keystoreProps['keyPassword'] | gh secret set KEY_PASSWORD --repo EvgenSk/suled-mobile
    Write-Host "✅ KEY_PASSWORD uploaded" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to upload KEY_PASSWORD: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`n==================================" -ForegroundColor Green
Write-Host "  ✅ All Secrets Uploaded!" -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Green

# Verify secrets
Write-Host "`n🔍 Verifying secrets on GitHub..." -ForegroundColor Yellow
try {
    $secrets = gh secret list --repo EvgenSk/suled-mobile 2>&1
    if ($secrets -match "KEYSTORE_BASE64") {
        Write-Host "✅ Secrets verified on GitHub" -ForegroundColor Green
        Write-Host "`nUploaded secrets:" -ForegroundColor Cyan
        Write-Host $secrets -ForegroundColor Gray
    }
} catch {
    Write-Host "⚠️  Could not verify secrets, but upload likely succeeded" -ForegroundColor Yellow
}

Write-Host "`n==================================" -ForegroundColor Cyan
Write-Host "  Next Steps:" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan

Write-Host "`n1. Commit and push your code (if not already):" -ForegroundColor Yellow
Write-Host "   git add ." -ForegroundColor Gray
Write-Host "   git commit -m ""Setup Android release""`n   git push" -ForegroundColor Gray

Write-Host "`n2. Create and push a version tag to trigger release:" -ForegroundColor Yellow
Write-Host "   git tag v1.0.0" -ForegroundColor Gray
Write-Host "   git push origin v1.0.0" -ForegroundColor Gray

Write-Host "`n3. Watch the release build:" -ForegroundColor Yellow
Write-Host "   https://github.com/EvgenSk/suled-mobile/actions" -ForegroundColor Gray

Write-Host "`n4. Download the APK/AAB when ready:" -ForegroundColor Yellow
Write-Host "   https://github.com/EvgenSk/suled-mobile/releases" -ForegroundColor Gray

Write-Host "`n==================================" -ForegroundColor Cyan
Write-Host "`n🎉 GitHub Actions setup complete!" -ForegroundColor Green
Write-Host "Your Android releases are now automated.`n" -ForegroundColor Green
