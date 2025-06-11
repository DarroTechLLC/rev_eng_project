// Weekly report PDF download functionality
document.addEventListener('DOMContentLoaded', function() {
    const downloadPdfBtn = document.getElementById('downloadPdfBtn');
    
    if (downloadPdfBtn) {
        downloadPdfBtn.addEventListener('click', handlePdfDownload);
    }
});

function handlePdfDownload(e) {
    e.preventDefault();
    
    // Get the current date from the date input
    const reportDate = document.getElementById('reportDate')?.value;
    
    // Build the URL with parameters
    let url = '/align/weekly-report/pdf';
    if (reportDate) {
        url += `?date=${reportDate}`;
    }
    
    // Show loading state
    const icon = this.querySelector('i');
    const originalIconClass = icon.className;
    icon.className = 'fas fa-spinner fa-spin fa-sm text-white-50';
    
    // Download the PDF
    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.blob();
        })
        .then(blob => {
            // Create a URL for the blob
            const url = window.URL.createObjectURL(blob);
            
            // Create a temporary link and click it
            const a = document.createElement('a');
            a.href = url;
            a.download = `weekly-report-${reportDate || 'current'}.pdf`;
            document.body.appendChild(a);
            a.click();
            
            // Clean up
            window.URL.revokeObjectURL(url);
            a.remove();
            
            // Show success message
            showAlert('info', 'PDF downloaded successfully!');
        })
        .catch(error => {
            console.error('Error:', error);
            showAlert('error', 'Failed to download PDF. Please try again.');
        })
        .finally(() => {
            // Restore the original icon
            icon.className = originalIconClass;
        });
}

function showAlert(type, message) {
    const alertId = type === 'error' ? 'errorAlert' : 'infoAlert';
    const messageId = type === 'error' ? 'errorMessage' : 'infoMessage';
    
    const alertElement = document.getElementById(alertId);
    const messageElement = document.getElementById(messageId);
    
    if (alertElement && messageElement) {
        messageElement.textContent = message;
        alertElement.classList.remove('d-none');
        
        // Auto-hide after 5 seconds
        setTimeout(() => {
            alertElement.classList.add('d-none');
        }, 5000);
    }
} 