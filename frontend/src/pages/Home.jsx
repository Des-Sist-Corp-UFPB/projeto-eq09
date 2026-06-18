import React, { useState, useEffect } from 'react';
import { Search, Plus, Film, X } from 'lucide-react';
import MovieCard from '../components/MovieCard';
import MovieDetailModal from '../components/MovieDetailModal';

export default function Home({ usuario }) {
  const [filmes, setFilmes] = useState([]);
  const [busca, setBusca] = useState('');
  const [selectedMovie, setSelectedMovie] = useState(null);
  const [showAddForm, setShowAddForm] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  
  // Form State for Admin Creation
  const [newMovie, setNewMovie] = useState({
    titulo: '',
    diretor: '',
    ano: new Date().getFullYear(),
    sinopse: '',
    genero: '',
    imagemUrl: ''
  });
  const [formError, setFormError] = useState('');
  const [formSuccess, setFormSuccess] = useState('');
  const [isUploading, setIsUploading] = useState(false);
  const [uploadError, setUploadError] = useState('');

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
        setNewMovie(prev => ({
          ...prev,
          imagemUrl: data.url
        }));
      } else {
        const errData = await response.json().catch(() => ({}));
        setUploadError(errData.error || 'Erro ao fazer upload da imagem.');
      }
    } catch (err) {
      setUploadError('Erro de conexão ao servidor.');
    } finally {
      setIsUploading(false);
    }
  };

  const fetchMovies = async (searchQuery = '') => {
    setIsLoading(true);
    try {
      const url = searchQuery 
        ? `/api/filmes?busca=${encodeURIComponent(searchQuery)}`
        : '/api/filmes';
      
      const response = await fetch(url);
      if (response.ok) {
        const data = await response.json();
        setFilmes(data);
      }
    } catch (err) {
      console.error("Erro ao carregar filmes:", err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchMovies(busca);
  }, [busca]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setNewMovie(prev => ({
      ...prev,
      [name]: name === 'ano' ? parseInt(value, 10) || '' : value
    }));
  };

  const handleAddMovie = async (e) => {
    e.preventDefault();
    setFormError('');
    setFormSuccess('');

    if (!newMovie.titulo.trim() || !newMovie.ano) {
      setFormError('Título do filme e ano de lançamento são obrigatórios.');
      return;
    }

    try {
      const response = await fetch('/api/filmes', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${usuario.token}`
        },
        body: JSON.stringify(newMovie)
      });

      if (response.ok) {
        const createdMovie = await response.json();
        setFilmes(prev => [createdMovie, ...prev]);
        setNewMovie({
          titulo: '',
          diretor: '',
          ano: new Date().getFullYear(),
          sinopse: '',
          genero: '',
          imagemUrl: ''
        });
        setFormSuccess('Filme cadastrado com sucesso!');
        setTimeout(() => {
          setShowAddForm(false);
          setFormSuccess('');
        }, 1500);
      } else {
        const errText = await response.text();
        setFormError(errText || 'Falha ao cadastrar filme. Verifique se possui permissão ADMIN.');
      }
    } catch (err) {
      setFormError('Erro de conexão ao servidor.');
    }
  };

  return (
    <main className="container animate-fade-in" style={{ paddingBottom: '40px' }}>
      
      {/* Banner */}
      <section style={{
        textAlign: 'center',
        padding: '30px 0 40px 0',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: '12px'
      }}>
        <h1 style={{
          fontSize: '32px',
          fontWeight: 800,
          background: 'linear-gradient(135deg, #fff 40%, var(--primary))',
          WebkitBackgroundClip: 'text',
          WebkitTextFillColor: 'transparent',
          lineHeight: '1.15',
          letterSpacing: '-0.5px'
        }}>
          Descubra Histórias Extraordinárias
        </h1>
        <p style={{
          color: 'var(--text-secondary)',
          fontSize: '15px',
          maxWidth: '520px',
          lineHeight: '1.5'
        }}>
          Explore o catálogo oficial, registre suas notas e compartilhe opiniões sobre as maiores produções cinematográficas do mundo.
        </p>
      </section>

      {/* Control bar: Search and Add Button */}
      <section style={{
        display: 'flex',
        flexDirection: 'row',
        flexWrap: 'wrap',
        gap: '16px',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '32px'
      }}>
        {/* Search Input Container */}
        <div style={{ position: 'relative', flexGrow: 1, minWidth: '260px', maxWidth: '400px' }}>
          <input 
            type="text" 
            placeholder="Pesquisar filmes por título..." 
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
            className="form-input"
            style={{ paddingLeft: '44px' }}
          />
          <Search size={16} color="var(--text-muted)" style={{
            position: 'absolute',
            left: '16px',
            top: '50%',
            transform: 'translateY(-50%)'
          }} />
        </div>

        {/* Add Movie Button (ADMIN Only) */}
        {usuario && usuario.role === 'ADMIN' && (
          <button 
            onClick={() => setShowAddForm(!showAddForm)}
            className="btn-primary"
            style={{
              padding: '10px 20px',
              fontSize: '14px',
              background: showAddForm 
                ? 'linear-gradient(135deg, var(--danger), #ff1a53)' 
                : 'linear-gradient(135deg, var(--primary), var(--primary-hover))',
              color: showAddForm ? '#fff' : '#0c0e12',
              boxShadow: showAddForm ? 'none' : '0 4px 12px var(--primary-glow)'
            }}
          >
            {showAddForm ? <X size={15} /> : <Plus size={15} />}
            <span>{showAddForm ? 'Cancelar' : 'Cadastrar Filme'}</span>
          </button>
        )}
      </section>

      {/* Admin Add Movie Form Panel */}
      {showAddForm && (
        <section 
          className="glass-panel animate-fade-in" 
          style={{
            padding: '24px',
            marginBottom: '32px',
            backgroundColor: 'var(--bg-secondary)',
            border: '1px solid rgba(0, 224, 84, 0.15)',
            boxShadow: '0 8px 30px rgba(0, 224, 84, 0.04)'
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '20px' }}>
            <Film size={20} color="var(--primary)" />
            <h2 style={{ fontSize: '18px', fontWeight: 800, color: '#fff', letterSpacing: '-0.3px' }}>Novo Cadastro de Filme</h2>
          </div>

          <form onSubmit={handleAddMovie} style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '20px' }}>
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
              gap: '16px'
            }}>
              {/* Titulo */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)' }}>Título do Filme *</label>
                <input
                  type="text"
                  name="titulo"
                  placeholder="Ex: Interstellar"
                  value={newMovie.titulo}
                  onChange={handleInputChange}
                  className="form-input"
                  required
                />
              </div>

              {/* Diretor */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)' }}>Diretor</label>
                <input
                  type="text"
                  name="diretor"
                  placeholder="Ex: Christopher Nolan"
                  value={newMovie.diretor}
                  onChange={handleInputChange}
                  className="form-input"
                />
              </div>

              {/* Ano */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)' }}>Ano de Lançamento *</label>
                <input
                  type="number"
                  name="ano"
                  min="1880"
                  max={new Date().getFullYear() + 5}
                  value={newMovie.ano}
                  onChange={handleInputChange}
                  className="form-input"
                  required
                />
              </div>

              {/* Genero */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)' }}>Gênero</label>
                <input
                  type="text"
                  name="genero"
                  placeholder="Ex: Ficção Científica, Ação"
                  value={newMovie.genero}
                  onChange={handleInputChange}
                  className="form-input"
                />
              </div>
            </div>

            {/* Imagem URL e Upload */}
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
              gap: '16px'
            }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)' }}>Fazer Upload do Cartaz</label>
                <input
                  type="file"
                  accept="image/*"
                  onChange={handleFileUpload}
                  className="form-input"
                  style={{ padding: '8px' }}
                  disabled={isUploading}
                />
                {isUploading && <span style={{ fontSize: '11px', color: 'var(--primary)' }}>Enviando imagem...</span>}
                {uploadError && <span style={{ fontSize: '11px', color: 'var(--danger)' }}>{uploadError}</span>}
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)' }}>URL do Cartaz</label>
                <input
                  type="url"
                  name="imagemUrl"
                  placeholder="URL gerada no upload ou link externo"
                  value={newMovie.imagemUrl}
                  onChange={handleInputChange}
                  className="form-input"
                />
              </div>
            </div>

            {/* Sinopse */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)' }}>Sinopse do Filme</label>
              <textarea
                name="sinopse"
                placeholder="Uma breve descrição sobre a história do filme..."
                value={newMovie.sinopse}
                onChange={handleInputChange}
                className="form-input"
                style={{ minHeight: '80px', resize: 'vertical' }}
              />
            </div>

            {/* Form feedback alerts */}
            {formError && (
              <div className="glass-panel" style={{ padding: '10px 14px', color: 'var(--danger)', borderColor: 'var(--danger)', fontSize: '13px', background: 'rgba(255, 51, 102, 0.05)' }}>
                {formError}
              </div>
            )}
            {formSuccess && (
              <div className="glass-panel" style={{ padding: '10px 14px', color: 'var(--success)', borderColor: 'var(--success)', fontSize: '13px', background: 'rgba(0, 224, 84, 0.05)' }}>
                {formSuccess}
              </div>
            )}

            <button type="submit" className="btn-primary" style={{ justifySelf: 'start', padding: '10px 24px', fontSize: '14px' }}>
              <Plus size={15} />
              <span>Salvar Filme</span>
            </button>
          </form>
        </section>
      )}

      {/* Grid of Movies */}
      {isLoading ? (
        <div style={{ textAlign: 'center', padding: '60px', color: 'var(--text-secondary)', fontSize: '15px' }}>
          Carregando catálogo DSCboxd...
        </div>
      ) : filmes.length > 0 ? (
        <section className="movie-grid">
          {filmes.map(filme => (
            <MovieCard 
              key={filme.id} 
              filme={filme} 
              onClick={(m) => setSelectedMovie(m)} 
            />
          ))}
        </section>
      ) : (
        <section className="glass-panel" style={{
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
          <h3 style={{ fontSize: '16px', fontWeight: 600, color: 'var(--text-secondary)' }}>Nenhum filme encontrado</h3>
          <p style={{ fontSize: '13px' }}>Tente pesquisar por outro termo ou cadastre um novo filme.</p>
        </section>
      )}

      {/* Movie Details Modal Overlay */}
      {selectedMovie && (
        <MovieDetailModal 
          filme={selectedMovie} 
          usuario={usuario} 
          onClose={() => {
            setSelectedMovie(null);
            fetchMovies(busca); // Recarrega os filmes para atualizar as médias na Home
          }} 
        />
      )}
      
    </main>
  );
}
