# Test VIP Lyrics API Functionality
Write-Host "====== VIP Lyrics API Test ======" -ForegroundColor Cyan
Write-Host ""

$testSongId = 1856336348

# Test 1: Get VIP Lyrics
Write-Host "[Test 1] Fetching VIP lyrics..." -ForegroundColor Yellow
try {
    $vipLyric = Invoke-RestMethod -Uri "https://ncm.206601.xyz/play/vrc?id=$testSongId"
    
    Write-Host "  OK Song Name: $($vipLyric.songName)" -ForegroundColor Green
    Write-Host "  OK Artist: $($vipLyric.artist)" -ForegroundColor Green
    
    # Check if lyrics have JSON lines
    $lyricLines = $vipLyric.lyric -split "`n"
    $jsonLines = $lyricLines | Where-Object { $_ -match '^\{"t":\d+,"c":\[' }
    $lrcLines = $lyricLines | Where-Object { $_ -match '^\[\d{2}:\d{2}\.\d+\]' }
    
    Write-Host "  OK JSON lines found: $($jsonLines.Count)" -ForegroundColor Green
    Write-Host "  OK LRC lines found: $($lrcLines.Count)" -ForegroundColor Green
    
    if ($vipLyric.transLyric) {
        Write-Host "  OK Translation available: Yes" -ForegroundColor Green
    } else {
        Write-Host "  INFO Translation available: No" -ForegroundColor Gray
    }
} catch {
    Write-Host "  ERROR Failed to fetch VIP lyrics: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Test 2: Format Comparison
Write-Host "[Test 2] Format Comparison..." -ForegroundColor Yellow

Write-Host "  Original NetEase API format:" -ForegroundColor Cyan
Write-Host "    {" -ForegroundColor Gray
Write-Host "      `"code`": 200," -ForegroundColor Gray
Write-Host "      `"lrc`": {`"lyric`": `"[00:22.300]...`"}," -ForegroundColor Gray
Write-Host "      `"tlyric`": {`"lyric`": `"[00:22.300]...`"}" -ForegroundColor Gray
Write-Host "    }" -ForegroundColor Gray

Write-Host ""
Write-Host "  VIP API format:" -ForegroundColor Cyan
Write-Host "    {" -ForegroundColor Gray
Write-Host "      `"songName`": `"..`"," -ForegroundColor Gray
Write-Host "      `"lyric`": `"{...JSON...}\n[00:22.300]...`"," -ForegroundColor Gray
Write-Host "      `"transLyric`": `"[00:22.300]...`"" -ForegroundColor Gray
Write-Host "    }" -ForegroundColor Gray

Write-Host ""

# Test 3: Show conversion process
Write-Host "[Test 3] Lyric Cleaning Process..." -ForegroundColor Yellow

$sampleLyric = @"
{"t":0,"c":[{"tx":"Test"}]}
{"t":1000,"c":[{"tx":"Test2"}]}
[00:22.300]
[00:23.325]First line
[00:26.065]Second line
"@

Write-Host "  Before cleaning:" -ForegroundColor Cyan
$sampleLyric -split "`n" | ForEach-Object { Write-Host "    $_" -ForegroundColor Gray }

Write-Host ""
Write-Host "  After cleaning (JSON lines removed):" -ForegroundColor Cyan
$cleaned = ($sampleLyric -split "`n" | Where-Object { $_ -notmatch '^\{"t":\d+,"c":\[' }) -join "`n"
$cleaned -split "`n" | ForEach-Object { Write-Host "    $_" -ForegroundColor Gray }

Write-Host ""
Write-Host "====== Test Complete ======" -ForegroundColor Cyan
Write-Host ""

Write-Host "Mod Implementation Summary:" -ForegroundColor White
Write-Host "  1. VipLyric.java - Parses VIP API response" -ForegroundColor Green
Write-Host "  2. VipLyricConverter.java - Converts to standard format" -ForegroundColor Green
Write-Host "  3. WebApi.getVipLyric() - Fetches VIP lyrics" -ForegroundColor Green
Write-Host "  4. MusicListManage.tryGetVipDirectUrl() - Gets lyrics with URL" -ForegroundColor Green
Write-Host "  5. SongInfo.lyricJson - Stores converted lyrics" -ForegroundColor Green
Write-Host "  6. MusicToClientMessage - Transmits lyrics to client" -ForegroundColor Green
Write-Host ""

Write-Host "Expected Behavior:" -ForegroundColor White
Write-Host "  - When VIP song is detected:" -ForegroundColor Gray
Write-Host "    1. Get direct URL from https://ncm.206601.xyz/play/direct?id=XXX" -ForegroundColor Gray
Write-Host "    2. Get lyrics from https://ncm.206601.xyz/play/vrc?id=XXX" -ForegroundColor Gray
Write-Host "    3. Clean JSON lines from lyrics" -ForegroundColor Gray
Write-Host "    4. Convert to standard NetEase format" -ForegroundColor Gray
Write-Host "    5. Store in SongInfo.lyricJson" -ForegroundColor Gray
Write-Host "    6. Display lyrics during playback" -ForegroundColor Gray
