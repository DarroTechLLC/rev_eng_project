/**
 * WebAuthn client-side operations
 * This file contains functions for registering and authenticating with biometric credentials
 */

// Check if WebAuthn is supported by the browser
function isWebAuthnSupported() {
    return window.PublicKeyCredential !== undefined;
}

// Check if platform authenticator (like TouchID, FaceID, Windows Hello) is available
async function isPlatformAuthenticatorAvailable() {
    if (!isWebAuthnSupported()) {
        return false;
    }

    return await PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable();
}

// Convert base64 string to ArrayBuffer
function base64ToArrayBuffer(base64) {
    // Convert base64url → base64
    base64 = base64.replace(/-/g, '+').replace(/_/g, '/');

    // Add padding if missing
    while (base64.length % 4 !== 0) {
        base64 += '=';
    }

    const binaryString = window.atob(base64);
    const len = binaryString.length;
    const bytes = new Uint8Array(len);
    for (let i = 0; i < len; i++) {
        bytes[i] = binaryString.charCodeAt(i);
    }
    return bytes.buffer;
}

// Convert ArrayBuffer to base64 string
function arrayBufferToBase64(buffer) {
    const bytes = new Uint8Array(buffer);
    let binary = '';
    for (let i = 0; i < bytes.byteLength; i++) {
        binary += String.fromCharCode(bytes[i]);
    }
    return window.btoa(binary);
}

// Prepare credential options for WebAuthn API
function preparePublicKeyOptions(options) {
    // Convert challenge from base64 to ArrayBuffer
    options.challenge = base64ToArrayBuffer(options.challenge);

    // If user is present, convert user.id from base64 to ArrayBuffer
    if (options.user && options.user.id) {
        options.user.id = base64ToArrayBuffer(options.user.id);
    }

    // If allowCredentials is present, convert id from base64 to ArrayBuffer
    if (options.allowCredentials) {
        options.allowCredentials = options.allowCredentials.map(credential => {
            return {
                ...credential,
                id: base64ToArrayBuffer(credential.id)
            };
        });
    }

    return options;
}

