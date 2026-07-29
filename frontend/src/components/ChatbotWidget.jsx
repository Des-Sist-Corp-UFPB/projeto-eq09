import React, { useState, useRef, useEffect } from 'react';
import { Bot, Send, X, Film, Sparkles, User, RefreshCw } from 'lucide-react';

export default function ChatbotWidget({ usuario, onSelectMovie }) {
  const [isOpen, setIsOpen] = useState(false);
  const [mensagem, setMensagem] = useState('');
  const [mensagens, setMensagens] = useState([
    {
      id: 1,
      sender: 'bot',
      texto: 'Olá! Sou o seu Assistente CineBot 🤖🍿. Posso recomendar os melhores filmes para você com base nos filmes que você já assistiu no seu Diário! O que você gostaria de assistir hoje?',
      recomendacoes: []
    }
  ]);
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (isOpen) {
      scrollToBottom();
    }
  }, [mensagens, isOpen, loading]);

  const handleSendMessage = async (textToSend) => {
    const text = textToSend || mensagem;
    if (!text || !text.trim() || loading) return;

    const userMsgId = Date.now();
    const newMessages = [
      ...mensagens,
      { id: userMsgId, sender: 'user', texto: text, recomendacoes: [] }
    ];

    setMensagens(newMessages);
    if (!textToSend) setMensagem('');
    setLoading(true);

    try {
      const headers = { 'Content-Type': 'application/json' };
      if (usuario?.token) {
        headers['Authorization'] = `Bearer ${usuario.token}`;
      }

      const response = await fetch('/api/chatbot/conversar', {
        method: 'POST',
        headers,
        body: JSON.stringify({ mensagem: text })
      });

      if (response.ok) {
        const data = await response.json();
        setMensagens(prev => [
          ...prev,
          {
            id: Date.now() + 1,
            sender: 'bot',
            texto: data.resposta,
            recomendacoes: data.recomendacoes || []
          }
        ]);
      } else {
        setMensagens(prev => [
          ...prev,
          {
            id: Date.now() + 1,
            sender: 'bot',
            texto: 'Desculpe, ocorreu um erro ao consultar o assistente de recomendação.',
            recomendacoes: []
          }
        ]);
      }
    } catch (err) {
      setMensagens(prev => [
        ...prev,
        {
          id: Date.now() + 1,
          sender: 'bot',
          texto: 'Erro de conexão com o servidor de recomendações.',
          recomendacoes: []
        }
      ]);
    } finally {
      setLoading(false);
    }
  };

  const presetPrompts = [
    "Recomende filmes baseados nos meus assistidos",
    "Sugira um filme de Ficção Científica",
    "O que assistir hoje?",
    "Quero ver um filme de Ação"
  ];

  return (
    <div style={{ position: 'fixed', bottom: '24px', right: '24px', zIndex: 1000 }}>
      {/* Botão Flutuante para Abrir Chat */}
      {!isOpen && (
        <button
          onClick={() => setIsOpen(true)}
          style={{
            background: 'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)',
            color: '#fff',
            border: 'none',
            borderRadius: '50px',
            padding: '14px 22px',
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            fontWeight: 700,
            fontSize: '14px',
            cursor: 'pointer',
            boxShadow: '0 8px 24px rgba(168, 85, 247, 0.4), 0 2px 8px rgba(0, 0, 0, 0.2)',
            transition: 'transform 0.2s, box-shadow 0.2s',
          }}
          onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.05)'}
          onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
        >
          <Sparkles size={18} />
          <span>IA CineBot</span>
        </button>
      )}

      {/* Janela do Chatbot */}
      {isOpen && (
        <div
          style={{
            width: '380px',
            maxWidth: 'calc(100vw - 32px)',
            height: '540px',
            maxHeight: 'calc(100vh - 100px)',
            backgroundColor: '#12131a',
            border: '1px solid rgba(255, 255, 255, 0.1)',
            borderRadius: '20px',
            boxShadow: '0 16px 40px rgba(0, 0, 0, 0.6), 0 0 30px rgba(168, 85, 247, 0.15)',
            display: 'flex',
            flexDirection: 'column',
            overflow: 'hidden',
            backdropFilter: 'blur(16px)'
          }}
        >
          {/* Cabeçalho do Chat */}
          <div
            style={{
              background: 'linear-gradient(135deg, rgba(99, 102, 241, 0.2) 0%, rgba(168, 85, 247, 0.2) 100%)',
              borderBottom: '1px solid rgba(255, 255, 255, 0.08)',
              padding: '16px 20px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between'
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <div
                style={{
                  width: '36px',
                  height: '36px',
                  borderRadius: '10px',
                  background: 'linear-gradient(135deg, #6366f1, #a855f7)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: '#fff'
                }}
              >
                <Bot size={20} />
              </div>
              <div>
                <h4 style={{ margin: 0, fontSize: '15px', fontWeight: 700, color: '#fff' }}>CineBot IA</h4>
                <span style={{ fontSize: '11px', color: '#a1a1aa' }}>Recomendador de Filmes</span>
              </div>
            </div>
            <button
              onClick={() => setIsOpen(false)}
              style={{
                background: 'rgba(255, 255, 255, 0.05)',
                border: 'none',
                color: '#a1a1aa',
                borderRadius: '50%',
                width: '30px',
                height: '30px',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}
            >
              <X size={16} />
            </button>
          </div>

          {/* Área de Mensagens */}
          <div
            style={{
              flex: 1,
              padding: '16px',
              overflowY: 'auto',
              display: 'flex',
              flexDirection: 'column',
              gap: '12px'
            }}
          >
            {mensagens.map((msg) => (
              <div
                key={msg.id}
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: msg.sender === 'user' ? 'flex-end' : 'flex-start'
                }}
              >
                <div
                  style={{
                    maxWidth: '85%',
                    padding: '12px 16px',
                    borderRadius: msg.sender === 'user' ? '18px 18px 4px 18px' : '18px 18px 18px 4px',
                    background: msg.sender === 'user'
                      ? 'linear-gradient(135deg, #6366f1, #4f46e5)'
                      : 'rgba(255, 255, 255, 0.06)',
                    color: '#fff',
                    fontSize: '13px',
                    lineHeight: '1.5',
                    boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
                  }}
                >
                  {msg.texto}
                </div>

                {/* Cards de filmes recomendados */}
                {msg.recomendacoes && msg.recomendacoes.length > 0 && (
                  <div style={{ marginTop: '10px', display: 'flex', flexDirection: 'column', gap: '8px', width: '100%' }}>
                    {msg.recomendacoes.map((rec) => (
                      <div
                        key={rec.id}
                        onClick={() => onSelectMovie && onSelectMovie(rec)}
                        style={{
                          background: 'rgba(255, 255, 255, 0.03)',
                          border: '1px solid rgba(255, 255, 255, 0.08)',
                          borderRadius: '12px',
                          padding: '10px',
                          display: 'flex',
                          gap: '12px',
                          alignItems: 'center',
                          cursor: 'pointer',
                          transition: 'background 0.2s, transform 0.2s'
                        }}
                        onMouseEnter={(e) => {
                          e.currentTarget.style.background = 'rgba(168, 85, 247, 0.12)';
                          e.currentTarget.style.transform = 'translateY(-2px)';
                        }}
                        onMouseLeave={(e) => {
                          e.currentTarget.style.background = 'rgba(255, 255, 255, 0.03)';
                          e.currentTarget.style.transform = 'translateY(0)';
                        }}
                      >
                        {rec.imagemUrl ? (
                          <img
                            src={rec.imagemUrl}
                            alt={rec.titulo}
                            style={{ width: '42px', height: '60px', borderRadius: '6px', objectFit: 'cover' }}
                          />
                        ) : (
                          <div style={{ width: '42px', height: '60px', borderRadius: '6px', background: '#27272a', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                            <Film size={18} color="#a1a1aa" />
                          </div>
                        )}
                        <div style={{ flex: 1, overflow: 'hidden' }}>
                          <h5 style={{ margin: 0, fontSize: '13px', color: '#fff', fontWeight: 700, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                            {rec.titulo} ({rec.ano})
                          </h5>
                          <span style={{ fontSize: '11px', color: '#a855f7', fontWeight: 600 }}>{rec.genero}</span>
                          <p style={{ margin: '2px 0 0 0', fontSize: '10px', color: '#a1a1aa', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                            {rec.motivo}
                          </p>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ))}

            {loading && (
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '8px 14px', background: 'rgba(255,255,255,0.05)', borderRadius: '16px', width: 'fit-content' }}>
                <RefreshCw size={14} className="spin" style={{ animation: 'spin 1s linear infinite', color: '#a855f7' }} />
                <span style={{ fontSize: '12px', color: '#a1a1aa' }}>Analisando seu diário de filmes...</span>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Atalhos Rápidos */}
          <div style={{ padding: '8px 12px', display: 'flex', gap: '6px', overflowX: 'auto', borderTop: '1px solid rgba(255,255,255,0.04)' }}>
            {presetPrompts.map((prompt, idx) => (
              <button
                key={idx}
                onClick={() => handleSendMessage(prompt)}
                disabled={loading}
                style={{
                  background: 'rgba(255,255,255,0.04)',
                  border: '1px solid rgba(255,255,255,0.08)',
                  borderRadius: '12px',
                  padding: '6px 10px',
                  color: '#d4d4d8',
                  fontSize: '11px',
                  whiteSpace: 'nowrap',
                  cursor: 'pointer',
                  transition: 'background 0.2s'
                }}
                onMouseEnter={(e) => e.currentTarget.style.background = 'rgba(99, 102, 241, 0.2)'}
                onMouseLeave={(e) => e.currentTarget.style.background = 'rgba(255,255,255,0.04)'}
              >
                {prompt}
              </button>
            ))}
          </div>

          {/* Input de Envio de Mensagem */}
          <form
            onSubmit={(e) => { e.preventDefault(); handleSendMessage(); }}
            style={{
              padding: '12px',
              borderTop: '1px solid rgba(255, 255, 255, 0.08)',
              display: 'flex',
              gap: '8px'
            }}
          >
            <input
              type="text"
              placeholder="Pergunte ao CineBot..."
              value={mensagem}
              onChange={(e) => setMensagem(e.target.value)}
              disabled={loading}
              style={{
                flex: 1,
                background: 'rgba(255, 255, 255, 0.05)',
                border: '1px solid rgba(255, 255, 255, 0.1)',
                borderRadius: '12px',
                padding: '10px 14px',
                color: '#fff',
                fontSize: '13px',
                outline: 'none'
              }}
            />
            <button
              type="submit"
              disabled={loading || !mensagem.trim()}
              style={{
                background: 'linear-gradient(135deg, #6366f1, #a855f7)',
                border: 'none',
                borderRadius: '12px',
                width: '40px',
                height: '40px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#fff',
                cursor: loading || !mensagem.trim() ? 'not-allowed' : 'pointer',
                opacity: loading || !mensagem.trim() ? 0.5 : 1
              }}
            >
              <Send size={16} />
            </button>
          </form>
        </div>
      )}
    </div>
  );
}
