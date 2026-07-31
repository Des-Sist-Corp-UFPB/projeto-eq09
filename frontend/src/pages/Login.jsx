import React, { useState } from 'react';
import { User, Lock, LogIn, AlertCircle } from 'lucide-react';

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
    <div className="animate-fade-in" style={{ width: '100%', display: 'flex', justifyContent: 'center' }}>
      <div 
        className="glass-panel" 
        style={{
          width: '100%',
          maxWidth: '400px',
          padding: '40px 32px',
          backgroundColor: 'var(--bg-secondary)',
          border: '1px solid rgba(255, 255, 255, 0.05)',
          boxShadow: '0 20px 50px rgba(0, 0, 0, 0.5)'
        }}
      >
        {/* Header */}
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <div style={{
            background: 'linear-gradient(135deg, var(--accent), var(--primary))',
            borderRadius: '10px',
            width: '44px',
            height: '44px',
            margin: '0 auto 16px auto',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 0 15px var(--primary-glow)'
          }}>
            <LogIn size={20} color="#0c0e12" />
          </div>
          <h2 style={{ fontSize: '24px', fontWeight: 800, color: '#fff', marginBottom: '6px', letterSpacing: '-0.5px' }}>
            DSC<span style={{ color: 'var(--primary)' }}>boxd</span>
          </h2>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>Acesse sua conta para avaliar e comentar filmes</p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {/* Username */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
              Nome de Usuário
            </label>
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
              <User size={15} color="var(--text-muted)" style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)' }} />
            </div>
          </div>

          {/* Password */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
              Senha
            </label>
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
              <Lock size={15} color="var(--text-muted)" style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)' }} />
            </div>
          </div>

          {/* Error Alert */}
          {error && (
            <div className="glass-panel" style={{
              padding: '10px 14px',
              borderRadius: 'var(--border-radius-sm)',
              backgroundColor: 'rgba(255, 51, 102, 0.1)',
              borderColor: 'var(--danger)',
              color: 'var(--danger)',
              fontSize: '13px',
              fontWeight: 500,
              display: 'flex',
              alignItems: 'center',
              gap: '8px'
            }}>
              <AlertCircle size={15} style={{ flexShrink: 0 }} />
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
          <span style={{ fontSize: '13px', color: 'var(--text-muted)' }}>Não tem uma conta? </span>
          <button 
            onClick={() => onNavigate('register')}
            style={{
              background: 'none',
              border: 'none',
              color: 'var(--primary)',
              fontWeight: 700,
              fontSize: '13px',
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