// Register a new biometric credential
async function registerBiometric() {
    try {
        // Check if WebAuthn is supported
        if (!isWebAuthnSupported()) {
            console.error('WebAuthn is not supported by this browser');
            return { error: 'WebAuthn is not supported by this browser' };
        }

        // Check if platform authenticator is available
        const available = await isPlatformAuthenticatorAvailable();
        if (!available) {
            console.error('Platform authenticator is not available');
            return { error: 'Platform authenticator is not available' };
        }

        console.log('Registering new biometric credential');

        // Get registration options from server
        let response;
        try {
            response = await fetch('/webauthn/register/options', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                }
            });
        } catch (fetchError) {
            console.error('Network error when fetching registration options:', fetchError);
            return { error: 'Network error: ' + fetchError.message };
        }

        if (!response.ok) {
            let errorMessage = 'Failed to get registration options';
            try {
                // Check if the response is JSON
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    const error = await response.json();
                    errorMessage = error.error || errorMessage;
                } else {
                    // If not JSON, get the text
                    const text = await response.text();
                    console.error('Non-JSON response:', text);
                    errorMessage += ' (Server returned non-JSON response)';
                }
            } catch (parseError) {
                console.error('Error parsing error response:', parseError);
                errorMessage += ' (Error parsing response)';
            }
            console.error(errorMessage);
            return { error: errorMessage };
        }

        let data;
        try {
            data = await response.json();
        } catch (jsonError) {
            console.error('Error parsing JSON response:', jsonError);
            return { error: 'Error parsing server response: ' + jsonError.message };
        }

        const challengeId = data.challengeId;
        if (!challengeId || !data.options) {
            console.error('Invalid response format:', data);
            return { error: 'Invalid server response format' };
        }

        console.log('Got registration options, preparing WebAuthn request');

        // Prepare options for WebAuthn API
        const publicKeyOptions = preparePublicKeyOptions(data.options);

        // Create credential
        let credential;
        try {
            credential = await navigator.credentials.create({
                publicKey: publicKeyOptions
            });
        } catch (credentialError) {
            console.error('Error creating credential with authenticator:', credentialError);
            return { error: 'Error creating credential: ' + credentialError.message };
        }

        console.log('Created credential with authenticator, sending to server for verification');

        // Prepare credential for server
        const credentialData = {
            id: credential.id,
            type: credential.type,
            rawId: arrayBufferToBase64(credential.rawId),
            response: {
                clientDataJSON: arrayBufferToBase64(credential.response.clientDataJSON),
                attestationObject: arrayBufferToBase64(credential.response.attestationObject)
            }
        };

        // Get public key if available
        let publicKey;
        try {
            publicKey = credential.response.getPublicKey ? 
                arrayBufferToBase64(credential.response.getPublicKey()) : 
                arrayBufferToBase64(new ArrayBuffer(0));
        } catch (publicKeyError) {
            console.error('Error getting public key:', publicKeyError);
            publicKey = arrayBufferToBase64(new ArrayBuffer(0));
        }

        // Send credential to server for verification
        let verifyResponse;
        try {
            verifyResponse = await fetch('/webauthn/register/verify', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({
                    challengeId: challengeId,
                    credential: {
                        id: credential.id,
                        publicKey: publicKey
                    }
                })
            });
        } catch (verifyFetchError) {
            console.error('Network error when verifying registration:', verifyFetchError);
            return { error: 'Network error during verification: ' + verifyFetchError.message };
        }

        if (!verifyResponse.ok) {
            let errorMessage = 'Failed to verify registration';
            try {
                // Check if the response is JSON
                const contentType = verifyResponse.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    const error = await verifyResponse.json();
                    errorMessage = error.error || errorMessage;
                } else {
                    // If not JSON, get the text
                    const text = await verifyResponse.text();
                    console.error('Non-JSON verification response:', text);
                    errorMessage += ' (Server returned non-JSON response)';
                }
            } catch (parseError) {
                console.error('Error parsing verification error response:', parseError);
                errorMessage += ' (Error parsing response)';
            }
            console.error(errorMessage);
            return { error: errorMessage };
        }

        let verifyData;
        try {
            verifyData = await verifyResponse.json();
        } catch (jsonError) {
            console.error('Error parsing verification JSON response:', jsonError);
            return { error: 'Error parsing verification response: ' + jsonError.message };
        }

        console.log('Registration successful');
        return verifyData;
    } catch (error) {
        console.error('Error registering biometric:', error);
        return { error: error.message || 'Error registering biometric' };
    }
}

