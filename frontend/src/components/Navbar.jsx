import React, { useState, useEffect } from 'react';
import { Film, LogOut, User, Calendar } from 'lucide-react';

export default function Navbar({ usuario, onLogout, onNavigate }) {
  const [isOpen, setIsOpen] = useState(false);
  const [profilePhoto, setProfilePhoto] = useState('https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200&auto=format&fit=crop');

  // Load profile photo reactively
  useEffect(() => {
    const loadPhoto = () => {
      if (usuario) {
        const saved = localStorage.getItem(`profile_${usuario.username}`);
        if (saved) {
          try {
            const parsed = JSON.parse(saved);
            if (parsed.foto) {
              setProfilePhoto(parsed.foto);
            }
          } catch (e) {
            console.error("Erro ao carregar foto do navbar:", e);
          }
        }
      }
    };

    loadPhoto();
    
    // Listen for storage events (allows sync in same tab when profile updates)
    window.addEventListener('storage', loadPhoto);
    return () => window.removeEventListener('storage', loadPhoto);
  }, [usuario]);

  return (
    <header className="glass-panel" style={{
      position: 'sticky',
      top: 0,
      zIndex: 100,
      borderRadius: '0 0 var(--border-radius-md) var(--border-radius-md)',
      borderTop: 'none',
      borderLeft: 'none',
      borderRight: 'none',
      padding: '16px 0',
      marginBottom: '32px'
    }}>
      <div className="container" style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center'
      }}>
        {/* Logo brand: DSCboxd */}
        <div 
          onClick={() => onNavigate('home')} 
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            cursor: 'pointer',
            userSelect: 'none'
          }}
        >
          {/* Logo Icon */}
          <div style={{
            background: 'linear-gradient(135deg, var(--accent), var(--primary))',
            borderRadius: '8px',
            padding: '6px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 0 12px var(--primary-glow)'
          }}>
            <Film size={20} color="#0c0e12" />
          </div>
          <span style={{
            fontSize: '20px',
            fontWeight: 800,
            color: '#fff',
            letterSpacing: '-0.5px'
          }}>
            DSC<span style={{
              background: 'linear-gradient(135deg, var(--primary), var(--primary-hover))',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent'
            }}>boxd</span>
          </span>
        </div>

        {/* User Profile Dropdown Nav Trigger */}
        <nav style={{ display: 'flex', alignItems: 'center', zIndex: 110 }}>
          {usuario && (
            <button
              onClick={() => onNavigate('diario')}
              className="btn-secondary"
              style={{
                padding: '6px 14px',
                fontSize: '13px',
                borderRadius: '20px',
                border: '1px solid rgba(255, 255, 255, 0.04)',
                background: 'rgba(255, 255, 255, 0.01)',
                marginRight: '12px',
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                color: 'var(--text-secondary)'
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.color = '#fff';
                e.currentTarget.style.borderColor = 'rgba(0, 224, 84, 0.3)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.color = 'var(--text-secondary)';
                e.currentTarget.style.borderColor = 'rgba(255, 255, 255, 0.04)';
              }}
            >
              <Calendar size={14} color="var(--primary)" />
              <span>Diário</span>
            </button>
          )}
          {usuario && (
            <div style={{ position: 'relative' }}>
              {/* Trigger click panel */}
              <div 
                onClick={() => setIsOpen(!isOpen)}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '10px',
                  cursor: 'pointer',
                  userSelect: 'none',
                  padding: '6px 12px',
                  borderRadius: '20px',
                  transition: 'background-color 0.2s',
                  backgroundColor: isOpen ? 'rgba(255,255,255,0.05)' : 'transparent'
                }}
                onMouseEnter={(e) => {
                  if (!isOpen) e.currentTarget.style.backgroundColor = 'rgba(255,255,255,0.03)';
                }}
                onMouseLeave={(e) => {
                  if (!isOpen) e.currentTarget.style.backgroundColor = 'transparent';
                }}
              >
                <span style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>
                  {usuario.username}
                </span>
                <img 
                  src={profilePhoto} 
                  alt={usuario.username} 
                  style={{
                    width: '32px',
                    height: '32px',
                    borderRadius: '50%',
                    objectFit: 'cover',
                    border: '2px solid var(--primary)',
                    boxShadow: '0 0 8px var(--primary-glow)'
                  }}
                  onError={(e) => {
                    e.target.src = 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200&auto=format&fit=crop';
                  }}
                />
              </div>

              {/* Dropdown Menu Panel */}
              {isOpen && (
                <>
                  {/* Backdrop overlay to close dropdown on click outside */}
                  <div 
                    onClick={() => setIsOpen(false)}
                    style={{
                      position: 'fixed',
                      top: 0,
                      left: 0,
                      right: 0,
                      bottom: 0,
                      zIndex: 100,
                      cursor: 'default'
                    }}
                  />
                  <div 
                    className="glass-panel animate-fade-in"
                    style={{
                      position: 'absolute',
                      top: '45px',
                      right: 0,
                      width: '170px',
                      backgroundColor: 'var(--bg-secondary)',
                      border: '1px solid rgba(255, 255, 255, 0.05)',
                      boxShadow: '0 10px 30px rgba(0,0,0,0.6)',
                      borderRadius: 'var(--border-radius-sm)',
                      padding: '4px 0',
                      zIndex: 101
                    }}
                  >
                    {/* View Profile */}
                    <button 
                      onClick={() => {
                        onNavigate('profile');
                        setIsOpen(false);
                      }}
                      style={{
                        width: '100%',
                        padding: '10px 16px',
                        background: 'none',
                        border: 'none',
                        color: 'var(--text-secondary)',
                        textAlign: 'left',
                        fontSize: '13px',
                        fontWeight: 600,
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px',
                        transition: 'var(--transition-smooth)'
                      }}
                      onMouseEnter={(e) => {
                        e.currentTarget.style.color = '#fff';
                        e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.03)';
                      }}
                      onMouseLeave={(e) => {
                        e.currentTarget.style.color = 'var(--text-secondary)';
                        e.currentTarget.style.backgroundColor = 'transparent';
                      }}
                    >
                      <User size={14} color="var(--primary)" />
                      <span>Ver Perfil</span>
                    </button>
                    
                    {/* Meu Diário */}
                    <button 
                      onClick={() => {
                        onNavigate('diario');
                        setIsOpen(false);
                      }}
                      style={{
                        width: '100%',
                        padding: '10px 16px',
                        background: 'none',
                        border: 'none',
                        color: 'var(--text-secondary)',
                        textAlign: 'left',
                        fontSize: '13px',
                        fontWeight: 600,
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px',
                        transition: 'var(--transition-smooth)'
                      }}
                      onMouseEnter={(e) => {
                        e.currentTarget.style.color = '#fff';
                        e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.03)';
                      }}
                      onMouseLeave={(e) => {
                        e.currentTarget.style.color = 'var(--text-secondary)';
                        e.currentTarget.style.backgroundColor = 'transparent';
                      }}
                    >
                      <Calendar size={14} color="var(--primary)" />
                      <span>Meu Diário</span>
                    </button>
                    
                    <div style={{ height: '1px', background: 'rgba(255,255,255,0.04)', margin: '4px 0' }} />
                    
                    {/* Logout */}
                    <button 
                      onClick={() => {
                        onLogout();
                        setIsOpen(false);
                      }}
                      style={{
                        width: '100%',
                        padding: '10px 16px',
                        background: 'none',
                        border: 'none',
                        color: 'var(--danger)',
                        textAlign: 'left',
                        fontSize: '13px',
                        fontWeight: 600,
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px',
                        transition: 'var(--transition-smooth)'
                      }}
                      onMouseEnter={(e) => {
                        e.currentTarget.style.backgroundColor = 'rgba(255, 51, 102, 0.05)';
                      }}
                      onMouseLeave={(e) => {
                        e.currentTarget.style.backgroundColor = 'transparent';
                      }}
                    >
                      <LogOut size={14} />
                      <span>Sair</span>
                    </button>
                  </div>
                </>
              )}
            </div>
          )}
        </nav>
      </div>
    </header>
  );
}
