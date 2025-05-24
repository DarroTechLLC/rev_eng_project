document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('createFarmForm');
    const submitBtn = document.getElementById('saveFarmBtn');
    const spinner = submitBtn.querySelector('.spinner-border');
    let isDirty = false;

    // Load temp sources when modal opens
    $('#createFarmModal').on('show.bs.modal', async function() {
        try {
            const response = await fetch('/api/admin/farms/temp-sources', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
                }
            });
            
            if (!response.ok) {
                throw new Error('Failed to fetch temperature sources');
            }

            const tempSources = await response.json();
            const select = document.getElementById('tempSourceId');
            select.innerHTML = '<option value="">- None -</option>';
            
            tempSources.forEach(source => {
                const option = document.createElement('option');
                option.value = source.id;
                option.textContent = source.name;
                select.appendChild(option);
            });
        } catch (error) {
            console.error('Error loading temperature sources:', error);
            toastr.error('Failed to load temperature sources');
        }
    });

    // Field validation
    async function validateField(field, value) {
        // Skip validation if display name is empty (since it's optional)
        if (field === 'displayName' && (!value || value.trim() === '')) {
            return { valid: true };
        }

        try {
            const endpoint = field === 'name' ? 'name' : 'display-name';
            const response = await fetch(`/api/admin/farms/validate/${endpoint}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
                },
                body: JSON.stringify({ 
                    [field === 'name' ? 'name' : 'displayName']: value 
                })
            });

            if (!response.ok) {
                throw new Error('Validation request failed');
            }

            return await response.json();
        } catch (error) {
            console.error('Validation error:', error);
            return { valid: false, error: 'Validation failed. Please try again.' };
        }
    }

    // Handle field validation
    document.querySelectorAll('[data-validate]').forEach(input => {
        let validationTimeout;

        input.addEventListener('blur', async () => {
            const field = input.dataset.validate;
            const value = input.value;

            // Skip validation if display name is empty (since it's optional)
            if (field === 'displayName' && (!value || value.trim() === '')) {
                input.classList.remove('is-invalid');
                input.nextElementSibling.nextElementSibling.textContent = '';
                return;
            }

            // Check display name length before making API call
            if (field === 'displayName' && value.length > 6) {
                input.classList.add('is-invalid');
                input.nextElementSibling.nextElementSibling.textContent = 'Display Name cannot be more than 6 characters';
                return;
            }

            const result = await validateField(field, value);
            
            if (!result.valid) {
                input.classList.add('is-invalid');
                input.nextElementSibling.nextElementSibling.textContent = result.error;
            } else {
                input.classList.remove('is-invalid');
                input.nextElementSibling.nextElementSibling.textContent = '';
            }
        });

        // Add input event listener for real-time validation
        input.addEventListener('input', () => {
            isDirty = true;
            const field = input.dataset.validate;
            const value = input.value;

            // Clear any existing validation timeout
            if (validationTimeout) {
                clearTimeout(validationTimeout);
            }

            // Check display name length in real-time
            if (field === 'displayName') {
                if (value.length > 6) {
                    input.classList.add('is-invalid');
                    input.nextElementSibling.nextElementSibling.textContent = 'Display Name cannot be more than 6 characters';
                } else {
                    input.classList.remove('is-invalid');
                    input.nextElementSibling.nextElementSibling.textContent = '';
                }
            } else {
                if (input.classList.contains('is-invalid')) {
                    input.classList.remove('is-invalid');
                    input.nextElementSibling.nextElementSibling.textContent = '';
                }
            }

            // Debounce validation for 300ms
            validationTimeout = setTimeout(async () => {
                if (field === 'displayName' && value.length > 6) {
                    return;
                }

                if (value && value.trim() !== '') {
                    const result = await validateField(field, value);
                    
                    if (!result.valid) {
                        input.classList.add('is-invalid');
                        input.nextElementSibling.nextElementSibling.textContent = result.error;
                    } else {
                        input.classList.remove('is-invalid');
                        input.nextElementSibling.nextElementSibling.textContent = '';
                    }
                }
            }, 300);
        });
    });

    // Track form changes
    form.addEventListener('change', () => isDirty = true);

    // Handle form submission
    submitBtn.addEventListener('click', async (e) => {
        e.preventDefault();
        
        if (!form.checkValidity()) {
            form.classList.add('was-validated');
            return;
        }

        // Check display name length before submitting
        const displayName = document.getElementById('displayName').value;
        if (displayName && displayName.length > 6) {
            document.getElementById('displayName').classList.add('is-invalid');
            document.getElementById('displayName').nextElementSibling.nextElementSibling.textContent = 'Display Name cannot be more than 6 characters';
            return;
        }

        // Clear any existing validation errors
        document.querySelectorAll('.is-invalid').forEach(el => {
            el.classList.remove('is-invalid');
            el.nextElementSibling.nextElementSibling.textContent = '';
        });

        spinner.classList.remove('d-none');
        submitBtn.disabled = true;

        try {
            const formData = {
                name: document.getElementById('farmName').value,
                displayName: document.getElementById('displayName').value || null,
                farmType: document.getElementById('farmType').value || null,
                tempSourceId: document.getElementById('tempSourceId').value || null,
                isTempSource: document.getElementById('isTempSource').checked
            };

            const response = await fetch('/api/admin/farms/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
                },
                body: JSON.stringify(formData)
            });

            const result = await response.json();

            if (result.success) {
                toastr.success('Farm created successfully');
                
                // Refresh farm list if the function exists
                if (typeof loadFarms === 'function') {
                    loadFarms();
                } else {
                    // If no loadFarms function, reload the page
                    window.location.reload();
                }
                
                // Close modal
                $('#createFarmModal').modal('hide');
                
                // Reset form
                form.reset();
                isDirty = false;
            } else {
                toastr.error(result.message || 'Error creating farm');
            }
        } catch (error) {
            console.error('Error creating farm:', error);
            toastr.error('Error creating farm. Please try again.');
        } finally {
            spinner.classList.add('d-none');
            submitBtn.disabled = false;
        }
    });

    // Handle modal close
    $('#createFarmModal').on('hide.bs.modal', function(e) {
        if (isDirty) {
            const confirm = window.confirm('You have unsaved changes. Are you sure you want to close?');
            if (!confirm) {
                e.preventDefault();
            }
        }
    });

    // Reset form when modal is closed
    $('#createFarmModal').on('hidden.bs.modal', function() {
        form.reset();
        form.classList.remove('was-validated');
        isDirty = false;
        document.querySelectorAll('.is-invalid').forEach(el => {
            el.classList.remove('is-invalid');
            el.nextElementSibling.nextElementSibling.textContent = '';
        });
    });
}); 