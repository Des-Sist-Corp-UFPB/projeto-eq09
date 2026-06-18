import React, { useState, useEffect } from 'react';
import { X, Star, MessageSquare, Send, Calendar, Tag, User, MessageCircle, Trash2 } from 'lucide-react';

export default function MovieDetailModal({ filme, usuario, onClose }) {
  const [comentarios, setComentarios] = useState([]);
  const [novoComentario, setNovoComentario] = useState('');
  const [userRating, setUserRating] = useState(0);
  const [isLoadingComments, setIsLoadingComments] = useState(false);
  const [submitMessage, setSubmitMessage] = useState({ text: '', type: '' });
  
  // Detalhes locais para atualização reativa de nota
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
    
    // Tenta obter do localStorage se o usuário logado já avaliou este filme
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
        // Se for a primeira avaliação deste usuário, incrementa o total
        const jaAvaliou = localStorage.getItem(`rating_${usuario.username}_${filme.id}`);
        if (!jaAvaliou) {
          setTotalAvaliacoesLocal(prev => prev + 1);
          // Calcula nova média aproximada localmente
          setNotaMediaLocal(prev => {
            const sum = (prev * totalAvaliacoesLocal) + nota;
            return Math.round((sum / (totalAvaliacoesLocal + 1)) * 10) / 10;
          });
        } else {
          // Atualiza a média
          setNotaMediaLocal(prev => {
            const oldRating = parseInt(jaAvaliou, 10);
            const sum = (prev * totalAvaliacoesLocal) - oldRating + nota;
            return Math.round((sum / totalAvaliacoesLocal) * 10) / 10;
          });
        }

        setUserRating(nota);
        localStorage.setItem(`rating_${usuario.username}_${filme.id}`, nota.toString());
        setSubmitMessage({ text: 'Avaliação enviada com sucesso!', type: 'success' });
        
        // Remove a mensagem após 3 segundos
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
      backgroundColor: 'rgba(5, 5, 8, 0.85)',
      backdropFilter: 'blur(10px)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 1000,
      padding: '20px'
    }}>
      <div 
        className="glass-panel animate-fade-in"
        style={{
          width: '100%',
          maxWidth: '900px',
          maxHeight: '90vh',
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
          border: '1px solid rgba(157, 78, 221, 0.15)'
        }}
      >
        {/* Header */}
        <div style={{
          padding: '20px 24px',
          borderBottom: '1px solid rgba(255, 255, 255, 0.05)',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          background: 'rgba(255,255,255,0.01)'
        }}>
          <h2 style={{ fontSize: '24px', fontWeight: 800 }}>Detalhes do Filme</h2>
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
            <X size={20} />
          </button>
        </div>

        {/* Scrollable Content Container */}
        <div style={{
          overflowY: 'auto',
          padding: '24px',
          display: 'grid',
          gridTemplateColumns: '1fr',
          gap: '24px'
        }}>
          
          {/* Top Panel: Poster and Info */}
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'window.innerWidth > 600 ? "240px 1fr" : "1fr"',
            gap: '24px',
            alignItems: 'start',
            '@media(min-width: 600px)': {
              gridTemplateColumns: '240px 1fr'
            }
          }} className="modal-grid-top">
            
            {/* Poster */}
            <div style={{
              width: '100%',
              maxWidth: '240px',
              margin: '0 auto',
              borderRadius: 'var(--border-radius-sm)',
              overflow: 'hidden',
              boxShadow: '0 10px 25px rgba(0,0,0,0.5)',
              border: '1px solid rgba(255,255,255,0.05)'
            }}>
              <img 
                src={filme.imagemUrl || "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=500&auto=format&fit=crop"} 
                alt={filme.titulo}
                style={{ width: '100%', display: 'block', objectFit: 'cover', minHeight: '320px' }}
                onError={(e) => {
                  e.target.src = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=500&auto=format&fit=crop";
                }}
              />
            </div>

            {/* Info and Synopsis */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div>
                <h1 style={{ fontSize: '28px', fontWeight: 800, marginBottom: '8px', color: '#fff' }}>
                  {filme.titulo}
                </h1>
                
                {/* Meta row */}
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '12px', marginBottom: '16px' }}>
                  {filme.genero && (
                    <span className="glass-panel" style={{
                      fontSize: '12px',
                      padding: '4px 10px',
                      borderRadius: '50px',
                      color: 'var(--accent-hover)',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '4px',
                      background: 'rgba(58, 134, 200, 0.08)'
                    }}>
                      <Tag size={12} />
                      {filme.genero}
                    </span>
                  )}
                  <span className="glass-panel" style={{
                    fontSize: '12px',
                    padding: '4px 10px',
                    borderRadius: '50px',
                    color: 'var(--text-secondary)',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px'
                  }}>
                    <Calendar size={12} />
                    {filme.ano}
                  </span>
                  <span className="glass-panel" style={{
                    fontSize: '12px',
                    padding: '4px 10px',
                    borderRadius: '50px',
                    color: 'var(--text-secondary)',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px'
                  }}>
                    <User size={12} />
                    Dirigido por: {filme.diretor || 'Desconhecido'}
                  </span>
                </div>
              </div>

              {/* Synopsis */}
              <div>
                <h4 style={{ color: 'var(--text-secondary)', fontSize: '14px', textTransform: 'uppercase', letterSpacing: '1px', marginBottom: '6px' }}>Sinopse</h4>
                <p style={{ color: 'var(--text-secondary)', fontSize: '15px', lineHeight: '1.6' }}>
                  {filme.sinopse || "Sem sinopse disponível para este filme."}
                </p>
              </div>

              {/* Rating metrics */}
              <div style={{
                display: 'flex',
                alignItems: 'center',
                gap: '24px',
                padding: '16px',
                background: 'rgba(255, 255, 255, 0.02)',
                borderRadius: 'var(--border-radius-sm)',
                border: '1px solid rgba(255,255,255,0.03)',
                marginTop: '8px'
              }}>
                <div>
                  <span style={{ fontSize: '12px', color: 'var(--text-muted)', display: 'block', textTransform: 'uppercase', marginBottom: '4px' }}>Nota Média</span>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Star size={24} style={{ color: 'var(--star-color)', fill: 'var(--star-color)' }} />
                    <span style={{ fontSize: '28px', fontWeight: 800, color: '#fff' }}>{notaMediaLocal.toFixed(1)}</span>
                  </div>
                </div>
                
                <div style={{ width: '1px', height: '40px', background: 'rgba(255,255,255,0.08)' }}></div>
                
                <div>
                  <span style={{ fontSize: '12px', color: 'var(--text-muted)', display: 'block', textTransform: 'uppercase', marginBottom: '4px' }}>Avaliações</span>
                  <span style={{ fontSize: '24px', fontWeight: 700, color: 'var(--text-primary)' }}>{totalAvaliacoesLocal}</span>
                </div>
              </div>

            </div>

          </div>

          {/* Messages alert */}
          {submitMessage.text && (
            <div className="glass-panel" style={{
              padding: '12px 16px',
              borderRadius: 'var(--border-radius-sm)',
              backgroundColor: submitMessage.type === 'success' ? 'rgba(6, 214, 160, 0.15)' : 'rgba(239, 71, 111, 0.15)',
              borderColor: submitMessage.type === 'success' ? 'var(--success)' : 'var(--danger)',
              color: submitMessage.type === 'success' ? 'var(--success)' : 'var(--danger)',
              fontSize: '14px',
              fontWeight: 500,
              textAlign: 'center'
            }}>
              {submitMessage.text}
            </div>
          )}

          {/* Interactive Rating Form */}
          <div className="glass-panel" style={{ padding: '20px', display: 'flex', flexWrap: 'wrap', gap: '16px', alignItems: 'center', justifyContent: 'space-between' }}>
            <div>
              <h3 style={{ fontSize: '16px', fontWeight: 700, marginBottom: '4px' }}>Sua Avaliação</h3>
              <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
                {usuario ? 'Clique em uma estrela para registrar sua nota' : 'Faça login para avaliar este filme'}
              </p>
            </div>
            
            <div className="star-rating">
              {[1, 2, 3, 4, 5].map((star) => (
                <Star
                  key={star}
                  size={32}
                  className={`star ${star <= userRating ? 'active' : ''}`}
                  style={{
                    cursor: usuario ? 'pointer' : 'not-allowed',
                    fill: star <= userRating ? 'var(--star-color)' : 'none'
                  }}
                  onClick={() => handleRate(star)}
                />
              ))}
            </div>
          </div>

          {/* Comments Section */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <h3 style={{ fontSize: '18px', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '8px' }}>
              <MessageSquare size={20} color="var(--primary)" />
              Comentários ({comentarios.length})
            </h3>

            {/* Comment Form */}
            {usuario ? (
              <form onSubmit={handleAddComment} style={{ display: 'flex', gap: '12px' }}>
                <input
                  type="text"
                  placeholder="Escreva um comentário sobre o filme..."
                  value={novoComentario}
                  onChange={(e) => setNovoComentario(e.target.value)}
                  className="form-input"
                  style={{ flexGrow: 1 }}
                />
                <button type="submit" className="btn-primary" style={{ padding: '12px 20px', flexShrink: 0 }}>
                  <Send size={16} />
                  <span>Enviar</span>
                </button>
              </form>
            ) : (
              <div className="glass-panel" style={{ padding: '14px', textAlign: 'center', fontSize: '14px', color: 'var(--text-secondary)', background: 'rgba(255,255,255,0.01)' }}>
                Você precisa estar logado para comentar.
              </div>
            )}

            {/* Comments List */}
            <div 
              className="glass-panel" 
              style={{
                maxHeight: '300px',
                overflowY: 'auto',
                padding: '16px',
                display: 'flex',
                flexDirection: 'column',
                gap: '12px',
                background: 'rgba(10, 11, 14, 0.4)'
              }}
            >
              {isLoadingComments ? (
                <div style={{ textAlign: 'center', padding: '20px', color: 'var(--text-secondary)' }}>Carregando comentários...</div>
              ) : comentarios.length > 0 ? (
                comentarios.map((c) => (
                  <div 
                    key={c.id} 
                    style={{
                      padding: '12px',
                      borderRadius: 'var(--border-radius-sm)',
                      background: 'rgba(255,255,255,0.02)',
                      border: '1px solid rgba(255,255,255,0.03)'
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                      <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--primary)' }}>@{c.username}</span>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
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
                              opacity: 0.8,
                              transition: 'opacity 0.2s, background-color 0.2s',
                            }}
                            title="Excluir comentário"
                            onMouseEnter={(e) => { e.currentTarget.style.opacity = 1; e.currentTarget.style.backgroundColor = 'rgba(239, 71, 111, 0.1)'; }}
                            onMouseLeave={(e) => { e.currentTarget.style.opacity = 0.8; e.currentTarget.style.backgroundColor = 'transparent'; }}
                          >
                            <Trash2 size={14} />
                          </button>
                        )}
                      </div>
                    </div>
                    <p style={{ fontSize: '14px', color: 'var(--text-primary)', lineHeight: '1.4' }}>{c.texto}</p>
                  </div>
                ))
              ) : (
                <div style={{
                  padding: '30px 10px',
                  textAlign: 'center',
                  color: 'var(--text-muted)',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  gap: '8px'
                }}>
                  <MessageCircle size={32} />
                  <span>Seja o primeiro a comentar neste filme!</span>
                </div>
              )}
            </div>

          </div>

        </div>
      </div>
    </div>
  );
}
