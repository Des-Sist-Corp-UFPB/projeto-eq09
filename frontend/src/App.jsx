import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Profile from './pages/Profile';
import Diario from './pages/Diario';

export default function App() {
  const [currentPage, setCurrentPage] = useState('login');
  const [usuario, setUsuario] = useState(null);
  const [isInitializing, setIsInitializing] = useState(true);

  // Restores session from localStorage on startup
  useEffect(() => {
    const storedUser = localStorage.getItem('dscboxd_user');
    if (storedUser) {
      try {
        setUsuario(JSON.parse(storedUser));
        setCurrentPage('home');
      } catch (err) {
        localStorage.removeItem('dscboxd_user');
        setCurrentPage('login');
      }
    } else {
      setCurrentPage('login');
    }
    setIsInitializing(false);
  }, []);

  const handleLoginSuccess = (userData) => {
    setUsuario(userData);
    localStorage.setItem('dscboxd_user', JSON.stringify(userData));
    setCurrentPage('home');
  };

  const handleLogout = () => {
    setUsuario(null);
    localStorage.removeItem('dscboxd_user');
    setCurrentPage('login');
  };

  const navigateTo = (pageName) => {
    // If not logged in, force navigation to login or register
    if (!usuario && pageName !== 'register' && pageName !== 'login') {
      setCurrentPage('login');
    } else {
      setCurrentPage(pageName);
    }
  };

  // Prevent flash of login screen during startup initialization
  if (isInitializing) {
    return (
      <div style={{
        minHeight: '100vh',
        backgroundColor: 'var(--bg-primary)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        color: 'var(--text-secondary)'
      }}>
        Inicializando...
      </div>
    );
  }

  // Separate flow: If not authenticated, render Login/Register inside full screen wrapper
  if (!usuario) {
    return (
      <div className="auth-wrapper">
        {currentPage === 'register' ? (
          <Register onNavigate={navigateTo} />
        ) : (
          <Login onLoginSuccess={handleLoginSuccess} onNavigate={navigateTo} />
        )}
      </div>
    );
  }

  const renderPage = () => {
    switch (currentPage) {
      case 'home':
        return <Home usuario={usuario} />;
      case 'profile':
        return <Profile usuario={usuario} onNavigate={navigateTo} />;
      case 'diario':
        return <Diario usuario={usuario} />;
      case 'login':
        return <Login onLoginSuccess={handleLoginSuccess} onNavigate={navigateTo} />;
      case 'register':
        return <Register onNavigate={navigateTo} />;
      default:
        return <Home usuario={usuario} />;
    }
  };

  // Main flow for logged-in users: Navbar, Home (Catalog), and Footer
  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', backgroundColor: 'var(--bg-primary)' }}>
      {/* Top Navigation - Shows only the logo and the logout button */}
      <Navbar usuario={usuario} onLogout={handleLogout} onNavigate={navigateTo} />
      
      {/* Active Page view */}
      <div style={{ flexGrow: 1 }}>
        {renderPage()}
      </div>

      {/* Footer */}
      <footer style={{
        textAlign: 'center',
        padding: '30px 24px',
        borderTop: '1px solid rgba(255, 255, 255, 0.04)',
        background: 'var(--bg-secondary)',
        color: 'var(--text-muted)',
        fontSize: '14px',
        marginTop: '60px'
      }}>
        <p>&copy; {new Date().getFullYear()} DSCboxd. Desenvolvido para a disciplina Desenvolvimento de Sistemas Corporativos (DSC) - UFPB.</p>
      </footer>
    </div>
  );
}
