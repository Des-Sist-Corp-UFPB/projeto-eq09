import React, { useState, useEffect } from 'react';
import { ArrowLeft, Edit2, Upload, Link, Heart, Film, Loader2, X } from 'lucide-react';
import MovieCard from '../components/MovieCard';
import MovieDetailModal from '../components/MovieDetailModal';

export default function Profile({ usuario, onNavigate }) {
  const [profile, setProfile] = useState({
    foto: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200&auto=format&fit=crop',
    bio: 'Nenhuma biografia adicionada ainda.',
    favoritos: []
  });

  const [isEditingBio, setIsEditingBio] = useState(false);
  const [editedBio, setEditedBio] = useState('');
  
  const [isEditingFoto, setIsEditingFoto] = useState(false);
  const [fotoUrlInput, setFotoUrlInput] = useState('');
  const [isUploading, setIsUploading] = useState(false);
  const [uploadError, setUploadError] = useState('');

  const [allMovies, setAllMovies] = useState([]);
  const [favoriteMovies, setFavoriteMovies] = useState([]);
  const [isLoadingMovies, setIsLoadingMovies] = useState(false);
  const [selectedMovie, setSelectedMovie] = useState(null);

  // Load profile from localStorage and fetch movies
  const loadProfileData = () => {
    const saved = localStorage.getItem(`profile_${usuario.username}`);
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        setProfile(prev => ({
          ...prev,
          foto: parsed.foto || prev.foto,
          bio: parsed.bio !== undefined ? parsed.bio : prev.bio,
          favoritos: parsed.favoritos || []
        }));
      } catch (e) {
        console.error("Erro ao carregar perfil:", e);
      }
    }
  };

  useEffect(() => {
    loadProfileData();
    fetchMovies();

    window.addEventListener('storage', loadProfileData);
    return () => window.removeEventListener('storage', loadProfileData);
  }, [usuario.username]);

  const fetchMovies = async () => {
    setIsLoadingMovies(true);
    try {
      const response = await fetch('/api/filmes');
      if (response.ok) {
        const data = await response.json();
        setAllMovies(data);
      }
    } catch (e) {
      console.error("Erro ao buscar filmes para favoritos:", e);
    } finally {
      setIsLoadingMovies(false);
    }
  };

  // Filter movies when allMovies or profile favorites change
  useEffect(() => {
    if (allMovies.length > 0) {
      const filtered = allMovies.filter(movie => profile.favoritos.includes(movie.id));
      setFavoriteMovies(filtered);
    } else {
      setFavoriteMovies([]);
    }
  }, [allMovies, profile.favoritos]);

  const handleSaveBio = () => {
    const updatedProfile = { ...profile, bio: editedBio };
    setProfile(updatedProfile);
    localStorage.setItem(`profile_${usuario.username}`, JSON.stringify(updatedProfile));
    setIsEditingBio(false);
  };

  const handleSaveFoto = (url) => {
    const finalUrl = url.trim() || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200&auto=format&fit=crop';
    const updatedProfile = { ...profile, foto: finalUrl };
    setProfile(updatedProfile);
    localStorage.setItem(`profile_${usuario.username}`, JSON.stringify(updatedProfile));
    
    // Also notify other components/navbar by updating user info cache if needed
    window.dispatchEvent(new Event('storage'));
    
    setIsEditingFoto(false);
    setFotoUrlInput('');
  };

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setIsUploading(true);
    setUploadError('');
    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await fetch('/api/upload', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${usuario.token}`
        },
        body: formData
      });

      if (response.ok) {
        const data = await response.json();
        handleSaveFoto(data.url);
      } else {
        const errData = await response.json().catch(() => ({}));
        setUploadError(errData.error || 'Erro ao fazer upload da foto.');
      }
    } catch (err) {
      setUploadError('Erro de conexão ao servidor.');
    } finally {
      setIsUploading(false);
    }
  };

  const handleRemoveFavorite = (movieId, e) => {
    e.stopPropagation(); // Prevent opening modal
    const updatedFavs = profile.favoritos.filter(id => id !== movieId);
    const updatedProfile = { ...profile, favoritos: updatedFavs };
    setProfile(updatedProfile);
    localStorage.setItem(`profile_${usuario.username}`, JSON.stringify(updatedProfile));
  };

  return (
    <main className="container animate-fade-in" style={{ paddingBottom: '60px', paddingTop: '10px' }}>
      
      {/* Back Button */}
      <button 
        onClick={() => onNavigate('home')}
        className="btn-secondary"
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          padding: '8px 16px',
          fontSize: '13px',
          marginBottom: '24px',
          border: '1px solid rgba(255, 255, 255, 0.05)',
          borderRadius: 'var(--border-radius-sm)',
          background: 'rgba(255, 255, 255, 0.02)'
        }}
      >
        <ArrowLeft size={14} />
        <span>Voltar ao catálogo</span>
      </button>

      {/* Profile Card Header */}
      <section className="glass-panel" style={{
        padding: '32px',
        backgroundColor: 'var(--bg-secondary)',
        border: '1px solid rgba(255, 255, 255, 0.04)',
        display: 'flex',
        flexWrap: 'wrap',
        gap: '32px',
        alignItems: 'center',
        marginBottom: '40px'
      }}>
        {/* Profile Avatar */}
        <div style={{ position: 'relative', width: '130px', height: '130px', margin: '0 auto' }}>
          <img 
            src={profile.foto} 
            alt={usuario.username} 
            style={{
              width: '100%',
              height: '100%',
              borderRadius: '50%',
              objectFit: 'cover',
              border: '3px solid var(--primary)',
              boxShadow: '0 0 15px var(--primary-glow)'
            }}
            onError={(e) => {
              e.target.src = 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200&auto=format&fit=crop';
            }}
          />
          <button 
            onClick={() => {
              setIsEditingFoto(!isEditingFoto);
              setFotoUrlInput(profile.foto);
              setUploadError('');
            }}
            className="btn-primary animate-fade-in"
            style={{
              position: 'absolute',
              bottom: '0',
              right: '0',
              borderRadius: '50%',
              width: '36px',
              height: '36px',
              padding: 0,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: '0 4px 10px rgba(0,0,0,0.4)'
            }}
            title="Alterar foto de perfil"
          >
            <Edit2 size={14} />
          </button>
        </div>

        {/* Profile Details */}
        <div style={{ flexGrow: 1, minWidth: '280px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
          {/* Avatar edit inputs */}
          {isEditingFoto ? (
            <div className="glass-panel" style={{ padding: '16px', background: 'var(--bg-primary)', border: '1px solid rgba(0, 224, 84, 0.15)', display: 'flex', flexDirection: 'column', gap: '10px' }}>
              <span style={{ fontSize: '12px', fontWeight: 700, color: 'var(--primary)' }}>ALTERAR FOTO DE PERFIL</span>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px' }}>
                <div style={{ flexGrow: 1, minWidth: '180px', position: 'relative' }}>
                  <input 
                    type="url"
                    className="form-input"
                    placeholder="URL da Imagem..."
                    value={fotoUrlInput}
                    onChange={(e) => setFotoUrlInput(e.target.value)}
                    style={{ fontSize: '13px', padding: '8px 12px' }}
                  />
                </div>
                <button 
                  onClick={() => handleSaveFoto(fotoUrlInput)}
                  className="btn-primary" 
                  style={{ padding: '8px 16px', fontSize: '12px' }}
                >
                  Confirmar URL
                </button>
              </div>
              
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px', margin: '4px 0' }}>
                <div style={{ flexGrow: 1, height: '1px', background: 'rgba(255,255,255,0.06)' }}></div>
                <span style={{ fontSize: '10px', color: 'var(--text-muted)' }}>OU FAÇA UPLOAD</span>
                <div style={{ flexGrow: 1, height: '1px', background: 'rgba(255,255,255,0.06)' }}></div>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <label className="btn-secondary" style={{ padding: '8px 16px', fontSize: '12px', flexGrow: 1, cursor: 'pointer' }}>
                  <Upload size={14} />
                  <span>Escolher arquivo local...</span>
                  <input 
                    type="file" 
                    accept="image/*" 
                    onChange={handleFileUpload} 
                    style={{ display: 'none' }} 
                    disabled={isUploading}
                  />
                </label>
                <button 
                  onClick={() => setIsEditingFoto(false)}
                  className="btn-secondary" 
                  style={{ padding: '8px 16px', fontSize: '12px' }}
                >
                  Cancelar
                </button>
              </div>
              {isUploading && <span style={{ fontSize: '11px', color: 'var(--primary)' }}>Enviando imagem...</span>}
              {uploadError && <span style={{ fontSize: '11px', color: 'var(--danger)' }}>{uploadError}</span>}
            </div>
          ) : null}

          <div>
            <span style={{ fontSize: '11px', fontWeight: 800, background: 'rgba(0, 224, 84, 0.08)', color: 'var(--primary)', padding: '2px 8px', borderRadius: '50px', border: '1px solid rgba(0, 224, 84, 0.15)' }}>
              @{usuario.role}
            </span>
            <h1 style={{ fontSize: '28px', fontWeight: 800, color: '#fff', marginTop: '6px', letterSpacing: '-0.5px' }}>
              {usuario.username}
            </h1>
          </div>

          {/* Biography View/Edit */}
          <div style={{ marginTop: '4px' }}>
            <span style={{ fontSize: '10px', fontWeight: 700, color: 'var(--text-muted)', display: 'block', textTransform: 'uppercase', letterSpacing: '0.8px', marginBottom: '4px' }}>
              Biografia
            </span>
            {isEditingBio ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <textarea 
                  className="form-input"
                  value={editedBio}
                  onChange={(e) => setEditedBio(e.target.value)}
                  style={{ minHeight: '80px', fontSize: '14px', resize: 'vertical' }}
                  placeholder="Escreva algo sobre você..."
                />
                <div style={{ display: 'flex', gap: '10px' }}>
                  <button 
                    onClick={handleSaveBio}
                    className="btn-primary" 
                    style={{ padding: '8px 20px', fontSize: '13px' }}
                  >
                    Salvar
                  </button>
                  <button 
                    onClick={() => setIsEditingBio(false)}
                    className="btn-secondary" 
                    style={{ padding: '8px 20px', fontSize: '13px' }}
                  >
                    Cancelar
                  </button>
                </div>
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start', gap: '8px' }}>
                <p style={{ color: 'var(--text-secondary)', fontSize: '14px', lineHeight: '1.5', whiteSpace: 'pre-wrap' }}>
                  {profile.bio}
                </p>
                <button 
                  onClick={() => {
                    setEditedBio(profile.bio);
                    setIsEditingBio(true);
                  }}
                  className="btn-secondary"
                  style={{
                    padding: '6px 12px',
                    fontSize: '11px',
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '4px',
                    border: '1px solid rgba(255, 255, 255, 0.05)',
                    background: 'rgba(255, 255, 255, 0.01)'
                  }}
                >
                  <Edit2 size={10} />
                  <span>Editar Biografia</span>
                </button>
              </div>
            )}
          </div>
        </div>
      </section>

      {/* Favorite Movies Section */}
      <section style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
        <h2 style={{ fontSize: '20px', fontWeight: 800, color: '#fff', display: 'flex', alignItems: 'center', gap: '10px', letterSpacing: '-0.3px' }}>
          <Heart size={20} color="var(--primary)" style={{ fill: 'var(--primary)' }} />
          Filmes Favoritos ({favoriteMovies.length})
        </h2>

        {isLoadingMovies ? (
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '60px' }}>
            <Loader2 className="animate-spin" size={24} color="var(--primary)" />
            <span style={{ color: 'var(--text-secondary)', marginLeft: '10px' }}>Carregando favoritos...</span>
          </div>
        ) : favoriteMovies.length > 0 ? (
          <div className="movie-grid">
            {favoriteMovies.map(movie => (
              <MovieCard 
                key={movie.id} 
                filme={movie} 
                usuario={usuario}
                onClick={(m) => setSelectedMovie(m)} 
              />
            ))}
          </div>
        ) : (
          <div className="glass-panel" style={{
            padding: '60px 20px',
            textAlign: 'center',
            color: 'var(--text-muted)',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '12px',
            backgroundColor: 'var(--bg-secondary)',
            border: '1px solid rgba(255, 255, 255, 0.03)'
          }}>
            <Film size={40} color="var(--text-muted)" />
            <h3 style={{ fontSize: '16px', fontWeight: 600, color: 'var(--text-secondary)' }}>Nenhum filme favoritado ainda</h3>
            <p style={{ fontSize: '13px', maxWidth: '380px', margin: '0 auto', lineHeight: '1.4' }}>
              Explore o catálogo principal e clique no ícone de coração nos filmes para adicioná-los à sua lista de favoritos!
            </p>
          </div>
        )}
      </section>

      {/* Movie Details Modal Overlay */}
      {selectedMovie && (
        <MovieDetailModal 
          filme={selectedMovie} 
          usuario={usuario} 
          onClose={() => {
            setSelectedMovie(null);
            loadProfileData(); // Reload favorites in case they were altered in the detail modal
            fetchMovies();     // Recalculate movie details
          }} 
        />
      )}

    </main>
  );
}