// Authenticate with a biometric credential automatically (without username)
async function authenticateWithBiometricAuto() {
    try {
        // Check if WebAuthn is supported
        if (!isWebAuthnSupported()) {
            console.error('WebAuthn is not supported by this browser');
            return { error: 'WebAuthn is not supported by this browser' };
        }

        console.log('Authenticating with biometric automatically (no username required)');

        // Get authentication options from server
        let response;
        try {
            response = await fetch('/webauthn/authenticate/options/auto', {
                method: 'GET',
                headers: {
                    'Accept': 'application/json'
                }
            });
        } catch (fetchError) {
            console.error('Network error when fetching authentication options:', fetchError);
            return { error: 'Network error: ' + fetchError.message };
        }

        if (!response.ok) {
            let errorMessage = 'Failed to get authentication options';
            try {
                // Check if the response is JSON
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    const error = await response.json();
                    errorMessage = error.error || errorMessage;

                    // Extract additional error information if available
                    if (error.errorCode) {
                        return { 
                            error: errorMessage, 
                            errorCode: error.errorCode, 
                            message: error.message 
                        };
                    }
                } else {
                    // If not JSON, get the text
                    const text = await response.text();
                    console.error('Non-JSON response:', text);
                    errorMessage += ' (Server returned non-JSON response)';
                }
            } catch (parseError) {
                console.error('Error parsing error response:', parseError);
                errorMessage += ' (Error parsing response)';
            }
            console.error(errorMessage);
            return { error: errorMessage };
        }

        let data;
        try {
            data = await response.json();
        } catch (jsonError) {
            console.error('Error parsing JSON response:', jsonError);
            return { error: 'Error parsing server response: ' + jsonError.message };
        }

        const challengeId = data.challengeId;
        if (!challengeId || !data.options) {
            console.error('Invalid response format:', data);
            return { error: 'Invalid server response format' };
        }

        console.log('Got authentication options, preparing WebAuthn request');

        // Prepare options for WebAuthn API
        const publicKeyOptions = preparePublicKeyOptions(data.options);

        // Get credential
        let credential;
        try {
            credential = await navigator.credentials.get({
                publicKey: publicKeyOptions
            });
        } catch (credentialError) {
            console.error('Error getting credential from authenticator:', credentialError);
            return { error: 'Error accessing authenticator: ' + credentialError.message };
        }

        console.log('Got credential from authenticator, sending to server for verification');

        // Prepare credential for server
        const credentialData = {
            id: credential.id,
            type: credential.type,
            rawId: arrayBufferToBase64(credential.rawId),
            response: {
                clientDataJSON: arrayBufferToBase64(credential.response.clientDataJSON),
                authenticatorData: arrayBufferToBase64(credential.response.authenticatorData),
                signature: arrayBufferToBase64(credential.response.signature),
                userHandle: credential.response.userHandle ? arrayBufferToBase64(credential.response.userHandle) : null
            }
        };

        // Send credential to server for verification
        let verifyResponse;
        try {
            verifyResponse = await fetch('/webauthn/authenticate/verify', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({
                    challengeId,
                    credential: {
                        id: credential.id
                    }
                })
            });
        } catch (verifyFetchError) {
            console.error('Network error when verifying authentication:', verifyFetchError);
            return { error: 'Network error during verification: ' + verifyFetchError.message };
        }

        if (!verifyResponse.ok) {
            let errorMessage = 'Failed to verify authentication';
            try {
                // Check if the response is JSON
                const contentType = verifyResponse.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    const error = await verifyResponse.json();
                    errorMessage = error.error || errorMessage;
                } else {
                    // If not JSON, get the text
                    const text = await verifyResponse.text();
                    console.error('Non-JSON verification response:', text);
                    errorMessage += ' (Server returned non-JSON response)';
                }
            } catch (parseError) {
                console.error('Error parsing verification error response:', parseError);
                errorMessage += ' (Error parsing response)';
            }
            console.error(errorMessage);
            return { error: errorMessage };
        }

        let verifyData;
        try {
            verifyData = await verifyResponse.json();
        } catch (jsonError) {
            console.error('Error parsing verification JSON response:', jsonError);
            return { error: 'Error parsing verification response: ' + jsonError.message };
        }

        console.log('Authentication successful');
        return verifyData;
    } catch (error) {
        console.error('Error authenticating with biometric:', error);
        return { error: error.message || 'Error authenticating with biometric' };
    }
}

