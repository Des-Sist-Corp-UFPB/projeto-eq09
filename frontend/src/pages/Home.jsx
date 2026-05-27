import React, { useState, useEffect } from 'react';
import { Search, Plus, Film, Tag, Clock, AlignLeft, User, FileImage, X } from 'lucide-react';
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

  const fetchMovies = async (searchQuery = '') => {
    setIsLoading(true);
    try {
      const url = searchQuery 
        ? `http://localhost:8080/api/filmes?busca=${encodeURIComponent(searchQuery)}`
        : 'http://localhost:8080/api/filmes';
      
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
      const response = await fetch('http://localhost:8080/api/filmes', {
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
    <main className="container animate-fade-in" style={{ paddingBottom: '80px' }}>
      
      {/* Banner */}
      <section style={{
        textAlign: 'center',
        padding: '20px 0 40px 0',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: '12px'
      }}>
        <h1 style={{
          fontSize: '38px',
          fontWeight: 800,
          background: 'linear-gradient(135deg, #fff 40%, var(--primary))',
          WebkitBackgroundClip: 'text',
          WebkitTextFillColor: 'transparent',
          lineHeight: '1.2'
        }}>
          Descubra Histórias Extraordinárias
        </h1>
        <p style={{
          color: 'var(--text-secondary)',
          fontSize: '16px',
          maxWidth: '500px',
          lineHeight: '1.5'
        }}>
          Explore o catálogo oficial, dê notas e compartilhe sua opinião sobre as maiores obras do cinema mundial.
        </p>
      </section>

      {/* Control bar: Search and Add Button */}
      <section style={{
        display: 'flex',
        flexWrap: 'wrap',
        gap: '16px',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '32px'
      }}>
        {/* Search */}
        <div style={{ position: 'relative', width: '100%', maxWidth: '400px' }}>
          <input 
            type="text" 
            placeholder="Pesquisar filmes por título..." 
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
            className="form-input"
            style={{ paddingLeft: '44px' }}
          />
          <Search size={18} color="var(--text-muted)" style={{
            position: 'absolute',
            left: '16px',
            top: '50%',
            transform: 'translateY(-50%)'
          }} />
        </div>

        {/* Add Movie (ADMIN) */}
        {usuario && usuario.role === 'ADMIN' && (
          <button 
            onClick={() => setShowAddForm(!showAddForm)}
            className="btn-primary"
            style={{
              padding: '10px 20px',
              fontSize: '14px',
              background: showAddForm 
                ? 'linear-gradient(135deg, var(--danger), #ef476f)' 
                : 'linear-gradient(135deg, var(--primary), var(--primary-hover))',
              boxShadow: showAddForm ? 'none' : '0 4px 15px var(--primary-glow)'
            }}
          >
            {showAddForm ? <X size={16} /> : <Plus size={16} />}
            <span>{showAddForm ? 'Cancelar' : 'Cadastrar Filme'}</span>
          </button>
        )}
      </section>

      {/* Admin Add Movie Form Panel */}
      {showAddForm && (
        <section 
          className="glass-panel animate-fade-in" 
          style={{
            padding: '30px',
            marginBottom: '40px',
            border: '1px solid rgba(157, 78, 221, 0.25)',
            boxShadow: '0 8px 30px rgba(157, 78, 221, 0.08)'
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '24px' }}>
            <Film size={22} color="var(--primary)" />
            <h2 style={{ fontSize: '20px', fontWeight: 800 }}>Novo Cadastro de Filme</h2>
          </div>

          <form onSubmit={handleAddMovie} style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '20px' }}>
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
              gap: '20px'
            }}>
              {/* Titulo */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                <label style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>Título do Filme *</label>
                <div style={{ position: 'relative' }}>
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
              </div>

              {/* Diretor */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                <label style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>Diretor</label>
                <div style={{ position: 'relative' }}>
                  <input
                    type="text"
                    name="diretor"
                    placeholder="Ex: Christopher Nolan"
                    value={newMovie.diretor}
                    onChange={handleInputChange}
                    className="form-input"
                  />
                </div>
              </div>

              {/* Ano */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                <label style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>Ano de Lançamento *</label>
                <div style={{ position: 'relative' }}>
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
              </div>

              {/* Genero */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                <label style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>Gênero</label>
                <div style={{ position: 'relative' }}>
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
            </div>

            {/* Imagem URL */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>URL da Imagem do Cartaz</label>
              <input
                type="url"
                name="imagemUrl"
                placeholder="Ex: https://images.unsplash.com/... ou deixe em branco para imagem padrão"
                value={newMovie.imagemUrl}
                onChange={handleInputChange}
                className="form-input"
              />
            </div>

            {/* Sinopse */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>Sinopse do Filme</label>
              <textarea
                name="sinopse"
                placeholder="Uma breve descrição sobre a história do filme..."
                value={newMovie.sinopse}
                onChange={handleInputChange}
                className="form-input"
                style={{ minHeight: '100px', resize: 'vertical' }}
              />
            </div>

            {/* Form feedback alerts */}
            {formError && (
              <div className="glass-panel" style={{ padding: '12px', color: 'var(--danger)', borderColor: 'var(--danger)', fontSize: '14px', textAlign: 'center', background: 'rgba(239, 71, 111, 0.05)' }}>
                {formError}
              </div>
            )}
            {formSuccess && (
              <div className="glass-panel" style={{ padding: '12px', color: 'var(--success)', borderColor: 'var(--success)', fontSize: '14px', textAlign: 'center', background: 'rgba(6, 214, 160, 0.05)' }}>
                {formSuccess}
              </div>
            )}

            <button type="submit" className="btn-primary" style={{ justifySelf: 'start', padding: '12px 30px' }}>
              <Plus size={18} />
              <span>Salvar Filme</span>
            </button>
          </form>
        </section>
      )}

      {/* Grid of Movies */}
      {isLoading ? (
        <div style={{ textAlign: 'center', padding: '60px', color: 'var(--text-secondary)', fontSize: '16px' }}>
          Carregando catálogo CineAvalia...
        </div>
      ) : filmes.length > 0 ? (
        <section style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))',
          gap: '30px'
        }}>
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
          gap: '12px'
        }}>
          <Film size={48} />
          <h3 style={{ fontSize: '18px', fontWeight: 600, color: 'var(--text-secondary)' }}>Nenhum filme encontrado</h3>
          <p style={{ fontSize: '14px' }}>Tente pesquisar por outro termo ou cadastre um novo filme.</p>
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
