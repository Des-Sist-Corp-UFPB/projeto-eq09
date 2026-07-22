import React, { useState, useEffect } from 'react';
import { Star, Tag, Calendar, User, Heart } from 'lucide-react';

export default function MovieCard({ filme, usuario, onClick }) {
  const [isFavorited, setIsFavorited] = useState(false);

  // Nota com 1 casa decimal
  const formattedRating = filme.notaMedia ? filme.notaMedia.toFixed(1) : '0.0';

  // Load and sync favorite status
  useEffect(() => {
    const checkFavorite = () => {
      if (usuario) {
        const saved = localStorage.getItem(`profile_${usuario.username}`);
        if (saved) {
          try {
            const parsed = JSON.parse(saved);
            setIsFavorited(parsed.favoritos?.includes(filme.id) || false);
          } catch (e) {
            console.error("Erro ao ler favoritos do card:", e);
          }
        }
      }
    };

    checkFavorite();
    
    window.addEventListener('storage', checkFavorite);
    return () => window.removeEventListener('storage', checkFavorite);
  }, [usuario, filme.id]);

  const handleFavoriteClick = (e) => {
    e.stopPropagation(); // Prevent opening detail modal
    if (!usuario) return;

    const profileKey = `profile_${usuario.username}`;
    const saved = localStorage.getItem(profileKey);
    let profile = { foto: '', bio: '', favoritos: [] };

    if (saved) {
      try {
        profile = JSON.parse(saved);
      } catch (err) {}
    }

    if (!profile.favoritos) {
      profile.favoritos = [];
    }

    let updatedFavs;
    if (profile.favoritos.includes(filme.id)) {
      updatedFavs = profile.favoritos.filter(id => id !== filme.id);
      setIsFavorited(false);
    } else {
      updatedFavs = [...profile.favoritos, filme.id];
      setIsFavorited(true);
    }

    profile.favoritos = updatedFavs;
    localStorage.setItem(profileKey, JSON.stringify(profile));
    
    // Dispatch storage event to sync all parts of UI (navbar, profile, other cards)
    window.dispatchEvent(new Event('storage'));
  };

  return (
    <article 
      onClick={() => onClick(filme)}
      className="glass-panel glass-panel-hover animate-fade-in"
      style={{
        cursor: 'pointer',
        overflow: 'hidden',
        display: 'flex',
        flexDirection: 'column',
        height: '380px',
        position: 'relative',
        backgroundColor: 'var(--bg-secondary)',
        border: '1px solid rgba(255, 255, 255, 0.04)',
        transition: 'transform 0.3s cubic-bezier(0.25, 0.8, 0.25, 1), border-color 0.3s, box-shadow 0.3s'
      }}
      onMouseEnter={(e) => {
        e.currentTarget.style.transform = 'translateY(-6px)';
        e.currentTarget.style.borderColor = 'rgba(0, 224, 84, 0.25)';
        e.currentTarget.style.boxShadow = '0 10px 25px rgba(0, 0, 0, 0.4)';
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.transform = 'translateY(0)';
        e.currentTarget.style.borderColor = 'rgba(255, 255, 255, 0.04)';
        e.currentTarget.style.boxShadow = '0 8px 32px 0 rgba(0, 0, 0, 0.45)';
      }}
    >
      {/* Poster Image */}
      <div style={{
        width: '100%',
        height: '210px',
        position: 'relative',
        overflow: 'hidden',
        background: '#0c0e12'
      }}>
        <img 
          src={filme.imagemUrl || "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=500&auto=format&fit=crop"} 
          alt={filme.titulo}
          style={{
            width: '100%',
            height: '100%',
            objectFit: 'cover',
            transition: 'transform 0.5s ease'
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.transform = 'scale(1.06)';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.transform = 'scale(1)';
          }}
          onError={(e) => {
            e.target.src = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=500&auto=format&fit=crop";
          }}
        />

        {/* Floating Favorite Heart Button */}
        {usuario && (
          <button
            onClick={handleFavoriteClick}
            style={{
              position: 'absolute',
              top: '10px',
              left: '10px',
              background: 'rgba(20, 24, 28, 0.85)',
              backdropFilter: 'blur(8px)',
              border: '1px solid rgba(255, 255, 255, 0.06)',
              borderRadius: '50%',
              width: '28px',
              height: '28px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'pointer',
              zIndex: 2,
              boxShadow: '0 4px 10px rgba(0,0,0,0.4)',
              transition: 'transform 0.2s'
            }}
            onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.1)'}
            onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
            title={isFavorited ? "Remover dos favoritos" : "Favoritar filme"}
          >
            <Heart 
              size={14} 
              color={isFavorited ? 'var(--danger)' : '#fff'} 
              fill={isFavorited ? 'var(--danger)' : 'none'} 
              style={{ transition: 'fill 0.2s, color 0.2s' }}
            />
          </button>
        )}

        {/* Floating Rating Badge */}
        <div style={{
          position: 'absolute',
          top: '10px',
          right: '10px',
          background: 'rgba(20, 24, 28, 0.85)',
          backdropFilter: 'blur(8px)',
          border: '1px solid rgba(255, 255, 255, 0.06)',
          borderRadius: '50px',
          padding: '3px 8px',
          display: 'flex',
          alignItems: 'center',
          gap: '3px',
          boxShadow: '0 4px 10px rgba(0,0,0,0.4)',
          zIndex: 1
        }}>
          <Star size={12} className="star active" style={{ display: 'inline', color: 'var(--star-color)', fill: 'var(--star-color)' }} />
          <span style={{ fontSize: '11px', fontWeight: 700, color: '#fff' }}>{formattedRating}</span>
        </div>
      </div>

      {/* Info Content */}
      <div style={{
        padding: '16px',
        display: 'flex',
        flexDirection: 'column',
        flexGrow: 1,
        justifyContent: 'space-between'
      }}>
        <div>
          {/* Genre Tag */}
          {filme.genero && (
            <span style={{
              fontSize: '10px',
              fontWeight: 700,
              background: 'rgba(0, 224, 84, 0.08)',
              color: 'var(--primary)',
              padding: '2px 8px',
              borderRadius: '50px',
              display: 'inline-flex',
              alignItems: 'center',
              gap: '3px',
              marginBottom: '8px',
              border: '1px solid rgba(0, 224, 84, 0.15)'
            }}>
              <Tag size={9} />
              {filme.genero}
            </span>
          )}

          {/* Title */}
          <h3 style={{
            fontSize: '16px',
            fontWeight: 700,
            lineHeight: '1.25',
            color: 'var(--text-primary)',
            display: '-webkit-box',
            WebkitLineClamp: 2,
            WebkitBoxOrient: 'vertical',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            marginBottom: '4px',
            letterSpacing: '-0.3px'
          }}>
            {filme.titulo}
          </h3>
        </div>

        {/* Footer Metadata */}
        <div style={{
          borderTop: '1px solid rgba(255, 255, 255, 0.04)',
          paddingTop: '10px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          fontSize: '11px',
          color: 'var(--text-secondary)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <User size={11} color="var(--text-muted)" style={{ flexShrink: 0 }} />
            <span style={{
              maxWidth: '90px',
              whiteSpace: 'nowrap',
              overflow: 'hidden',
              textOverflow: 'ellipsis'
            }}>
              {filme.diretor || 'N/A'}
            </span>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '3px' }}>
            <Calendar size={11} color="var(--text-muted)" />
            <span>{filme.ano}</span>
          </div>
        </div>
      </div>
    </article>
  );
}
