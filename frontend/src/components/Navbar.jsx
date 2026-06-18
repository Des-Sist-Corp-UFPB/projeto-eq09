import React from 'react';
import { Film, LogOut } from 'lucide-react';

export default function Navbar({ onLogout, onNavigate }) {
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
          {/* Logo Icon with Letterboxd Tri-color styled ring/bg */}
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

        {/* Minimalist Navigation: Only Logout Button */}
        <nav style={{ display: 'flex', alignItems: 'center' }}>
          <button 
            onClick={onLogout}
            className="btn-secondary"
            style={{
              padding: '8px 16px',
              fontSize: '14px',
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              border: '1px solid rgba(255, 255, 255, 0.05)',
              borderRadius: 'var(--border-radius-sm)',
              background: 'rgba(255, 255, 255, 0.02)'
            }}
          >
            <LogOut size={15} />
            <span>Sair</span>
          </button>
        </nav>
      </div>
    </header>
  );
}
