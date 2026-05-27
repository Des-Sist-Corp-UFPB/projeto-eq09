import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';

export default function App() {
  const [currentPage, setCurrentPage] = useState('home');
  const [usuario, setUsuario] = useState(null);

  // Restaura sessão anterior do localStorage
  useEffect(() => {
    const storedUser = localStorage.getItem('cineavalia_user');
    if (storedUser) {
      try {
        setUsuario(JSON.parse(storedUser));
      } catch (err) {
        localStorage.removeItem('cineavalia_user');
      }
    }
  }, []);

  const handleLoginSuccess = (userData) => {
    setUsuario(userData);
    localStorage.setItem('cineavalia_user', JSON.stringify(userData));
    setCurrentPage('home');
  };

  const handleLogout = () => {
    setUsuario(null);
    localStorage.removeItem('cineavalia_user');
    setCurrentPage('home');
  };

  const navigateTo = (pageName) => {
    setCurrentPage(pageName);
  };

  const renderPage = () => {
    switch (currentPage) {
      case 'home':
        return <Home usuario={usuario} />;
      case 'login':
        return <Login onLoginSuccess={handleLoginSuccess} onNavigate={navigateTo} />;
      case 'register':
        return <Register onNavigate={navigateTo} />;
      default:
        return <Home usuario={usuario} />;
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      {/* Top Navigation */}
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
        background: 'rgba(10, 11, 14, 0.6)',
        color: 'var(--text-muted)',
        fontSize: '14px',
        marginTop: '60px'
      }}>
        <p>&copy; {new Date().getFullYear()} CineAvalia. Desenvolvido para a disciplina Desenvolvimento de Sistemas Corporativos (DSC) - UFPB.</p>
      </footer>
    </div>
  );
}
