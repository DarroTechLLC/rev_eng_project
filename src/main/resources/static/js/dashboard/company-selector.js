/**
 * Company Selector Controller
 * Handles loading and selecting companies for the dashboard
 */
class CompanySelector {
    constructor() {
        this.companies = [];
        this.container = document.querySelector('.company-list');
        this.init();
    }

    async init() {
        try {
            console.log('🏢 Initializing company selector');
            await this.loadCompanies();
            this.renderCompanies();
        } catch (error) {
            console.error('❌ Error initializing company selector:', error);
            this.showError('Failed to load companies. Please try again later.');
        }
    }

    async loadCompanies() {
        try {
            console.log('📡 Loading companies from API');
            const response = await fetch('/api/companies');
            if (!response.ok) throw new Error('Failed to fetch companies');
            
            const data = await response.json();
            this.companies = data.companies || [];
            console.log(`✅ Loaded ${this.companies.length} companies`);
        } catch (error) {
            console.error('❌ Error loading companies:', error);
            throw error;
        }
    }

    renderCompanies() {
        console.log('🎨 Rendering company list');
        if (!this.companies.length) {
            this.container.innerHTML = '<p class="error">No companies available</p>';
            return;
        }

        const html = this.companies.map(company => `
            <div class="company-card" data-company-id="${company.id}">
                <h3>${company.name}</h3>
                <button onclick="companySelector.selectCompany('${company.id}')">
                    View Dashboard
                </button>
            </div>
        `).join('');

        this.container.innerHTML = html;
        console.log('✅ Company list rendered');
    }

    async selectCompany(companyId) {
        try {
            console.log(`🔍 Selecting company: ${companyId}`);
            
            // Set company in session
            const response = await fetch('/api/session/company', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    // Get CSRF token from meta tag
                    [document.querySelector('meta[name="_csrf_header"]').content]: 
                    document.querySelector('meta[name="_csrf"]').content
                },
                body: JSON.stringify({ companyId })
            });

            if (!response.ok) throw new Error('Failed to set company');

            // Redirect to dashboard
            console.log('➡️ Redirecting to dashboard');
            window.location.href = `/dashboard/${companyId}/daily-volume`;
        } catch (error) {
            console.error('❌ Error selecting company:', error);
            this.showError('Failed to select company. Please try again.');
        }
    }

    showError(message) {
        const errorDiv = document.createElement('div');
        errorDiv.className = 'error-message';
        errorDiv.textContent = message;
        this.container.prepend(errorDiv);
    }
}

// Initialize the company selector
const companySelector = new CompanySelector(); 