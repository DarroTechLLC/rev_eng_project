import React, { createContext, useContext, useState, useEffect } from 'react';
import { useAuth } from './AuthContext';

interface Company {
    companyId: string;
    companyName: string;
    displayName: string;
    logoUrl: string | null;
}

interface CompanyContextType {
    companies: Company[];
    selectedCompany: Company | null;
    setSelectedCompany: (company: Company | null) => void;
    clearSelectedCompany: () => void;
    getCompanyById: (companyId: string) => Company | undefined;
    getCompanyByName: (companyName: string) => Company | undefined;
}

const CompanyContext = createContext<CompanyContextType | undefined>(undefined);

export const CompanyProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [companies, setCompanies] = useState<Company[]>([]);
    const [selectedCompany, setSelectedCompany] = useState<Company | null>(() => {
        const saved = localStorage.getItem('selectedCompany');
        return saved ? JSON.parse(saved) : null;
    });

    const { user } = useAuth();

    useEffect(() => {
        if (user) {
            fetchCompanies();
        } else {
            setCompanies([]);
            clearSelectedCompany();
        }
    }, [user]);

    const fetchCompanies = async () => {
        try {
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

            // If we have a selected company, verify it's still valid
            if (selectedCompany) {
                const stillExists = data.some(c => c.companyId === selectedCompany.companyId);
                if (!stillExists) {
                    clearSelectedCompany();
                }
            }
        } catch (err) {
            console.error('Error fetching companies:', err);
            setCompanies([]);
        }
    };

    useEffect(() => {
        if (selectedCompany) {
            localStorage.setItem('selectedCompany', JSON.stringify(selectedCompany));
        } else {
            localStorage.removeItem('selectedCompany');
        }
    }, [selectedCompany]);

    const clearSelectedCompany = () => {
        setSelectedCompany(null);
        localStorage.removeItem('selectedCompany');
    };

    const getCompanyById = (companyId: string): Company | undefined => {
        return companies.find(c => c.companyId === companyId);
    };

    const getCompanyByName = (companyName: string): Company | undefined => {
        return companies.find(c => c.companyName.toLowerCase() === companyName.toLowerCase());
    };

    return (
        <CompanyContext.Provider
            value={{
                companies,
                selectedCompany,
                setSelectedCompany,
                clearSelectedCompany,
                getCompanyById,
                getCompanyByName,
            }}
        >
            {children}
        </CompanyContext.Provider>
    );
};

export const useCompany = () => {
    const context = useContext(CompanyContext);
    if (context === undefined) {
        throw new Error('useCompany must be used within a CompanyProvider');
    }
    return context;
};

export default CompanyProvider; 