@echo off
echo ========================================
echo FINAL FORGOT PASSWORD SERVICE TEST
echo ========================================
echo.

echo Test 1: Username-based forgot password
powershell -Command "try { $result = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/forgot-password' -Method Post -ContentType 'application/json' -Body '{\"identifier\":\"testuser\"}'; Write-Host 'SUCCESS: ' $result.success 'Error: ' $result.error } catch { Write-Host 'ERROR: ' $_.Exception.Message }"
echo.

echo Test 2: Email-based forgot password
powershell -Command "try { $result = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/forgot-password' -Method Post -ContentType 'application/json' -Body '{\"identifier\":\"test@example.com\"}'; Write-Host 'SUCCESS: ' $result.success 'Error: ' $result.error } catch { Write-Host 'ERROR: ' $_.Exception.Message }"
echo.

echo Test 3: Empty input
powershell -Command "try { $result = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/forgot-password' -Method Post -ContentType 'application/json' -Body '{\"identifier\":\"\"}'; Write-Host 'SUCCESS: ' $result.success 'Error: ' $result.error } catch { Write-Host 'ERROR: ' $_.Exception.Message }"
echo.

echo Test 4: Invalid email
powershell -Command "try { $result = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/forgot-password' -Method Post -ContentType 'application/json' -Body '{\"identifier\":\"invalid@nonexistent.com\"}'; Write-Host 'SUCCESS: ' $result.success 'Error: ' $result.error } catch { Write-Host 'ERROR: ' $_.Exception.Message }"
echo.

echo Test 5: Invalid username
powershell -Command "try { $result = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/forgot-password' -Method Post -ContentType 'application/json' -Body '{\"identifier\":\"nonexistentuser\"}'; Write-Host 'SUCCESS: ' $result.success 'Error: ' $result.error } catch { Write-Host 'ERROR: ' $_.Exception.Message }"
echo.

echo ========================================
echo ALL TESTS COMPLETED!
echo ========================================
pause

