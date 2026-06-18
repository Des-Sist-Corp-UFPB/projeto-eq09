import React, { useState, useEffect } from 'react';
import { X, Star, MessageSquare, Send, Calendar, Tag, User, MessageCircle, Trash2 } from 'lucide-react';

export default function MovieDetailModal({ filme, usuario, onClose }) {
  const [comentarios, setComentarios] = useState([]);
  const [novoComentario, setNovoComentario] = useState('');
  const [userRating, setUserRating] = useState(0);
  const [isLoadingComments, setIsLoadingComments] = useState(false);
  const [submitMessage, setSubmitMessage] = useState({ text: '', type: '' });
  
  // Local details for reactive rating update
  const [notaMediaLocal, setNotaMediaLocal] = useState(filme.notaMedia || 0);
  const [totalAvaliacoesLocal, setTotalAvaliacoesLocal] = useState(filme.totalAvaliacoes || 0);

  const fetchComments = async () => {
    setIsLoadingComments(true);
    try {
      const response = await fetch(`/api/filmes/${filme.id}/comentarios`);
      if (response.ok) {
        const data = await response.json();
        setComentarios(data);
      }
    } catch (err) {
      console.error("Erro ao carregar comentários:", err);
    } finally {
      setIsLoadingComments(false);
    }
  };

  useEffect(() => {
    fetchComments();
    
    // Gets from localStorage if current logged-in user already rated this movie
    if (usuario) {
      const storedRating = localStorage.getItem(`rating_${usuario.username}_${filme.id}`);
      if (storedRating) {
        setUserRating(parseInt(storedRating, 10));
      }
    }
  }, [filme.id, usuario]);

  const handleRate = async (nota) => {
    if (!usuario) {
      setSubmitMessage({ text: 'Você precisa entrar na sua conta para avaliar!', type: 'danger' });
      return;
    }

    try {
      const response = await fetch(`/api/filmes/${filme.id}/avaliar`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${usuario.token}`
        },
        body: JSON.stringify({ nota })
      });

      if (response.ok) {
        // If first rating by this user, increment total local count
        const jaAvaliou = localStorage.getItem(`rating_${usuario.username}_${filme.id}`);
        if (!jaAvaliou) {
          setTotalAvaliacoesLocal(prev => prev + 1);
          setNotaMediaLocal(prev => {
            const sum = (prev * totalAvaliacoesLocal) + nota;
            return Math.round((sum / (totalAvaliacoesLocal + 1)) * 10) / 10;
          });
        } else {
          // Update local average
          setNotaMediaLocal(prev => {
            const oldRating = parseInt(jaAvaliou, 10);
            const sum = (prev * totalAvaliacoesLocal) - oldRating + nota;
            return Math.round((sum / totalAvaliacoesLocal) * 10) / 10;
          });
        }

        setUserRating(nota);
        localStorage.setItem(`rating_${usuario.username}_${filme.id}`, nota.toString());
        setSubmitMessage({ text: 'Avaliação enviada com sucesso!', type: 'success' });
        
        setTimeout(() => setSubmitMessage({ text: '', type: '' }), 3000);
      } else {
        const text = await response.text();
        setSubmitMessage({ text: text || 'Erro ao avaliar o filme.', type: 'danger' });
      }
    } catch (err) {
      setSubmitMessage({ text: 'Falha de rede ao enviar avaliação.', type: 'danger' });
    }
  };

  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!novoComentario.trim()) return;

    if (!usuario) {
      setSubmitMessage({ text: 'Você precisa entrar na sua conta para comentar!', type: 'danger' });
      return;
    }

    try {
      const response = await fetch(`/api/filmes/${filme.id}/comentarios`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${usuario.token}`
        },
        body: JSON.stringify({ texto: novoComentario })
      });

      if (response.ok) {
        const comentarioSalvo = await response.json();
        setComentarios(prev => [comentarioSalvo, ...prev]);
        setNovoComentario('');
        setSubmitMessage({ text: 'Comentário publicado!', type: 'success' });
        
        setTimeout(() => setSubmitMessage({ text: '', type: '' }), 3000);
      } else {
        setSubmitMessage({ text: 'Erro ao enviar comentário.', type: 'danger' });
      }
    } catch (err) {
      setSubmitMessage({ text: 'Falha de rede ao enviar comentário.', type: 'danger' });
    }
  };

  const handleDeleteComment = async (comentarioId) => {
    if (!usuario || usuario.role !== 'ADMIN') {
      setSubmitMessage({ text: 'Apenas administradores podem excluir comentários.', type: 'danger' });
      return;
    }

    if (!window.confirm('Tem certeza de que deseja excluir este comentário?')) {
      return;
    }

    try {
      const response = await fetch(`/api/filmes/${filme.id}/comentarios/${comentarioId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${usuario.token}`
        }
      });

      if (response.ok) {
        setComentarios(prev => prev.filter(c => c.id !== comentarioId));
        setSubmitMessage({ text: 'Comentário excluído com sucesso!', type: 'success' });
        setTimeout(() => setSubmitMessage({ text: '', type: '' }), 3000);
      } else {
        const text = await response.text();
        setSubmitMessage({ text: text || 'Erro ao excluir comentário.', type: 'danger' });
      }
    } catch (err) {
      setSubmitMessage({ text: 'Falha de rede ao excluir comentário.', type: 'danger' });
    }
  };

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(12, 14, 18, 0.88)',
      backdropFilter: 'blur(12px)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 1000,
      padding: '16px'
    }}>
      <div 
        className="glass-panel animate-fade-in"
        style={{
          width: '100%',
          maxWidth: '850px',
          maxHeight: '92vh',
          display: 'flex',
          flexDirection: 'column',
          backgroundColor: 'var(--bg-secondary)',
          overflow: 'hidden',
          border: '1px solid rgba(255, 255, 255, 0.05)'
        }}
      >
        {/* Header */}
        <div style={{
          padding: '16px 24px',
          borderBottom: '1px solid rgba(255, 255, 255, 0.04)',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          background: 'rgba(255,255,255,0.01)'
        }}>
          <h2 style={{ fontSize: '20px', fontWeight: 800, color: '#fff', letterSpacing: '-0.5px' }}>Detalhes do Filme</h2>
          <button 
            onClick={onClose}
            className="btn-secondary"
            style={{
              padding: '6px',
              borderRadius: '50px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}
          >
            <X size={18} />
          </button>
        </div>

        {/* Scrollable Content Container */}
        <div style={{
          overflowY: 'auto',
          padding: '24px',
          display: 'flex',
          flexDirection: 'column',
          gap: '24px'
        }}>
          
          {/* Top Panel: Poster and Info (Uses CSS responsive class) */}
          <div className="modal-grid-top">
            
            {/* Poster */}
            <div style={{
              width: '100%',
              borderRadius: 'var(--border-radius-sm)',
              overflow: 'hidden',
              boxShadow: '0 8px 25px rgba(0,0,0,0.6)',
              border: '1px solid rgba(255,255,255,0.04)'
            }}>
              <img 
                src={filme.imagemUrl || "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=500&auto=format&fit=crop"} 
                alt={filme.titulo}
                style={{ width: '100%', display: 'block', objectFit: 'cover', minHeight: '280px', maxHeight: '380px' }}
                onError={(e) => {
                  e.target.src = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=500&auto=format&fit=crop";
                }}
              />
            </div>

            {/* Info and Synopsis */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div>
                <h1 style={{ fontSize: '26px', fontWeight: 800, marginBottom: '8px', color: '#fff', letterSpacing: '-0.5px', lineHeight: '1.2' }}>
                  {filme.titulo}
                </h1>
                
                {/* Meta row */}
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px', marginBottom: '16px' }}>
                  {filme.genero && (
                    <span style={{
                      fontSize: '11px',
                      fontWeight: 700,
                      padding: '3px 10px',
                      borderRadius: '50px',
                      color: 'var(--primary)',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '4px',
                      background: 'rgba(0, 224, 84, 0.08)',
                      border: '1px solid rgba(0, 224, 84, 0.15)'
                    }}>
                      <Tag size={10} />
                      {filme.genero}
                    </span>
                  )}
                  <span className="glass-panel" style={{
                    fontSize: '11px',
                    fontWeight: 600,
                    padding: '3px 10px',
                    borderRadius: '50px',
                    color: 'var(--text-secondary)',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px',
                    background: 'rgba(255,255,255,0.02)'
                  }}>
                    <Calendar size={10} />
                    {filme.ano}
                  </span>
                  <span className="glass-panel" style={{
                    fontSize: '11px',
                    fontWeight: 600,
                    padding: '3px 10px',
                    borderRadius: '50px',
                    color: 'var(--text-secondary)',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px',
                    background: 'rgba(255,255,255,0.02)'
                  }}>
                    <User size={10} />
                    Dirigido por: {filme.diretor || 'Desconhecido'}
                  </span>
                </div>
              </div>

              {/* Synopsis */}
              <div>
                <h4 style={{ color: 'var(--text-muted)', fontSize: '11px', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.8px', marginBottom: '6px' }}>Sinopse</h4>
                <p style={{ color: 'var(--text-secondary)', fontSize: '14px', lineHeight: '1.6' }}>
                  {filme.sinopse || "Sem sinopse disponível para este filme."}
                </p>
              </div>

              {/* Rating metrics */}
              <div style={{
                display: 'flex',
                alignItems: 'center',
                gap: '20px',
                padding: '14px 16px',
                background: 'rgba(255, 255, 255, 0.01)',
                borderRadius: 'var(--border-radius-sm)',
                border: '1px solid rgba(255,255,255,0.02)',
                marginTop: '4px'
              }}>
                <div>
                  <span style={{ fontSize: '10px', fontWeight: 700, color: 'var(--text-muted)', display: 'block', textTransform: 'uppercase', marginBottom: '4px' }}>Nota Média</span>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Star size={20} style={{ color: 'var(--star-color)', fill: 'var(--star-color)' }} />
                    <span style={{ fontSize: '24px', fontWeight: 800, color: '#fff' }}>{notaMediaLocal.toFixed(1)}</span>
                  </div>
                </div>
                
                <div style={{ width: '1px', height: '32px', background: 'rgba(255,255,255,0.05)' }}></div>
                
                <div>
                  <span style={{ fontSize: '10px', fontWeight: 700, color: 'var(--text-muted)', display: 'block', textTransform: 'uppercase', marginBottom: '4px' }}>Avaliações</span>
                  <span style={{ fontSize: '20px', fontWeight: 700, color: 'var(--text-primary)' }}>{totalAvaliacoesLocal}</span>
                </div>
              </div>

            </div>

          </div>

          {/* Messages alert */}
          {submitMessage.text && (
            <div className="glass-panel" style={{
              padding: '10px 14px',
              borderRadius: 'var(--border-radius-sm)',
              backgroundColor: submitMessage.type === 'success' ? 'rgba(0, 224, 84, 0.1)' : 'rgba(255, 51, 102, 0.1)',
              borderColor: submitMessage.type === 'success' ? 'var(--success)' : 'var(--danger)',
              color: submitMessage.type === 'success' ? 'var(--success)' : 'var(--danger)',
              fontSize: '13px',
              fontWeight: 500,
              textAlign: 'center'
            }}>
              {submitMessage.text}
            </div>
          )}

          {/* Interactive Rating Form */}
          <div className="glass-panel" style={{ padding: '16px 20px', display: 'flex', flexWrap: 'wrap', gap: '16px', alignItems: 'center', justifyContent: 'space-between', backgroundColor: 'rgba(255,255,255,0.01)' }}>
            <div>
              <h3 style={{ fontSize: '14px', fontWeight: 700, marginBottom: '2px', color: '#fff' }}>Sua Avaliação</h3>
              <p style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                {usuario ? 'Clique em uma estrela para registrar sua nota' : 'Faça login para avaliar este filme'}
              </p>
            </div>
            
            <div className="star-rating">
              {[1, 2, 3, 4, 5].map((star) => (
                <Star
                  key={star}
                  size={28}
                  className={`star ${star <= userRating ? 'active' : ''}`}
                  style={{
                    cursor: usuario ? 'pointer' : 'not-allowed',
                    fill: star <= userRating ? 'var(--star-color)' : 'none',
                    stroke: star <= userRating ? 'var(--star-color)' : 'currentColor'
                  }}
                  onClick={() => handleRate(star)}
                />
              ))}
            </div>
          </div>

          {/* Comments Section */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <h3 style={{ fontSize: '16px', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '8px', color: '#fff' }}>
              <MessageSquare size={18} color="var(--primary)" />
              Comentários ({comentarios.length})
            </h3>

            {/* Comment Form */}
            {usuario ? (
              <form onSubmit={handleAddComment} style={{ display: 'flex', gap: '10px' }}>
                <input
                  type="text"
                  placeholder="Escreva um comentário sobre o filme..."
                  value={novoComentario}
                  onChange={(e) => setNovoComentario(e.target.value)}
                  className="form-input"
                  style={{ flexGrow: 1 }}
                />
                <button type="submit" className="btn-primary" style={{ padding: '10px 18px', flexShrink: 0, fontSize: '13px' }}>
                  <Send size={14} />
                  <span>Enviar</span>
                </button>
              </form>
            ) : (
              <div className="glass-panel" style={{ padding: '12px', textAlign: 'center', fontSize: '13px', color: 'var(--text-secondary)', background: 'rgba(255,255,255,0.01)' }}>
                Você precisa estar logado para comentar.
              </div>
            )}

            {/* Comments List */}
            <div 
              className="glass-panel" 
              style={{
                maxHeight: '260px',
                overflowY: 'auto',
                padding: '14px',
                display: 'flex',
                flexDirection: 'column',
                gap: '10px',
                background: 'rgba(20, 24, 28, 0.4)',
                border: '1px solid rgba(255, 255, 255, 0.03)'
              }}
            >
              {isLoadingComments ? (
                <div style={{ textAlign: 'center', padding: '16px', color: 'var(--text-secondary)', fontSize: '13px' }}>Carregando comentários...</div>
              ) : comentarios.length > 0 ? (
                comentarios.map((c) => (
                  <div 
                    key={c.id} 
                    style={{
                      padding: '12px',
                      borderRadius: 'var(--border-radius-sm)',
                      background: 'rgba(255,255,255,0.01)',
                      border: '1px solid rgba(255,255,255,0.02)'
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '4px' }}>
                      <span style={{ fontSize: '12px', fontWeight: 700, color: 'var(--primary)' }}>@{c.username}</span>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <span style={{ fontSize: '10px', color: 'var(--text-muted)' }}>
                          {new Date(c.criadoEm).toLocaleDateString('pt-BR', { hour: '2-digit', minute: '2-digit' })}
                        </span>
                        {usuario?.role === 'ADMIN' && (
                          <button
                            onClick={() => handleDeleteComment(c.id)}
                            style={{
                              background: 'none',
                              border: 'none',
                              color: 'var(--danger)',
                              cursor: 'pointer',
                              padding: '2px',
                              borderRadius: '4px',
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'center',
                              opacity: 0.7,
                              transition: 'opacity 0.2s, background-color 0.2s',
                            }}
                            title="Excluir comentário"
                            onMouseEnter={(e) => { e.currentTarget.style.opacity = 1; e.currentTarget.style.backgroundColor = 'rgba(255, 51, 102, 0.1)'; }}
                            onMouseLeave={(e) => { e.currentTarget.style.opacity = 0.7; e.currentTarget.style.backgroundColor = 'transparent'; }}
                          >
                            <Trash2 size={13} />
                          </button>
                        )}
                      </div>
                    </div>
                    <p style={{ fontSize: '13px', color: 'var(--text-secondary)', lineHeight: '1.45' }}>{c.texto}</p>
                  </div>
                ))
              ) : (
                <div style={{
                  padding: '24px 10px',
                  textAlign: 'center',
                  color: 'var(--text-muted)',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  gap: '6px'
                }}>
                  <MessageCircle size={28} color="var(--text-muted)" />
                  <span style={{ fontSize: '13px' }}>Seja o primeiro a comentar neste filme!</span>
                </div>
              )}
            </div>

          </div>

        </div>
      </div>
    </div>
  );
}
