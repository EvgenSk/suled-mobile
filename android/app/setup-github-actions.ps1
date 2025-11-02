# Script to encode keystore for GitHub Actions
# Run this to prepare your keystore for GitHub Secrets

$keystoreFile = "suled-release-key.jks"

Write-Host "`n==================================" -ForegroundColor Cyan
Write-Host "  GitHub Actions Keystore Setup" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan

# Check if keystore exists
if (-not (Test-Path $keystoreFile)) {
    Write-Host "`n❌ Keystore file not found: $keystoreFile" -ForegroundColor Red
    Write-Host "`nPlease generate your keystore first using:" -ForegroundColor Yellow
    Write-Host "  keytool -genkey -v -keystore $keystoreFile -keyalg RSA -keysize 2048 -validity 10000 -alias suled" -ForegroundColor Gray
    Write-Host "`nOr see RELEASE_SETUP.md for complete instructions.`n" -ForegroundColor Yellow
    exit 1
}

Write-Host "`n✅ Found keystore: $keystoreFile" -ForegroundColor Green

# Convert to base64
Write-Host "`n🔄 Converting keystore to base64..." -ForegroundColor Yellow

try {
    $bytes = [System.IO.File]::ReadAllBytes($keystoreFile)
    $base64 = [Convert]::ToBase64String($bytes)
    
    # Save to clipboard
    $base64 | Set-Clipboard
    
    # Save to file
    $outputFile = "keystore-base64.txt"
    $base64 | Out-File $outputFile -Encoding UTF8
    
    Write-Host "✅ Conversion successful!" -ForegroundColor Green
    Write-Host "`n📋 Base64 string has been:" -ForegroundColor Cyan
    Write-Host "   1. Copied to your clipboard" -ForegroundColor White
    Write-Host "   2. Saved to: $outputFile" -ForegroundColor White
    
    # Show preview
    $preview = $base64.Substring(0, [Math]::Min(50, $base64.Length))
    Write-Host "`n📝 Preview (first 50 characters):" -ForegroundColor Cyan
    Write-Host "   $preview..." -ForegroundColor Gray
    
    # Instructions
    Write-Host "`n==================================" -ForegroundColor Cyan
    Write-Host "  Next Steps:" -ForegroundColor Cyan
    Write-Host "==================================" -ForegroundColor Cyan
    Write-Host "`n1. Go to your GitHub repository" -ForegroundColor Yellow
    Write-Host "   https://github.com/YOUR_USERNAME/suled-mobile" -ForegroundColor Gray
    
    Write-Host "`n2. Navigate to Settings → Secrets and variables → Actions" -ForegroundColor Yellow
    
    Write-Host "`n3. Click 'New repository secret' and add these secrets:" -ForegroundColor Yellow
    Write-Host "`n   Name: KEYSTORE_BASE64" -ForegroundColor White
    Write-Host "   Value: [Paste from clipboard - Ctrl+V]" -ForegroundColor Gray
    
    Write-Host "`n   Name: KEYSTORE_PASSWORD" -ForegroundColor White
    Write-Host "   Value: [Your keystore password]" -ForegroundColor Gray
    
    Write-Host "`n   Name: KEY_ALIAS" -ForegroundColor White
    Write-Host "   Value: suled" -ForegroundColor Gray
    
    Write-Host "`n   Name: KEY_PASSWORD" -ForegroundColor White
    Write-Host "   Value: [Your key password]" -ForegroundColor Gray
    
    Write-Host "`n4. Delete the keystore-base64.txt file after adding to GitHub!" -ForegroundColor Yellow
    Write-Host "   Remove-Item $outputFile" -ForegroundColor Gray
    
    Write-Host "`n5. Create a release by pushing a version tag:" -ForegroundColor Yellow
    Write-Host "   git tag v1.0.0" -ForegroundColor Gray
    Write-Host "   git push origin v1.0.0" -ForegroundColor Gray
    
    Write-Host "`n==================================" -ForegroundColor Cyan
    Write-Host "`n⚠️  SECURITY WARNING:" -ForegroundColor Red
    Write-Host "   - Delete $outputFile after adding to GitHub Secrets" -ForegroundColor Yellow
    Write-Host "   - Never commit keystore files to git" -ForegroundColor Yellow
    Write-Host "   - Keep your keystore backed up separately" -ForegroundColor Yellow
    
    Write-Host "`n✅ Setup complete! See GITHUB_ACTIONS_SETUP.md for details.`n" -ForegroundColor Green
    
} catch {
    Write-Host "`n❌ Error converting keystore: $_" -ForegroundColor Red
    Write-Host "`nPlease check that the keystore file is valid.`n" -ForegroundColor Yellow
    exit 1
}
