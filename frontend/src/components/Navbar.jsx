import React from 'react';
import { Film, LogOut, User, Shield } from 'lucide-react';

export default function Navbar({ usuario, onLogout, onNavigate }) {
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
      marginBottom: '40px'
    }}>
      <div className="container" style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center'
      }}>
        {/* Logo */}
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
          <div style={{
            background: 'linear-gradient(135deg, var(--primary), var(--primary-hover))',
            borderRadius: '10px',
            padding: '8px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 0 15px var(--primary-glow)'
          }}>
            <Film size={24} color="#fff" />
          </div>
          <span style={{
            fontSize: '22px',
            fontWeight: 800,
            background: 'linear-gradient(135deg, #fff, var(--text-secondary))',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            letterSpacing: '1px'
          }}>
            Cine<span style={{
              background: 'linear-gradient(135deg, var(--primary), var(--primary-hover))',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent'
            }}>Avalia</span>
          </span>
        </div>

        {/* Navigation & Profile */}
        <nav style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
          {usuario ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
              {/* User Info Capsule */}
              <div className="glass-panel" style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                padding: '6px 14px',
                borderRadius: '50px',
                background: 'rgba(255, 255, 255, 0.03)',
                fontSize: '14px',
                fontWeight: 500
              }}>
                {usuario.role === 'ADMIN' ? (
                  <Shield size={16} color="var(--primary)" style={{ flexShrink: 0 }} />
                ) : (
                  <User size={16} color="var(--accent)" style={{ flexShrink: 0 }} />
                )}
                <span style={{ color: 'var(--text-primary)' }}>{usuario.username}</span>
                {usuario.role === 'ADMIN' && (
                  <span style={{
                    fontSize: '10px',
                    fontWeight: 700,
                    background: 'rgba(157, 78, 221, 0.2)',
                    color: 'var(--primary)',
                    padding: '2px 6px',
                    borderRadius: '10px',
                    marginLeft: '4px',
                    border: '1px solid rgba(157, 78, 221, 0.3)'
                  }}>
                    ADMIN
                  </span>
                )}
              </div>

              {/* Logout Button */}
              <button 
                onClick={onLogout}
                className="btn-secondary"
                style={{
                  padding: '8px 16px',
                  fontSize: '14px',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px'
                }}
              >
                <LogOut size={16} />
                <span>Sair</span>
              </button>
            </div>
          ) : (
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <button 
                onClick={() => onNavigate('login')}
                className="btn-secondary"
                style={{ padding: '8px 18px', fontSize: '14px' }}
              >
                Entrar
              </button>
              <button 
                onClick={() => onNavigate('register')}
                className="btn-primary"
                style={{ padding: '8px 18px', fontSize: '14px', boxShadow: 'none' }}
              >
                Criar Conta
              </button>
            </div>
          )}
        </nav>
      </div>
    </header>
  );
}
