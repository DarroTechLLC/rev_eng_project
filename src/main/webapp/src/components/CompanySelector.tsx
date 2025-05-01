import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useCompany } from '../contexts/CompanyContext';

interface Company {
    companyId: string;
    companyName: string;
    displayName: string;
    logoUrl: string | null;
}

const CompanySelector: React.FC = () => {
    const [companies, setCompanies] = useState<Company[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    
    const navigate = useNavigate();
    const { user } = useAuth();
    const { selectedCompany, setSelectedCompany } = useCompany();

    useEffect(() => {
        fetchCompanies();
    }, [user]);

    const fetchCompanies = async () => {
        try {
            setLoading(true);
            const response = await fetch('/api/companies', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (!response.ok) {
                throw new Error('Failed to fetch companies');
            }

            const data = await response.json();
            setCompanies(data);
            
            // If no company is selected and we have companies, select the first one
            if (!selectedCompany && data.length > 0) {
                handleCompanySelect(data[0]);
            }
        } catch (err) {
            setError(err instanceof Error ? err.message : 'An error occurred');
            console.error('Error fetching companies:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleCompanySelect = async (company: Company) => {
        try {
            const response = await fetch(`/api/companies/select/${company.companyId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (!response.ok) {
                throw new Error('Failed to select company');
            }

            const data = await response.json();
            if (data.success) {
                setSelectedCompany(company);
                // Refresh the current page to update data for the new company
                navigate(0);
            }
        } catch (err) {
            console.error('Error selecting company:', err);
            setError(err instanceof Error ? err.message : 'Failed to select company');
        }
    };

    if (loading) {
        return <div className="animate-pulse">Loading companies...</div>;
    }

    if (error) {
        return <div className="text-red-500">Error: {error}</div>;
    }

    if (companies.length === 0) {
        return <div className="text-gray-500">No companies available</div>;
    }

    return (
        <div className="relative">
            <label htmlFor="company-select" className="sr-only">Select Company</label>
            <select
                id="company-select"
                className="block w-full px-4 py-2 pr-8 leading-tight bg-white border border-gray-300 rounded-lg appearance-none focus:outline-none focus:border-blue-500"
                value={selectedCompany?.companyId || ''}
                onChange={(e) => {
                    const company = companies.find(c => c.companyId === e.target.value);
                    if (company) {
                        handleCompanySelect(company);
                    }
                }}
            >
                {companies.map((company) => (
                    <option key={company.companyId} value={company.companyId}>
                        {company.displayName || company.companyName}
                    </option>
                ))}
            </select>
            <div className="absolute inset-y-0 right-0 flex items-center px-2 pointer-events-none">
                <svg className="w-4 h-4 text-gray-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
            </div>
        </div>
    );
};

export default CompanySelector; 