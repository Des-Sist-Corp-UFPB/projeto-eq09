import React, { useState } from 'react';
import { User, Lock, UserPlus, Shield, AlertCircle, CheckCircle } from 'lucide-react';

export default function Register({ onNavigate }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [role, setRole] = useState('USER'); // Default is USER, but can be ADMIN for ease of testing!
  
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!username.trim() || !password.trim()) {
      setError('Por favor, preencha todos os campos.');
      return;
    }

    if (username.length < 3) {
      setError('O nome de usuário deve ter pelo menos 3 caracteres.');
      return;
    }

    if (password.length < 6) {
      setError('A senha deve ter pelo menos 6 caracteres.');
      return;
    }

    if (password !== confirmPassword) {
      setError('As senhas informadas não coincidem.');
      return;
    }

    setIsLoading(true);
    try {
      const response = await fetch('http://localhost:8080/api/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username, password, role })
      });

      if (response.ok) {
        setSuccess('Sua conta foi criada com sucesso! Redirecionando para login...');
        setTimeout(() => {
          onNavigate('login');
        }, 2000);
      } else {
        const text = await response.text();
        setError(text || 'Falha ao registrar usuário. O nome de usuário já pode estar em uso.');
      }
    } catch (err) {
      setError('Erro de conexão ao servidor.');
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
          maxWidth: '440px',
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
            <UserPlus size={24} color="#fff" />
          </div>
          <h2 style={{ fontSize: '26px', fontWeight: 800, color: '#fff', marginBottom: '8px' }}>Criar Nova Conta</h2>
          <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>Cadastre-se para participar da comunidade CineAvalia</p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
          
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

          {/* User Role (for easy testing!) */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <label style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>Tipo de Conta</label>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
              <button
                type="button"
                onClick={() => setRole('USER')}
                className={role === 'USER' ? 'btn-primary' : 'btn-secondary'}
                style={{
                  padding: '10px',
                  fontSize: '13px',
                  justifyContent: 'center',
                  boxShadow: 'none',
                  border: role === 'USER' ? 'none' : '1px solid var(--glass-border)'
                }}
              >
                Usuário Comum
              </button>
              <button
                type="button"
                onClick={() => setRole('ADMIN')}
                className={role === 'ADMIN' ? 'btn-primary' : 'btn-secondary'}
                style={{
                  padding: '10px',
                  fontSize: '13px',
                  justifyContent: 'center',
                  boxShadow: 'none',
                  border: role === 'ADMIN' ? 'none' : '1px solid var(--glass-border)'
                }}
              >
                Administrador
              </button>
            </div>
          </div>

          {/* Password */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <label style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>Senha</label>
            <div style={{ position: 'relative' }}>
              <input
                type="password"
                placeholder="Mínimo 6 caracteres"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="form-input"
                style={{ paddingLeft: '40px' }}
                required
              />
              <Lock size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)' }} />
            </div>
          </div>

          {/* Confirm Password */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <label style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>Confirmar Senha</label>
            <div style={{ position: 'relative' }}>
              <input
                type="password"
                placeholder="Repita sua senha"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className="form-input"
                style={{ paddingLeft: '40px' }}
                required
              />
              <Lock size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)' }} />
            </div>
          </div>

          {/* Feedback alerts */}
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
          {success && (
            <div className="glass-panel" style={{
              padding: '10px 14px',
              borderRadius: 'var(--border-radius-sm)',
              backgroundColor: 'rgba(6, 214, 160, 0.1)',
              borderColor: 'var(--success)',
              color: 'var(--success)',
              fontSize: '13px',
              fontWeight: 500,
              display: 'flex',
              alignItems: 'center',
              gap: '8px'
            }}>
              <CheckCircle size={16} style={{ flexShrink: 0 }} />
              <span>{success}</span>
            </div>
          )}

          {/* Submit */}
          <button 
            type="submit" 
            disabled={isLoading}
            className="btn-primary" 
            style={{ width: '100%', marginTop: '8px', justifyContent: 'center' }}
          >
            {isLoading ? 'Registrando...' : 'Cadastrar'}
          </button>
        </form>

        {/* Footer link */}
        <div style={{ textAlign: 'center', marginTop: '28px', borderTop: '1px solid rgba(255,255,255,0.05)', paddingTop: '20px' }}>
          <span style={{ fontSize: '14px', color: 'var(--text-muted)' }}>Já possui uma conta? </span>
          <button 
            onClick={() => onNavigate('login')}
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
            Conecte-se
          </button>
        </div>

      </div>
    </div>
  );
}
