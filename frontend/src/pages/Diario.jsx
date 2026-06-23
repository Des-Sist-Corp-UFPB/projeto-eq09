import React, { useState, useEffect } from 'react';
import { Calendar, Star, Film, Eye, Award, MessageSquare, ArrowUpDown, Loader2 } from 'lucide-react';
import MovieDetailModal from '../components/MovieDetailModal';

export default function Diario({ usuario }) {
  const [diarioFilmes, setDiarioFilmes] = useState([]);
  const [estatisticas, setEstatisticas] = useState({
    totalAssistidos: 0,
    totalAvaliacoes: 0,
    notaMedia: 0.0
  });
  const [isLoading, setIsLoading] = useState(true);
  const [ordem, setOrdem] = useState('recentes'); // 'recentes', 'antigos', 'alfabetica'
  const [selectedMovie, setSelectedMovie] = useState(null);
  const [isLoadingDetail, setIsLoadingDetail] = useState(false);

  const fetchDiarioDados = async () => {
    setIsLoading(true);
    try {
      // 1. Fetch Diary Movies
      const diaryResponse = await fetch('/api/diario', {
        headers: {
          'Authorization': `Bearer ${usuario.token}`
        }
      });
      let diaryData = [];
      if (diaryResponse.ok) {
        diaryData = await diaryResponse.json();
        setDiarioFilmes(diaryData);
      }

      // 2. Fetch User Stats
      const statsResponse = await fetch('/api/diario/estatisticas', {
        headers: {
          'Authorization': `Bearer ${usuario.token}`
        }
      });
      if (statsResponse.ok) {
        const statsData = await statsResponse.json();
        setEstatisticas(statsData);
      }
    } catch (err) {
      console.error("Erro ao carregar dados do diário:", err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchDiarioDados();
  }, [usuario]);

  // Sort function based on the selected criteria
  const getSortedMovies = () => {
    const sorted = [...diarioFilmes];
    if (ordem === 'recentes') {
      return sorted.sort((a, b) => new Date(b.dataAssistido) - new Date(a.dataAssistido));
    }
    if (ordem === 'antigos') {
      return sorted.sort((a, b) => new Date(a.dataAssistido) - new Date(b.dataAssistido));
    }
    if (ordem === 'alfabetica') {
      return sorted.sort((a, b) => a.titulo.localeCompare(b.titulo));
    }
    return sorted;
  };

  const handleMovieClick = async (filmeId) => {
    setIsLoadingDetail(true);
    try {
      const response = await fetch(`/api/filmes/${filmeId}`);
      if (response.ok) {
        const movieDetails = await response.json();
        setSelectedMovie(movieDetails);
      } else {
        alert("Erro ao buscar detalhes do filme.");
      }
    } catch (err) {
      console.error("Erro ao obter detalhes do filme:", err);
    } finally {
      setIsLoadingDetail(false);
    }
  };

  const sortedMovies = getSortedMovies();

  // Helper to format Iso Instant Date to pt-BR
  const formatDate = (isoString) => {
    try {
      const date = new Date(isoString);
      return date.toLocaleDateString('pt-BR');
    } catch (e) {
      return 'N/A';
    }
  };

  // Helper to render stars rating
  const renderStars = (nota) => {
    if (!nota) return null;
    return (
      <div style={{ display: 'flex', gap: '2px', color: 'var(--star-color)' }}>
        {[1, 2, 3, 4, 5].map((star) => (
          <Star 
            key={star} 
            size={12} 
            fill={star <= nota ? 'var(--star-color)' : 'none'} 
            stroke="var(--star-color)"
          />
        ))}
      </div>
    );
  };

  return (
    <main className="container animate-fade-in" style={{ paddingBottom: '40px' }}>
      
      {/* Header Title */}
      <section style={{ padding: '24px 0 16px 0', borderBottom: '1px solid rgba(255, 255, 255, 0.04)', marginBottom: '32px' }}>
        <h1 style={{
          fontSize: '28px',
          fontWeight: 800,
          color: '#fff',
          letterSpacing: '-0.5px',
          display: 'flex',
          alignItems: 'center',
          gap: '12px'
        }}>
          <Eye size={28} color="var(--primary)" />
          Meu Diário de Filmes
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '14px', marginTop: '4px' }}>
          Seu histórico pessoal de cinema. Veja os filmes que você assistiu, suas notas e anotações.
        </p>
      </section>

      {/* Loading Spinner */}
      {isLoading ? (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '100px 0', gap: '16px' }}>
          <Loader2 className="animate-spin" size={36} color="var(--primary)" />
          <span style={{ color: 'var(--text-secondary)', fontSize: '15px' }}>Carregando seu diário...</span>
        </div>
      ) : (
        <>
          {/* Stats Grid Dashboard */}
          <section style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
            gap: '20px',
            marginBottom: '40px'
          }}>
            {/* Stat Item 1: Filmes Assistidos */}
            <div className="glass-panel" style={{
              padding: '24px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              backgroundColor: 'rgba(28, 37, 45, 0.4)',
              borderLeft: '4px solid var(--primary)'
            }}>
              <div>
                <span style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Assistidos</span>
                <h2 style={{ fontSize: '36px', fontWeight: 800, color: '#fff', marginTop: '4px', lineHeight: 1 }}>{estatisticas.totalAssistidos}</h2>
              </div>
              <div style={{ background: 'rgba(0, 224, 84, 0.08)', borderRadius: '12px', padding: '12px' }}>
                <Film size={26} color="var(--primary)" />
              </div>
            </div>

            {/* Stat Item 2: Total Avaliacoes */}
            <div className="glass-panel" style={{
              padding: '24px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              backgroundColor: 'rgba(28, 37, 45, 0.4)',
              borderLeft: '4px solid var(--accent)'
            }}>
              <div>
                <span style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Avaliações</span>
                <h2 style={{ fontSize: '36px', fontWeight: 800, color: '#fff', marginTop: '4px', lineHeight: 1 }}>{estatisticas.totalAvaliacoes}</h2>
              </div>
              <div style={{ background: 'rgba(255, 128, 0, 0.08)', borderRadius: '12px', padding: '12px' }}>
                <Star size={26} color="var(--accent)" fill="var(--accent)" />
              </div>
            </div>

            {/* Stat Item 3: Nota Media */}
            <div className="glass-panel" style={{
              padding: '24px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              backgroundColor: 'rgba(28, 37, 45, 0.4)',
              borderLeft: '4px solid #33a6ff'
            }}>
              <div>
                <span style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Nota Média</span>
                <h2 style={{ fontSize: '36px', fontWeight: 800, color: '#fff', marginTop: '4px', lineHeight: 1 }}>
                  {estatisticas.notaMedia > 0 ? estatisticas.notaMedia.toFixed(1) : '-'}
                </h2>
              </div>
              <div style={{ background: 'rgba(51, 166, 255, 0.08)', borderRadius: '12px', padding: '12px' }}>
                <Award size={26} color="#33a6ff" />
              </div>
            </div>
          </section>

          {/* Filters Bar */}
          <section style={{
            display: 'flex',
            flexDirection: 'row',
            justifyContent: 'space-between',
            alignItems: 'center',
            flexWrap: 'wrap',
            gap: '16px',
            marginBottom: '24px',
            paddingBottom: '16px',
            borderBottom: '1px solid rgba(255, 255, 255, 0.03)'
          }}>
            <span style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-secondary)' }}>
              {diarioFilmes.length === 0 
                ? 'Nenhum filme no diário' 
                : `${diarioFilmes.length} ${diarioFilmes.length === 1 ? 'filme registrado' : 'filmes registrados'}`
              }
            </span>

            {diarioFilmes.length > 0 && (
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <ArrowUpDown size={14} color="var(--text-muted)" />
                <span style={{ fontSize: '13px', color: 'var(--text-secondary)', fontWeight: 600 }}>Ordenar por:</span>
                <select
                  value={ordem}
                  onChange={(e) => setOrdem(e.target.value)}
                  style={{
                    backgroundColor: 'var(--bg-secondary)',
                    color: 'var(--text-primary)',
                    border: '1px solid var(--glass-border)',
                    borderRadius: 'var(--border-radius-sm)',
                    padding: '6px 12px',
                    fontSize: '13px',
                    fontWeight: 600,
                    outline: 'none',
                    cursor: 'pointer'
                  }}
                >
                  <option value="recentes">Mais recentes assistidos</option>
                  <option value="antigos">Mais antigos assistidos</option>
                  <option value="alfabetica">Ordem alfabética</option>
                </select>
              </div>
            )}
          </section>

          {/* Loading Detail Spinner Indicator overlay */}
          {isLoadingDetail && (
            <div style={{
              position: 'fixed',
              top: '20px',
              right: '20px',
              zIndex: 2000,
              backgroundColor: 'var(--bg-secondary)',
              border: '1px solid var(--primary)',
              borderRadius: 'var(--border-radius-sm)',
              padding: '10px 16px',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              boxShadow: '0 4px 15px rgba(0,0,0,0.5)'
            }}>
              <Loader2 className="animate-spin" size={16} color="var(--primary)" />
              <span style={{ fontSize: '13px', color: '#fff', fontWeight: 600 }}>Carregando detalhes...</span>
            </div>
          )}

          {/* Diary Movies Grid */}
          {sortedMovies.length > 0 ? (
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))',
              gap: '24px'
            }}>
              {sortedMovies.map((item) => (
                <div 
                  key={item.id}
                  onClick={() => handleMovieClick(item.filmeId)}
                  className="glass-panel glass-panel-hover"
                  style={{
                    cursor: 'pointer',
                    display: 'flex',
                    flexDirection: 'column',
                    overflow: 'hidden',
                    backgroundColor: 'var(--bg-secondary)',
                    borderRadius: 'var(--border-radius-md)',
                    border: '1px solid rgba(255, 255, 255, 0.03)',
                    height: '100%',
                    position: 'relative'
                  }}
                >
                  {/* Poster Image */}
                  <div style={{ width: '100%', aspectRatio: '2/3', overflow: 'hidden', position: 'relative' }}>
                    <img 
                      src={item.imagemUrl || "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=500&auto=format&fit=crop"} 
                      alt={item.titulo}
                      style={{ width: '100%', height: '100%', objectFit: 'cover', transition: 'transform 0.3s ease' }}
                      onMouseEnter={(e) => { e.currentTarget.style.transform = 'scale(1.05)'; }}
                      onMouseLeave={(e) => { e.currentTarget.style.transform = 'scale(1.0)'; }}
                      onError={(e) => {
                        e.target.src = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=500&auto=format&fit=crop";
                      }}
                    />
                    
                    {/* Hover Observation Overlay */}
                    {item.observacao && (
                      <div 
                        className="obs-overlay"
                        style={{
                          position: 'absolute',
                          top: 0,
                          left: 0,
                          right: 0,
                          bottom: 0,
                          backgroundColor: 'rgba(20, 24, 28, 0.9)',
                          opacity: 0,
                          transition: 'opacity 0.2s ease',
                          display: 'flex',
                          flexDirection: 'column',
                          justifyContent: 'center',
                          padding: '16px',
                          color: 'var(--text-secondary)',
                          fontSize: '11px',
                          lineHeight: '1.4',
                          textAlign: 'center',
                          overflowY: 'auto'
                        }}
                        onMouseEnter={(e) => { e.currentTarget.style.opacity = 1; }}
                        onMouseLeave={(e) => { e.currentTarget.style.opacity = 0; }}
                      >
                        <MessageSquare size={14} color="var(--primary)" style={{ margin: '0 auto 6px auto' }} />
                        <p style={{ fontStyle: 'italic' }}>"{item.observacao}"</p>
                      </div>
                    )}
                  </div>

                  {/* Movie Info */}
                  <div style={{ padding: '12px', display: 'flex', flexDirection: 'column', gap: '8px', flexGrow: 1 }}>
                    <h4 style={{
                      fontSize: '14px',
                      fontWeight: 700,
                      color: '#fff',
                      margin: 0,
                      lineHeight: '1.25',
                      whiteSpace: 'nowrap',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis'
                    }} title={item.titulo}>
                      {item.titulo}
                    </h4>

                    {/* Marked Date */}
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--text-muted)', fontSize: '11px' }}>
                      <Calendar size={12} />
                      <span>{formatDate(item.dataAssistido)}</span>
                    </div>

                    {/* Star Rating if exists */}
                    {item.notaAvaliacao && (
                      <div style={{ marginTop: 'auto', paddingTop: '4px' }}>
                        {renderStars(item.notaAvaliacao)}
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="glass-panel" style={{
              padding: '60px 24px',
              textAlign: 'center',
              color: 'var(--text-muted)',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: '16px',
              backgroundColor: 'var(--bg-secondary)',
              border: '1px solid rgba(255, 255, 255, 0.03)',
              marginTop: '16px'
            }}>
              <Film size={44} color="var(--text-muted)" />
              <h3 style={{ fontSize: '18px', fontWeight: 600, color: 'var(--text-secondary)', letterSpacing: '-0.3px' }}>Seu diário está em branco</h3>
              <p style={{ fontSize: '14px', maxWidth: '400px', margin: '0 auto', lineHeight: '1.5' }}>
                Você ainda não marcou nenhum filme como assistido. Explore o catálogo principal, clique nos detalhes de um filme e selecione "Marcar como assistido" para começar o seu histórico!
              </p>
            </div>
          )}
        </>
      )}

      {/* Movie Details Modal */}
      {selectedMovie && (
        <MovieDetailModal 
          filme={selectedMovie} 
          usuario={usuario} 
          onClose={() => {
            setSelectedMovie(null);
            fetchDiarioDados(); // Reload to refresh ratings/watched flags
          }} 
        />
      )}
    </main>
  );
}