// Authenticate with a biometric credential (with username)
async function authenticateWithBiometric(username) {
    try {
        // Check if WebAuthn is supported
        if (!isWebAuthnSupported()) {
            console.error('WebAuthn is not supported by this browser');
            return { error: 'WebAuthn is not supported by this browser' };
        }

        console.log('Authenticating with biometric for username:', username);

        // Get authentication options from server
        let response;
        try {
            response = await fetch('/webauthn/authenticate/options', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({ username })
            });
        } catch (fetchError) {
            console.error('Network error when fetching authentication options:', fetchError);
            return { error: 'Network error: ' + fetchError.message };
        }

        if (!response.ok) {
            let errorMessage = 'Failed to get authentication options';
            try {
                // Check if the response is JSON
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    const error = await response.json();
                    errorMessage = error.error || errorMessage;

                    // Extract additional error information if available
                    if (error.errorCode) {
                        return { 
                            error: errorMessage, 
                            errorCode: error.errorCode, 
                            message: error.message 
                        };
                    }
                } else {
                    // If not JSON, get the text
                    const text = await response.text();
                    console.error('Non-JSON response:', text);
                    errorMessage += ' (Server returned non-JSON response)';
                }
            } catch (parseError) {
                console.error('Error parsing error response:', parseError);
                errorMessage += ' (Error parsing response)';
            }
            console.error(errorMessage);
            return { error: errorMessage };
        }

        let data;
        try {
            data = await response.json();
        } catch (jsonError) {
            console.error('Error parsing JSON response:', jsonError);
            return { error: 'Error parsing server response: ' + jsonError.message };
        }

        const challengeId = data.challengeId;
        if (!challengeId || !data.options) {
            console.error('Invalid response format:', data);
            return { error: 'Invalid server response format' };
        }

        console.log('Got authentication options, preparing WebAuthn request');

        // Prepare options for WebAuthn API
        const publicKeyOptions = preparePublicKeyOptions(data.options);

        // Get credential
        let credential;
        try {
            credential = await navigator.credentials.get({
                publicKey: publicKeyOptions
            });
        } catch (credentialError) {
            console.error('Error getting credential from authenticator:', credentialError);
            return { error: 'Error accessing authenticator: ' + credentialError.message };
        }

        console.log('Got credential from authenticator, sending to server for verification');

        // Prepare credential for server
        const credentialData = {
            id: credential.id,
            type: credential.type,
            rawId: arrayBufferToBase64(credential.rawId),
            response: {
                clientDataJSON: arrayBufferToBase64(credential.response.clientDataJSON),
                authenticatorData: arrayBufferToBase64(credential.response.authenticatorData),
                signature: arrayBufferToBase64(credential.response.signature),
                userHandle: credential.response.userHandle ? arrayBufferToBase64(credential.response.userHandle) : null
            }
        };

        // Send credential to server for verification
        let verifyResponse;
        try {
            verifyResponse = await fetch('/webauthn/authenticate/verify', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({
                    username,
                    challengeId,
                    credential: {
                        id: credential.id
                    }
                })
            });
        } catch (verifyFetchError) {
            console.error('Network error when verifying authentication:', verifyFetchError);
            return { error: 'Network error during verification: ' + verifyFetchError.message };
        }

        if (!verifyResponse.ok) {
            let errorMessage = 'Failed to verify authentication';
            try {
                // Check if the response is JSON
                const contentType = verifyResponse.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    const error = await verifyResponse.json();
                    errorMessage = error.error || errorMessage;
                } else {
                    // If not JSON, get the text
                    const text = await verifyResponse.text();
                    console.error('Non-JSON verification response:', text);
                    errorMessage += ' (Server returned non-JSON response)';
                }
            } catch (parseError) {
                console.error('Error parsing verification error response:', parseError);
                errorMessage += ' (Error parsing response)';
            }
            console.error(errorMessage);
            return { error: errorMessage };
        }

        let verifyData;
        try {
            verifyData = await verifyResponse.json();
        } catch (jsonError) {
            console.error('Error parsing verification JSON response:', jsonError);
            return { error: 'Error parsing verification response: ' + jsonError.message };
        }

        console.log('Authentication successful');
        return verifyData;
    } catch (error) {
        console.error('Error authenticating with biometric:', error);
        return { error: error.message || 'Error authenticating with biometric' };
    }
}

