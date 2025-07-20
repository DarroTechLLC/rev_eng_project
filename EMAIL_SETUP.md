# Email Configuration Setup

## Issue: Authentication Failed

If you're seeing the following error:
```
jakarta.mail.AuthenticationFailedException: 535-5.7.8 Username and Password not accepted.
```

This means the application is unable to authenticate with the email server using the provided credentials.

## Solution

### For Gmail Accounts

1. **Set up an App Password** (recommended if you have 2-factor authentication enabled):
   - Go to your Google Account settings: https://myaccount.google.com/
   - Navigate to "Security" > "2-Step Verification"
   - Scroll down to "App passwords"
   - Select "Mail" as the app and "Other" as the device (name it "Spring Boot App")
   - Click "Generate"
   - Copy the 16-character password that appears

2. **Update your .env file**:
   ```
   EMAIL_PASSWORD=your16characterapppassword
   ```

   **Important**: Remove all spaces from the App Password when adding it to the .env file. 
   Google displays App Passwords with spaces for readability (e.g., "abcd efgh ijkl mnop"), 
   but you should remove all spaces when using it in your application (e.g., "abcdefghijklmnop").

3. **If you don't use 2-factor authentication**:
   - You might need to enable "Less secure app access" in your Google Account settings
   - However, Google is phasing this out, so using an App Password is recommended

### For Other Email Providers

1. Update the following properties in `application.properties`:
   ```
   spring.mail.host=your_smtp_server
   spring.mail.port=your_smtp_port
   spring.mail.username=your_email_address
   ```

2. Add your password to the `.env` file:
   ```
   EMAIL_PASSWORD=your_email_password
   ```

## Testing

After configuring your email settings:

1. Restart the application
2. Try the forgot password functionality to test if emails are being sent correctly

## Troubleshooting

- Verify that your email account doesn't have additional security restrictions
- Check if your email provider requires specific SMTP settings
- Ensure the application has network access to reach the email server
- Check application logs for detailed error messages
