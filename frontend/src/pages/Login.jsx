import React, { useState } from 'react';
import { User, Lock, Key, LogIn, AlertCircle } from 'lucide-react';

export default function Login({ onLoginSuccess, onNavigate }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    
    if (!username.trim() || !password.trim()) {
      setError('Por favor, preencha todos os campos.');
      return;
    }

    setIsLoading(true);
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username, password })
      });

      if (response.ok) {
        const userData = await response.json(); // { token, username, role }
        onLoginSuccess(userData);
      } else {
        const text = await response.text();
        setError(text || 'Usuário ou senha incorretos.');
      }
    } catch (err) {
      setError('Erro de conexão com o servidor. Verifique se a API está online.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="container animate-fade-in" style={{
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      minHeight: '65vh',
      paddingTop: '20px'
    }}>
      <div 
        className="glass-panel" 
        style={{
          width: '100%',
          maxWidth: '420px',
          padding: '40px 32px',
          border: '1px solid rgba(157, 78, 221, 0.2)',
          boxShadow: '0 15px 35px rgba(157, 78, 221, 0.1)'
        }}
      >
        {/* Header */}
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <div style={{
            background: 'linear-gradient(135deg, var(--primary), var(--primary-hover))',
            borderRadius: '12px',
            width: '48px',
            height: '48px',
            margin: '0 auto 16px auto',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 0 15px var(--primary-glow)'
          }}>
            <LogIn size={24} color="#fff" />
          </div>
          <h2 style={{ fontSize: '26px', fontWeight: 800, color: '#fff', marginBottom: '8px' }}>Bem-vindo de Volta</h2>
          <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>Acesse sua conta para avaliar e comentar filmes</p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {/* Username */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <label style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>Nome de Usuário</label>
            <div style={{ position: 'relative' }}>
              <input
                type="text"
                placeholder="Ex: joao_cinema"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="form-input"
                style={{ paddingLeft: '40px' }}
                required
              />
              <User size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)' }} />
            </div>
          </div>

          {/* Password */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <label style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>Senha</label>
            <div style={{ position: 'relative' }}>
              <input
                type="password"
                placeholder="Sua senha secreta"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="form-input"
                style={{ paddingLeft: '40px' }}
                required
              />
              <Lock size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)' }} />
            </div>
          </div>

          {/* Error Alert */}
          {error && (
            <div className="glass-panel" style={{
              padding: '10px 14px',
              borderRadius: 'var(--border-radius-sm)',
              backgroundColor: 'rgba(239, 71, 111, 0.1)',
              borderColor: 'var(--danger)',
              color: 'var(--danger)',
              fontSize: '13px',
              fontWeight: 500,
              display: 'flex',
              alignItems: 'center',
              gap: '8px'
            }}>
              <AlertCircle size={16} style={{ flexShrink: 0 }} />
              <span>{error}</span>
            </div>
          )}

          {/* Submit */}
          <button 
            type="submit" 
            disabled={isLoading}
            className="btn-primary" 
            style={{ width: '100%', marginTop: '8px', justifyContent: 'center' }}
          >
            {isLoading ? 'Autenticando...' : 'Entrar'}
          </button>
        </form>

        {/* Register Link footer */}
        <div style={{ textAlign: 'center', marginTop: '28px', borderTop: '1px solid rgba(255,255,255,0.05)', paddingTop: '20px' }}>
          <span style={{ fontSize: '14px', color: 'var(--text-muted)' }}>Não tem uma conta? </span>
          <button 
            onClick={() => onNavigate('register')}
            style={{
              background: 'none',
              border: 'none',
              color: 'var(--primary)',
              fontWeight: 600,
              fontSize: '14px',
              cursor: 'pointer',
              textDecoration: 'underline',
              padding: 0
            }}
          >
            Cadastre-se aqui
          </button>
        </div>

      </div>
    </div>
  );
}