// Check if biometric authentication is available for the current user
async function isBiometricAvailable() {
    try {
        // Check if WebAuthn is supported
        if (!isWebAuthnSupported()) {
            console.log('WebAuthn is not supported by this browser');
            return false;
        }

        // Check if platform authenticator is available
        const available = await isPlatformAuthenticatorAvailable();
        if (!available) {
            console.log('Platform authenticator is not available on this device');
            return false;
        }

        console.log('Checking if user has registered biometric credentials');

        // Check if the user has registered credentials
        let response;
        try {
            response = await fetch('/webauthn/available', {
                headers: {
                    'Accept': 'application/json'
                }
            });
        } catch (fetchError) {
            console.error('Network error when checking biometric availability:', fetchError);
            return false;
        }

        if (!response.ok) {
            console.log('Server returned non-success status when checking biometric availability:', response.status);
            try {
                // Check if the response is JSON
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    const error = await response.json();
                    console.error('Error from server:', error);
                } else {
                    // If not JSON, get the text
                    const text = await response.text();
                    console.error('Non-JSON response from server:', text);
                }
            } catch (parseError) {
                console.error('Error parsing error response:', parseError);
            }
            return false;
        }

        let data;
        try {
            data = await response.json();
        } catch (jsonError) {
            console.error('Error parsing JSON response when checking biometric availability:', jsonError);
            // Try to get the raw text to see what was returned
            try {
                const text = await response.text();
                console.error('Raw response that could not be parsed as JSON:', text);
            } catch (textError) {
                console.error('Could not get response text:', textError);
            }
            return false;
        }

        console.log('Biometric availability check result:', data.available);
        return data.available;
    } catch (error) {
        console.error('Error checking biometric availability:', error);
        return false;
    }
}

// Initialize biometric authentication
async function initBiometricAuth() {
    // Check if biometric authentication is supported
    const supported = isWebAuthnSupported();
    if (!supported) {
        console.log('WebAuthn is not supported by this browser');
        return;
    }

    // Check if platform authenticator is available
    const available = await isPlatformAuthenticatorAvailable();
    if (!available) {
        console.log('Platform authenticator is not available');
        return;
    }

    // Show biometric login button if available
    const biometricButton = document.getElementById('biometric-login-button');
    if (biometricButton) {
        biometricButton.style.display = 'block';
    }
}

// Document ready function
document.addEventListener('DOMContentLoaded', function() {
    // Initialize biometric authentication
    initBiometricAuth();

    // Add event listener for biometric login button
    const biometricLoginButton = document.getElementById('biometric-login-button');
    if (biometricLoginButton) {
        biometricLoginButton.addEventListener('click', async function() {
            // Use the automatic biometric authentication that doesn't require username
            const result = await authenticateWithBiometricAuto();

            if (result.error) {
                if (result.errorCode === 'NO_CREDENTIALS') {
                    // Show a more user-friendly message with guidance
                    const message = result.message || 'You need to register your biometric credentials first. Please log in with your password and go to your account settings to register biometrics.';

                    // Create a nicer looking alert using Bootstrap if available
                    if (typeof $ !== 'undefined') {
                        // Remove any existing alerts
                        $('.biometric-alert').remove();

                        // Create a new alert
                        const alertHtml = `
                            <div class="biometric-alert alert alert-warning alert-dismissible fade show mt-3" role="alert">
                                <strong>Biometric Login Not Set Up</strong><br>
                                ${message}
                                <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                                    <span aria-hidden="true">&times;</span>
                                </button>
                            </div>
                        `;

                        // Insert the alert before the login button
                        $(biometricLoginButton).before(alertHtml);
                    } else {
                        // Fallback to regular alert if jQuery is not available
                        alert('Biometric Login Not Set Up: ' + message);
                    }
                } else {
                    // Show generic error for other types of errors
                    alert('Biometric authentication failed: ' + result.error);
                }
            } else if (result.success) {
                window.location.href = '/';
            }
        });
    }

    // Add event listener for biometric registration button
    const biometricRegisterButton = document.getElementById('biometric-register-button');
    if (biometricRegisterButton) {
        biometricRegisterButton.addEventListener('click', async function() {
            const result = await registerBiometric();

            if (result.error) {
                alert('Biometric registration failed: ' + result.error);
            } else if (result.success) {
                alert('Biometric registration successful! You can now use biometric authentication to log in.');
            }
        });
    }
});
