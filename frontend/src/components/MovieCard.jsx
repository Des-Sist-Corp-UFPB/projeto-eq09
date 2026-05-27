import React from 'react';
import { Star, Tag, Calendar, User } from 'lucide-react';

export default function MovieCard({ filme, onClick }) {
  // Nota com 1 casa decimal
  const formattedRating = filme.notaMedia ? filme.notaMedia.toFixed(1) : '0.0';

  return (
    <article 
      onClick={() => onClick(filme)}
      className="glass-panel glass-panel-hover animate-fade-in"
      style={{
        cursor: 'pointer',
        overflow: 'hidden',
        display: 'flex',
        flexDirection: 'column',
        height: '420px',
        position: 'relative',
        transition: 'transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275), border-color 0.3s, box-shadow 0.3s'
      }}
      onMouseEnter={(e) => {
        e.currentTarget.style.transform = 'translateY(-8px) scale(1.02)';
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.transform = 'translateY(0) scale(1)';
      }}
    >
      {/* Poster Image */}
      <div style={{
        width: '100%',
        height: '240px',
        position: 'relative',
        overflow: 'hidden',
        background: '#07080a'
      }}>
        <img 
          src={filme.imagemUrl || "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=500&auto=format&fit=crop"} 
          alt={filme.titulo}
          style={{
            width: '100%',
            height: '100%',
            objectFit: 'cover',
            transition: 'transform 0.6s ease'
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.transform = 'scale(1.1)';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.transform = 'scale(1)';
          }}
          onError={(e) => {
            e.target.src = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=500&auto=format&fit=crop";
          }}
        />

        {/* Floating Rating Badge */}
        <div style={{
          position: 'absolute',
          top: '12px',
          right: '12px',
          background: 'rgba(10, 11, 14, 0.75)',
          backdropFilter: 'blur(8px)',
          border: '1px solid rgba(255, 255, 255, 0.08)',
          borderRadius: '50px',
          padding: '4px 10px',
          display: 'flex',
          alignItems: 'center',
          gap: '4px',
          boxShadow: '0 4px 10px rgba(0,0,0,0.3)',
          zIndex: 1
        }}>
          <Star size={14} className="star active" style={{ display: 'inline', color: 'var(--star-color)' }} />
          <span style={{ fontSize: '12px', fontWeight: 700, color: '#fff' }}>{formattedRating}</span>
        </div>
      </div>

      {/* Info Content */}
      <div style={{
        padding: '20px',
        display: 'flex',
        flexDirection: 'column',
        flexGrow: 1,
        justifyContent: 'space-between'
      }}>
        <div>
          {/* Genre Tag */}
          {filme.genero && (
            <span style={{
              fontSize: '11px',
              fontWeight: 600,
              background: 'rgba(58, 134, 200, 0.12)',
              color: 'var(--accent-hover)',
              padding: '3px 8px',
              borderRadius: '50px',
              display: 'inline-flex',
              alignItems: 'center',
              gap: '4px',
              marginBottom: '8px',
              border: '1px solid rgba(58, 134, 200, 0.2)'
            }}>
              <Tag size={10} />
              {filme.genero}
            </span>
          )}

          {/* Title */}
          <h3 style={{
            fontSize: '18px',
            fontWeight: 700,
            lineHeight: '1.3',
            color: 'var(--text-primary)',
            display: '-webkit-box',
            WebkitLineClamp: 2,
            WebkitBoxOrient: 'vertical',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            marginBottom: '6px'
          }}>
            {filme.titulo}
          </h3>
        </div>

        {/* Metadados do Rodapé */}
        <div style={{
          borderTop: '1px solid rgba(255, 255, 255, 0.04)',
          paddingTop: '12px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          fontSize: '12px',
          color: 'var(--text-secondary)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <User size={12} color="var(--text-muted)" />
            <span style={{
              maxWidth: '90px',
              whiteSpace: 'nowrap',
              overflow: 'hidden',
              textOverflow: 'ellipsis'
            }}>
              {filme.diretor || 'N/A'}
            </span>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Calendar size={12} color="var(--text-muted)" />
            <span>{filme.ano}</span>
          </div>
        </div>
      </div>
    </article>
  );
}
