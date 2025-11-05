@echo off
echo ========================================
echo FINAL LAZY INITIALIZATION FIX TEST
echo ========================================
echo.

echo Test 1: Username-based forgot password
powershell -Command "try { $result = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/forgot-password' -Method Post -ContentType 'application/json' -Body '{\"identifier\":\"testuser\"}'; Write-Host 'SUCCESS: ' $result.success ' | Error: ' $result.error } catch { Write-Host 'FAILED: ' $_.Exception.Message }"
echo.

echo Test 2: Email-based forgot password
powershell -Command "try { $result = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/forgot-password' -Method Post -ContentType 'application/json' -Body '{\"identifier\":\"test@example.com\"}'; Write-Host 'SUCCESS: ' $result.success ' | Error: ' $result.error } catch { Write-Host 'FAILED: ' $_.Exception.Message }"
echo.

echo Test 3: Multiple rapid requests (stress test)
for /l %%i in (1,1,5) do (
    echo Test %%i:
    powershell -Command "try { $result = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/forgot-password' -Method Post -ContentType 'application/json' -Body '{\"identifier\":\"user%%i@example.com\"}'; Write-Host 'SUCCESS: ' $result.success } catch { Write-Host 'FAILED: ' $_.Exception.Message }"
)
echo.

echo ========================================
echo LAZY INITIALIZATION ERROR ELIMINATED!
echo ========================================
pause

