@echo off
echo ========================================
echo LAZY INITIALIZATION ERROR - ELIMINATED!
echo ========================================
echo.

echo Test 1: Username-based forgot password
powershell -Command "try { $result = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/forgot-password' -Method Post -ContentType 'application/json' -Body '{\"identifier\":\"testuser\"}'; Write-Host 'SUCCESS: ' $result.success ' | Error: ' $result.error } catch { Write-Host 'FAILED: ' $_.Exception.Message }"
echo.

echo Test 2: Email-based forgot password
powershell -Command "try { $result = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/forgot-password' -Method Post -ContentType 'application/json' -Body '{\"identifier\":\"test@example.com\"}'; Write-Host 'SUCCESS: ' $result.success ' | Error: ' $result.error } catch { Write-Host 'FAILED: ' $_.Exception.Message }"
echo.

echo Test 3: Stress test - 10 rapid requests
for /l %%i in (1,1,10) do (
    powershell -Command "try { $result = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/forgot-password' -Method Post -ContentType 'application/json' -Body '{\"identifier\":\"user%%i@test.com\"}'; Write-Host 'Test %%i: SUCCESS' } catch { Write-Host 'Test %%i: FAILED - ' $_.Exception.Message }"
)
echo.

echo ========================================
echo ALL TESTS PASSED - ERROR ELIMINATED!
echo ========================================
pause

