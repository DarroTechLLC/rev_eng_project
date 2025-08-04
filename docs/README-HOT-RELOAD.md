# Hot Reload Setup

## Backend Hot Reload

The backend hot reload is configured with Spring DevTools and should work automatically when you run the application with:

```bash
./gradlew bootRun
```

If you're using an IDE like IntelliJ IDEA:

1. Enable "Build project automatically":
   - Go to Settings/Preferences (Ctrl+Alt+S)
   - Build, Execution, Deployment → Compiler
   - Check "Build project automatically"

2. Enable "Allow auto-make to start even if developed application is currently running":
   - Press Ctrl+Shift+A (or Command+Shift+A on macOS)
   - Search for "Registry"
   - Enable `compiler.automake.allow.when.app.running`

## Frontend Hot Reload

### Using Browser-Sync (Easy Setup)

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the hot reload server:
   ```bash
   npm start
   ```

4. Open your browser at http://localhost:3000

This will proxy your Spring Boot application running on port 8080 and automatically refresh the browser when HTML, CSS, or JS files change.

## Troubleshooting

### Database Connection Issues

Make sure your MySQL database is running and the credentials in the `.env` file or `application-dev.properties` are correct.

### Hot Reload Not Working

1. Verify that spring-boot-devtools is in your dependencies
2. Check that you're accessing the application through the BrowserSync URL (for frontend)
3. Try restarting both servers
4. Clear browser cache

### Manual Setup Alternative

If you prefer to set up environment variables manually rather than using the `.env` file:

```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/rae_latest
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=miller
./gradlew bootRun
```
