@echo off
echo Testing Forgot Password Service...
echo.

echo Test 1: Email-based forgot password
curl -X POST http://localhost:8080/api/auth/forgot-password -H "Content-Type: application/json" -d "{\"identifier\":\"test@example.com\"}"
echo.
echo.

echo Test 2: Username-based forgot password  
curl -X POST http://localhost:8080/api/auth/forgot-password -H "Content-Type: application/json" -d "{\"identifier\":\"testuser\"}"
echo.
echo.

echo Test 3: Empty input
curl -X POST http://localhost:8080/api/auth/forgot-password -H "Content-Type: application/json" -d "{\"identifier\":\"\"}"
echo.
echo.

echo Test 4: Invalid email
curl -X POST http://localhost:8080/api/auth/forgot-password -H "Content-Type: application/json" -d "{\"identifier\":\"invalid@nonexistent.com\"}"
echo.
echo.

echo All tests completed!
pause

