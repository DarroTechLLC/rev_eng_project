// Weekly report PDF download functionality
// Load the weekly report fix script
function loadFixScript() {
    const script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = '/js/weekly-report-fix.js';
    document.head.appendChild(script);
    console.log('Weekly report fix script loaded'); // Debug log
}

// Load the fix script
loadFixScript();

document.addEventListener('DOMContentLoaded', function() {
    console.log('Weekly report script loaded'); // Debug log

    const downloadPdfBtn = document.getElementById('downloadPdfBtn');
    if (downloadPdfBtn) {
        console.log('Download button found'); // Debug log
        downloadPdfBtn.addEventListener('click', handlePdfDownload);
    } else {
        console.error('Download button not found'); // Debug log
    }
});

function handlePdfDownload(e) {
    e.preventDefault();
    console.log('Download button clicked'); // Debug log

    // Get the current date from the date input
    const reportDate = document.getElementById('reportDate')?.value;
    console.log('Report date:', reportDate); // Debug log

    // Build the URL with parameters
    let url = '/align/weekly-report/pdf';
    if (reportDate) {
        url += `?date=${reportDate}`;
    }
    console.log('Download URL:', url); // Debug log

    // Show loading state
    const icon = this.querySelector('i');
    const originalIconClass = icon.className;
    icon.className = 'fas fa-spinner fa-spin fa-sm text-white-50';

    // Download the PDF
    fetch(url)
        .then(response => {
            console.log('Response status:', response.status); // Debug log
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.blob();
        })
        .then(blob => {
            console.log('PDF blob received'); // Debug log

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
            console.error('Error:', error); // Debug log
            showAlert('error', 'Failed to download PDF. Please try again.');
        })
        .finally(() => {
            // Restore the original icon
            icon.className = originalIconClass;
        });
}

function showAlert(type, message) {
    console.log('Showing alert:', type, message); // Debug log

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
    } else {
        console.error('Alert elements not found:', alertId, messageId); // Debug log
    }
} 
