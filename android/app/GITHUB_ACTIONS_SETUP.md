# GitHub Actions Setup for Android Release

This guide shows you how to set up automated release builds using GitHub Actions.

## 🎯 What This Does

- **Automated Builds**: Automatically build APK/AAB on every tag or manual trigger
- **Secure Signing**: Keystore stored securely in GitHub Secrets
- **GitHub Releases**: Automatically create releases with downloadable APK/AAB
- **No Local Keystore Needed**: Team members can trigger releases without keystore access

## 📋 Prerequisites

1. Your keystore file (`suled-release-key.jks`)
2. Your keystore passwords
3. GitHub repository admin access

## 🔐 Step 1: Prepare Your Keystore

### Convert Keystore to Base64

This encodes your keystore file so it can be stored as a GitHub Secret:

```powershell
# Windows PowerShell
cd D:\Projects\Software\Suled\suled-mobile\android\app

# Convert keystore to base64
$bytes = [System.IO.File]::ReadAllBytes("suled-release-key.jks")
$base64 = [Convert]::ToBase64String($bytes)
$base64 | Set-Clipboard

# The base64 string is now in your clipboard!
# Or save to file:
$base64 | Out-File keystore-base64.txt
```

**⚠️ IMPORTANT**: The `keystore-base64.txt` file contains your signing key. Delete it after adding to GitHub Secrets!

## 🔑 Step 2: Add GitHub Secrets

Go to your GitHub repository:
1. Navigate to **Settings** → **Secrets and variables** → **Actions**
2. Click **New repository secret**
3. Add these 4 secrets:

| Secret Name | Value | Example |
|-------------|-------|---------|
| `KEYSTORE_BASE64` | The base64 string from Step 1 | `MIIKLAIBAzCCCe...` (very long) |
| `KEYSTORE_PASSWORD` | Your keystore password | `Suled2025!` |
| `KEY_ALIAS` | Your key alias | `suled` |
| `KEY_PASSWORD` | Your key password | `Suled2025!` |

### Visual Guide:
```
GitHub → Your Repo → Settings → Secrets and variables → Actions
  → New repository secret
    Name: KEYSTORE_BASE64
    Secret: [paste the base64 string]
    → Add secret
```

## 🚀 Step 3: Using the Workflows

### Option A: Automatic Release (Git Tags)

Create a release by pushing a version tag:

```powershell
# From your local repository
cd D:\Projects\Software\Suled\suled-mobile

# Create and push a version tag
git tag v1.0.0
git push origin v1.0.0
```

**What happens:**
1. GitHub Actions automatically triggers
2. Builds signed APK and AAB
3. Creates a GitHub Release with the files attached
4. You can download from Releases page

### Option B: Manual Trigger

Trigger a build manually from GitHub:

1. Go to **Actions** tab in your repo
2. Select **Android Release Build** workflow
3. Click **Run workflow**
4. Enter version number (e.g., `1.0.0`)
5. Click **Run workflow**

**What happens:**
1. Builds signed APK and AAB
2. Files available as artifacts (downloadable for 90 days)
3. No automatic GitHub Release created

### Option C: Automatic on Push (CI)

Every push to `master` or `develop` branch:
- Runs tests
- Builds debug APK
- Uploads artifacts

## 📦 Step 4: Download Build Artifacts

### From GitHub Releases (Tags):
1. Go to **Releases** page
2. Find your version (e.g., v1.0.0)
3. Download:
   - `app-release.apk` - For direct distribution
   - `app-release.aab` - For Google Play Store

### From Actions Artifacts (Manual runs):
1. Go to **Actions** tab
2. Click on the workflow run
3. Scroll to **Artifacts** section
4. Download:
   - `app-release.apk`
   - `app-release.aab`

## 🔄 Workflow Files

### `android-release.yml`
- **Triggers**: Version tags (`v*.*.*`) or manual trigger
- **Builds**: Signed APK and AAB
- **Output**: GitHub Release or Artifacts

### `android-ci.yml` (existing)
- **Triggers**: Push to master/develop
- **Builds**: Debug APK
- **Runs**: Tests and lint

## 📝 Version Management

### Semantic Versioning
Use semantic versioning for tags:
- `v1.0.0` - Major release
- `v1.1.0` - Minor update (new features)
- `v1.0.1` - Patch (bug fixes)

### Update build.gradle.kts
Before creating a release tag, update version in `app/build.gradle.kts`:

```kotlin
defaultConfig {
    versionCode = 2  // Increment for each release
    versionName = "1.1.0"  // Match your tag
}
```

## 🎯 Complete Release Process

### 1. Update Version
```kotlin
// app/build.gradle.kts
versionCode = 2
versionName = "1.1.0"
```

### 2. Commit Changes
```powershell
git add .
git commit -m "Bump version to 1.1.0"
git push
```

### 3. Create and Push Tag
```powershell
git tag v1.1.0
git push origin v1.1.0
```

### 4. Wait for GitHub Actions
- Watch the Actions tab for progress
- Takes about 5-10 minutes

### 5. Download from Releases
- Go to Releases page
- Download APK/AAB
- Test before publishing to Play Store

## 🔒 Security Best Practices

### ✅ DO:
- Store keystore only in GitHub Secrets
- Use strong passwords
- Limit repository access
- Delete local `keystore-base64.txt` after setup
- Backup keystore separately (encrypted)

### ❌ DON'T:
- Commit keystore files to git
- Share secrets in plain text
- Use weak passwords
- Store secrets in code or logs

## 🐛 Troubleshooting

### Build Fails with "Keystore not found"
- Check `KEYSTORE_BASE64` secret is set correctly
- Verify base64 encoding was successful

### "Incorrect password" Error
- Check `KEYSTORE_PASSWORD` and `KEY_PASSWORD` secrets
- Verify passwords are correct (test locally first)

### "Key alias not found"
- Check `KEY_ALIAS` secret matches your keystore
- Default is usually `suled`

### Workflow Doesn't Trigger
- Ensure tag format is `v*.*.*` (e.g., `v1.0.0`)
- Check workflow file is in `.github/workflows/`
- Verify you have push permission

## 📊 Workflow Status Badge

Add this to your README to show build status:

```markdown
![Android Release](https://github.com/YOUR_USERNAME/suled-mobile/actions/workflows/android-release.yml/badge.svg)
```

## 🎓 Advanced Options

### Deploy to Google Play Automatically

You can extend the workflow to automatically publish to Play Store using:
- [r0adkll/upload-google-play](https://github.com/r0adkll/upload-google-play)
- Google Play service account JSON key

### Notify on Slack/Discord

Add notification steps to alert your team when releases are ready.

### Multiple Environments

Create separate workflows for:
- `android-release-staging.yml` - Staging builds
- `android-release-production.yml` - Production builds

## 📚 Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Android Signing Guide](https://developer.android.com/studio/publish/app-signing)
- [Semantic Versioning](https://semver.org/)

---

## Quick Reference Commands

```powershell
# Create release
git tag v1.0.0
git push origin v1.0.0

# Delete tag (if mistake)
git tag -d v1.0.0
git push origin :refs/tags/v1.0.0

# Convert keystore to base64
$bytes = [System.IO.File]::ReadAllBytes("suled-release-key.jks")
[Convert]::ToBase64String($bytes) | Set-Clipboard

# Check workflow status
gh run list --workflow=android-release.yml
```

---

**Your automated release pipeline is ready!** 🎉

Just add the GitHub Secrets and push a version tag to create your first automated release.
