import React, { useState, useEffect } from 'react';
import { Calendar, Clock, Trash2, CheckCircle, Eye, Loader2, ArrowUpDown, Film, Award } from 'lucide-react';
import MovieDetailModal from '../components/MovieDetailModal';

export default function Watchlist({ usuario }) {
  const [watchlistFilmes, setWatchlistFilmes] = useState([]);
  const [stats, setStats] = useState({
    totalWatchlist: 0,
    ultimoAdicionado: '-',
    totalAssistidos: 0
  });
  const [isLoading, setIsLoading] = useState(true);
  const [ordem, setOrdem] = useState('recentes'); // 'recentes', 'antigos', 'alfabetica'
  const [selectedMovie, setSelectedMovie] = useState(null);
  const [isLoadingDetail, setIsLoadingDetail] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const fetchDados = async () => {
    setIsLoading(true);
    setErrorMessage('');
    try {
      // 1. Fetch Watchlist Movies
      const wlResponse = await fetch('/api/watchlist', {
        headers: {
          'Authorization': `Bearer ${usuario.token}`
        }
      });
      let wlData = [];
      if (wlResponse.ok) {
        wlData = await wlResponse.json();
        setWatchlistFilmes(wlData);
      }

      // 2. Fetch Watchlist stats (total and last added)
      const wlStatsResponse = await fetch('/api/watchlist/total', {
        headers: {
          'Authorization': `Bearer ${usuario.token}`
        }
      });
      let totalWl = 0;
      let ultimo = '-';
      if (wlStatsResponse.ok) {
        const wlStatsData = await wlStatsResponse.json();
        totalWl = wlStatsData.total;
        ultimo = wlStatsData.ultimoAdicionado || '-';
      }

      // 3. Fetch Diary stats (total watched movies)
      const diaryStatsResponse = await fetch('/api/diario/estatisticas', {
        headers: {
          'Authorization': `Bearer ${usuario.token}`
        }
      });
      let totalWatched = 0;
      if (diaryStatsResponse.ok) {
        const diaryStatsData = await diaryStatsResponse.json();
        totalWatched = diaryStatsData.totalAssistidos;
      }

      setStats({
        totalWatchlist: totalWl,
        ultimoAdicionado: ultimo,
        totalAssistidos: totalWatched
      });
    } catch (err) {
      console.error("Erro ao carregar dados da watchlist:", err);
      setErrorMessage("Erro ao conectar com o servidor.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchDados();
  }, [usuario]);

  // Handle Quick Remove from Watchlist
  const handleQuickRemove = async (e, filmeId, titulo) => {
    e.stopPropagation(); // prevent opening the modal
    if (!window.confirm(`Deseja remover "${titulo}" da sua Watchlist?`)) return;

    try {
      const response = await fetch(`/api/watchlist/filmes/${filmeId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${usuario.token}`
        }
      });

      if (response.ok) {
        // Update local state without full reload
        setWatchlistFilmes(prev => prev.filter(item => item.filmeId !== filmeId));
        setStats(prev => ({
          ...prev,
          totalWatchlist: prev.totalWatchlist - 1
        }));
        
        // Notify other pages (Home badges etc.)
        window.dispatchEvent(new Event('storage'));
      } else {
        alert("Erro ao remover filme da watchlist.");
      }
    } catch (err) {
      console.error("Erro ao remover da watchlist:", err);
    }
  };

  // Handle Quick Mark as Watched (removes from Watchlist and adds to Diary)
  const handleQuickMarkWatched = async (e, filmeId, titulo) => {
    e.stopPropagation(); // prevent opening the modal
    
    // As marking watched requires an optional description, we can prompt or just mark directly.
    // Let's prompt for an optional observation to make it premium!
    const obs = prompt(`Você assistiu "${titulo}"! Adicione uma observação rápida (opcional):`, "");
    if (obs === null) return; // user cancelled

    try {
      const response = await fetch(`/api/diario/filmes/${filmeId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${usuario.token}`
        },
        body: JSON.stringify({ observacao: obs })
      });

      if (response.ok) {
        // Mark Watched automatically deletes it from watchlist in backend!
        // So we update the local states
        setWatchlistFilmes(prev => prev.filter(item => item.filmeId !== filmeId));
        setStats(prev => ({
          ...prev,
          totalWatchlist: prev.totalWatchlist - 1,
          totalAssistidos: prev.totalAssistidos + 1,
          // Recalculating the last added is complex local, so we just let it be or update via fetchDados.
        }));
        
        // Notify other pages (Home badges etc.)
        window.dispatchEvent(new Event('storage'));
      } else {
        alert("Erro ao marcar filme como assistido.");
      }
    } catch (err) {
      console.error("Erro ao marcar como assistido:", err);
    }
  };

  // Sort function based on the selected criteria
  const getSortedMovies = () => {
    const sorted = [...watchlistFilmes];
    if (ordem === 'recentes') {
      return sorted.sort((a, b) => new Date(b.dataAdicao) - new Date(a.dataAdicao));
    }
    if (ordem === 'antigos') {
      return sorted.sort((a, b) => new Date(a.dataAdicao) - new Date(b.dataAdicao));
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
        alert("Erro ao carregar detalhes do filme.");
      }
    } catch (err) {
      console.error("Erro ao obter detalhes do filme:", err);
    } finally {
      setIsLoadingDetail(false);
    }
  };

  const sortedMovies = getSortedMovies();

  const formatDate = (isoString) => {
    try {
      const date = new Date(isoString);
      return date.toLocaleDateString('pt-BR');
    } catch (e) {
      return 'N/A';
    }
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
          <Clock size={28} color="var(--accent)" />
          Minha Watchlist
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '14px', marginTop: '4px' }}>
          Filmes que você quer assistir. Quando marcar um filme como "Assistido", ele será migrado automaticamente para o seu diário.
        </p>
      </section>

      {/* Error alert */}
      {errorMessage && (
        <div className="glass-panel" style={{
          padding: '12px',
          marginBottom: '20px',
          backgroundColor: 'rgba(255, 51, 102, 0.08)',
          color: 'var(--danger)',
          borderColor: 'var(--danger)',
          textAlign: 'center',
          fontSize: '14px'
        }}>
          {errorMessage}
        </div>
      )}

      {/* Loading Spinner */}
      {isLoading ? (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '100px 0', gap: '16px' }}>
          <Loader2 className="animate-spin" size={36} color="var(--primary)" />
          <span style={{ color: 'var(--text-secondary)', fontSize: '15px' }}>Carregando sua watchlist...</span>
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
            {/* Stat Item 1: Filmes na Watchlist */}
            <div className="glass-panel" style={{
              padding: '24px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              backgroundColor: 'rgba(28, 37, 45, 0.4)',
              borderLeft: '4px solid var(--accent)'
            }}>
              <div>
                <span style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Na Watchlist</span>
                <h2 style={{ fontSize: '36px', fontWeight: 800, color: '#fff', marginTop: '4px', lineHeight: 1 }}>{stats.totalWatchlist}</h2>
              </div>
              <div style={{ background: 'rgba(255, 128, 0, 0.08)', borderRadius: '12px', padding: '12px' }}>
                <Clock size={26} color="var(--accent)" />
              </div>
            </div>

            {/* Stat Item 2: Último Filme Adicionado */}
            <div className="glass-panel" style={{
              padding: '24px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              backgroundColor: 'rgba(28, 37, 45, 0.4)',
              borderLeft: '4px solid #33a6ff'
            }}>
              <div style={{ maxWidth: '70%' }}>
                <span style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Último Adicionado</span>
                <h4 style={{ 
                  fontSize: '16px', 
                  fontWeight: 800, 
                  color: '#fff', 
                  marginTop: '8px',
                  whiteSpace: 'nowrap',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis' 
                }} title={stats.ultimoAdicionado}>
                  {stats.ultimoAdicionado}
                </h4>
              </div>
              <div style={{ background: 'rgba(51, 166, 255, 0.08)', borderRadius: '12px', padding: '12px' }}>
                <Film size={26} color="#33a6ff" />
              </div>
            </div>

            {/* Stat Item 3: Filmes Assistidos */}
            <div className="glass-panel" style={{
              padding: '24px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              backgroundColor: 'rgba(28, 37, 45, 0.4)',
              borderLeft: '4px solid var(--primary)'
            }}>
              <div>
                <span style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Filmes Assistidos</span>
                <h2 style={{ fontSize: '36px', fontWeight: 800, color: '#fff', marginTop: '4px', lineHeight: 1 }}>{stats.totalAssistidos}</h2>
              </div>
              <div style={{ background: 'rgba(0, 224, 84, 0.08)', borderRadius: '12px', padding: '12px' }}>
                <Award size={26} color="var(--primary)" />
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
              {watchlistFilmes.length === 0 
                ? 'Nenhum filme salvo para assistir' 
                : `${watchlistFilmes.length} ${watchlistFilmes.length === 1 ? 'filme pendente' : 'filmes pendentes'}`
              }
            </span>

            {watchlistFilmes.length > 0 && (
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
                  <option value="recentes">Mais recentes adicionados</option>
                  <option value="antigos">Mais antigos adicionados</option>
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

          {/* Watchlist Movies Grid */}
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

                    {/* Quick Action Badges (Top right overlay) */}
                    <div style={{
                      position: 'absolute',
                      top: '10px',
                      right: '10px',
                      display: 'flex',
                      gap: '6px',
                      zIndex: 10
                    }}>
                      {/* Mark Watched Button */}
                      <button
                        onClick={(e) => handleQuickMarkWatched(e, item.filmeId, item.titulo)}
                        style={{
                          backgroundColor: 'rgba(0, 224, 84, 0.95)',
                          border: 'none',
                          borderRadius: '50%',
                          width: '28px',
                          height: '28px',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          color: '#0c0e12',
                          cursor: 'pointer',
                          boxShadow: '0 2px 8px rgba(0,0,0,0.5)',
                          transition: 'transform 0.15s ease'
                        }}
                        onMouseEnter={(e) => { e.currentTarget.style.transform = 'scale(1.15)'; }}
                        onMouseLeave={(e) => { e.currentTarget.style.transform = 'scale(1.0)'; }}
                        title="Marcar como Assistido"
                      >
                        <Eye size={14} />
                      </button>

                      {/* Remove Button */}
                      <button
                        onClick={(e) => handleQuickRemove(e, item.filmeId, item.titulo)}
                        style={{
                          backgroundColor: 'rgba(255, 51, 102, 0.95)',
                          border: 'none',
                          borderRadius: '50%',
                          width: '28px',
                          height: '28px',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          color: '#fff',
                          cursor: 'pointer',
                          boxShadow: '0 2px 8px rgba(0,0,0,0.5)',
                          transition: 'transform 0.15s ease'
                        }}
                        onMouseEnter={(e) => { e.currentTarget.style.transform = 'scale(1.15)'; }}
                        onMouseLeave={(e) => { e.currentTarget.style.transform = 'scale(1.0)'; }}
                        title="Remover da Watchlist"
                      >
                        <Trash2 size={13} />
                      </button>
                    </div>
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

                    {/* Added Date */}
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--text-muted)', fontSize: '11px', marginTop: 'auto' }}>
                      <Calendar size={12} />
                      <span>Salvo em {formatDate(item.dataAdicao)}</span>
                    </div>
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
              <Clock size={44} color="var(--text-muted)" />
              <h3 style={{ fontSize: '18px', fontWeight: 600, color: 'var(--text-secondary)', letterSpacing: '-0.3px' }}>Sua Watchlist está vazia</h3>
              <p style={{ fontSize: '14px', maxWidth: '400px', margin: '0 auto', lineHeight: '1.5' }}>
                Você ainda não adicionou nenhum filme para assistir futuramente. Explore o catálogo principal e clique em "Quero Assistir" nos detalhes dos filmes que deseja salvar!
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
            fetchDados(); // Reload to sync state changes
          }} 
        />
      )}
    </main>
  );
}
