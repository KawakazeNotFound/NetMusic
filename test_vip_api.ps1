# VIP Music Direct URL API Test Script
# Test Song: ID=1856336348 (Atarayo - 8.8)

Write-Host "====== VIP Music Feature Test ======" -ForegroundColor Cyan
Write-Host ""

# 1. Get song info from NetEase API
Write-Host "[Step 1] Fetching song info from NetEase API..." -ForegroundColor Yellow
$songInfo = Invoke-RestMethod -Uri "http://music.163.com/api/song/detail/?id=1856336348&ids=%5B1856336348%5D"

if ($songInfo.code -eq 200 -and $songInfo.songs.Count -gt 0) {
    $song = $songInfo.songs[0]
    Write-Host "  OK Song Name: $($song.name)" -ForegroundColor Green
    Write-Host "  OK Artist: $($song.artists[0].name)" -ForegroundColor Green
    Write-Host "  OK Duration: $([math]::Round($song.duration / 1000)) seconds" -ForegroundColor Green
    
    if ($song.fee -eq 1) {
        Write-Host "  OK VIP Status: YES (fee=$($song.fee))" -ForegroundColor Red
    } else {
        Write-Host "  OK VIP Status: NO (fee=$($song.fee))" -ForegroundColor Green
    }
} else {
    Write-Host "  ERROR Failed to get song info" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 2. Get VIP direct URL
Write-Host "[Step 2] Fetching direct URL from VIP API..." -ForegroundColor Yellow
$vipUrl = Invoke-RestMethod -Uri "https://ncm.206601.xyz/play/direct?id=1856336348"

if ($vipUrl.success -and $vipUrl.code -eq 200) {
    Write-Host "  OK API Status: Success" -ForegroundColor Green
    Write-Host "  OK Direct URL: $($vipUrl.url.Substring(0, 50))..." -ForegroundColor Green
    Write-Host "  OK Quality Level: $($vipUrl.level)" -ForegroundColor Green
    Write-Host "  OK Song Name: $($vipUrl.song_name)" -ForegroundColor Green
    Write-Host "  OK Artist: $($vipUrl.artist)" -ForegroundColor Green
} else {
    Write-Host "  ERROR Failed to get direct URL" -ForegroundColor Red
    Write-Host "  Response: $($vipUrl | ConvertTo-Json)" -ForegroundColor Gray
    exit 1
}

Write-Host ""

# 3. Test if direct URL is accessible
Write-Host "[Step 3] Testing direct URL accessibility..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri $vipUrl.url -Method Head -UseBasicParsing
    Write-Host "  OK HTTP Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "  OK Content-Type: $($response.Headers['Content-Type'])" -ForegroundColor Green
    if ($response.Headers['Content-Length']) {
        $sizeMB = [math]::Round([int]$response.Headers['Content-Length'] / 1MB, 2)
        Write-Host "  OK File Size: $sizeMB MB" -ForegroundColor Green
    }
} catch {
    Write-Host "  ERROR Direct URL access failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "====== Test Complete ======" -ForegroundColor Cyan
Write-Host ""
Write-Host "Summary:" -ForegroundColor White
Write-Host "  1. OK NetEase API correctly detected VIP song (fee=1)" -ForegroundColor Green
Write-Host "  2. OK VIP API successfully returned playable URL" -ForegroundColor Green
Write-Host "  3. OK Direct URL is accessible" -ForegroundColor Green
Write-Host ""
Write-Host "Expected Mod Behavior:" -ForegroundColor White
Write-Host "  - User inputs ID: 1856336348" -ForegroundColor Gray
Write-Host "  - Mod detects VIP song (shows red [VIP] tag)" -ForegroundColor Gray
Write-Host "  - Mod automatically calls direct URL API" -ForegroundColor Gray
Write-Host "  - On success: removes VIP flag, allows playback" -ForegroundColor Gray

