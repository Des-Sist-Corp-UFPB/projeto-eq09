import http from 'k6/http';
import { check, sleep } from 'k6';

// ─────────────────────────────────────────────────────────────────────────────
// Teste de Carga e Performance — k6
//
// Este script simula cenários realistas de uso da plataforma CineAvalia.
// Ele está dividido em três fluxos principais:
// 1. Navegação Pública: Usuários anônimos navegando e pesquisando filmes.
// 2. Fluxo do Usuário Logado: Login de usuário comum, visualização de estatísticas,
//    e manipulação da watchlist com limpeza automática para não poluir o banco de dados.
// 3. Fluxo do Administrador: Login de administrador, cadastro de novo filme e
//    remoção imediata (limpeza de dados).
// ─────────────────────────────────────────────────────────────────────────────

// URL base do ambiente local obtido das variáveis de ambiente (padrão porta 8109)
const BASE = __ENV.BASE_URL || 'http://localhost:8109';

// Número total de usuários virtuais (VUs) simultâneos
const VUS = Number(__ENV.VUS || 10);

// Configuração de cenários do k6 e thresholds de sucesso
export const options = {
  scenarios: {
    // Cenário 1: Navegação Pública (60% da carga configurada)
    public_browsing: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '10s', target: Math.max(1, Math.ceil(VUS * 0.6)) }, // Rampa de subida
        { duration: '30s', target: Math.max(1, Math.ceil(VUS * 0.6)) }, // Carga estável
        { duration: '10s', target: 0 },                               // Rampa de descida
      ],
      gracefulRampDown: '5s',
      exec: 'publicBrowsingScenario',
    },
    // Cenário 2: Usuário Logado (30% da carga configurada)
    user_flow: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '10s', target: Math.max(1, Math.ceil(VUS * 0.3)) }, // Rampa de subida
        { duration: '30s', target: Math.max(1, Math.ceil(VUS * 0.3)) }, // Carga estável
        { duration: '10s', target: 0 },                               // Rampa de descida
      ],
      gracefulRampDown: '5s',
      exec: 'userFlowScenario',
    },
    // Cenário 3: Administrador do Catálogo (10% da carga configurada)
    admin_flow: {
      executor: 'shared-iterations',
      vus: Math.max(1, Math.floor(VUS * 0.1)),
      iterations: Math.max(2, Math.floor(VUS * 2)),
      maxDuration: '1m',
      exec: 'adminFlowScenario',
    },
  },
  // Metas de performance da aplicação
  thresholds: {
    http_req_failed: ['rate<0.01'],   // Menos de 1% das requisições podem falhar (erros 5xx, 4xx inesperados)
    http_req_duration: ['p(95)<500'], // 95% das requisições devem responder em menos de 500ms
  },
};

// Variáveis de escopo da VU para armazenar tokens JWT e evitar logins repetitivos
let userToken = null;
let adminToken = null;

// =============================================================================
// Cenário 1: Navegação Pública (Visitantes Anônimos)
// =============================================================================
export function publicBrowsingScenario() {
  // 1. Healthcheck básico (Ping)
  const pingRes = http.get(`${BASE}/ping`);
  check(pingRes, {
    'ping status 200': (r) => r.status === 200,
    'ping response ok': (r) => r.json('status') === 'ok',
  });

  // 2. Listagem geral de filmes do catálogo
  const listRes = http.get(`${BASE}/api/filmes`);
  check(listRes, {
    'listar filmes status 200': (r) => r.status === 200,
    'listar filmes retorna lista': (r) => Array.isArray(r.json()),
  });

  // 3. Filtrar filmes com termo de busca (Pesquisa)
  const searchRes = http.get(`${BASE}/api/filmes?busca=Inception`);
  check(searchRes, {
    'busca filmes status 200': (r) => r.status === 200,
    'busca retorna pelo menos um resultado': (r) => Array.isArray(r.json()) && r.json().length > 0,
  });

  // 4. Detalhes de um filme específico (IDs 1 a 16 criados pelo DataInitializer)
  const filmId = Math.floor(Math.random() * 16) + 1;
  const detailRes = http.get(`${BASE}/api/filmes/${filmId}`);
  check(detailRes, {
    'obter filme por id status 200': (r) => r.status === 200,
    'detalhe contem id correto': (r) => r.json('id') === filmId,
  });

  // 5. Listar comentários do filme visualizado
  const commentsRes = http.get(`${BASE}/api/filmes/${filmId}/comentarios`);
  check(commentsRes, {
    'listar comentarios status 200': (r) => r.status === 200,
  });

  // Simula o tempo de leitura do usuário (think time)
  sleep(1);
}

// =============================================================================
// Cenário 2: Fluxo do Usuário Logado (Watchlist e Diário)
// =============================================================================
export function userFlowScenario() {
  // Realiza o login uma única vez por VU para evitar overhead de autenticação repetida
  if (!userToken) {
    const loginPayload = JSON.stringify({
      username: 'user',
      password: 'user123',
    });
    const loginRes = http.post(`${BASE}/api/auth/login`, loginPayload, {
      headers: { 'Content-Type': 'application/json' },
    });

    const loginOk = check(loginRes, {
      'login user status 200': (r) => r.status === 200,
      'user token gerado com sucesso': (r) => r.json('token') !== undefined && r.json('token') !== '',
    });

    if (loginOk) {
      userToken = loginRes.json('token');
    } else {
      // Se falhar o login, aguarda e retorna para evitar erros em cascata
      sleep(1);
      return;
    }
  }

  const authHeaders = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${userToken}`,
  };

  // 1. Acessa as estatísticas do diário do usuário
  const statsRes = http.get(`${BASE}/api/diario/estatisticas`, { headers: authHeaders });
  check(statsRes, {
    'obter estatisticas diario status 200': (r) => r.status === 200,
  });

  // 2. Consulta a watchlist do usuário
  const watchlistRes = http.get(`${BASE}/api/watchlist`, { headers: authHeaders });
  check(watchlistRes, {
    'listar watchlist status 200': (r) => r.status === 200,
  });

  // 3. Consulta o total da watchlist
  const totalRes = http.get(`${BASE}/api/watchlist/total`, { headers: authHeaders });
  check(totalRes, {
    'obter total watchlist status 200': (r) => r.status === 200,
  });

  // 4. Fluxo de alteração de Watchlist (Adição, Verificação e Remoção)
  // Para evitar colisões de VUs simultâneas operando no mesmo filme, usamos um ID derivado da VU
  const filmeId = (__VU % 16) + 1;

  // A. Verifica estado antes de adicionar
  const verifyBeforeRes = http.get(`${BASE}/api/watchlist/filmes/${filmeId}/verificar`, { headers: authHeaders });
  check(verifyBeforeRes, {
    'verificar status antes status 200': (r) => r.status === 200,
  });

  // B. Adiciona filme à watchlist (POST)
  const addRes = http.post(`${BASE}/api/watchlist/filmes/${filmeId}`, null, { headers: authHeaders });
  check(addRes, {
    'adicionar filme status 200': (r) => r.status === 200,
  });

  // C. Verifica se o filme foi adicionado
  const verifyAfterRes = http.get(`${BASE}/api/watchlist/filmes/${filmeId}/verificar`, { headers: authHeaders });
  check(verifyAfterRes, {
    'verificar status depois status 200': (r) => r.status === 200,
    'filme esta na watchlist': (r) => r.json('naWatchlist') === true,
  });

  // D. Limpeza imediata: Remove o filme da watchlist (DELETE) para não poluir o banco de dados
  const deleteRes = http.del(`${BASE}/api/watchlist/filmes/${filmeId}`, null, { headers: authHeaders });
  check(deleteRes, {
    'remover filme status 200': (r) => r.status === 200,
  });

  // E. Verifica se voltou ao estado original (não está mais na watchlist)
  const verifyFinalRes = http.get(`${BASE}/api/watchlist/filmes/${filmeId}/verificar`, { headers: authHeaders });
  check(verifyFinalRes, {
    'verificar status final status 200': (r) => r.status === 200,
    'filme nao esta mais na watchlist': (r) => r.json('naWatchlist') === false,
  });

  sleep(1);
}

// =============================================================================
// Cenário 3: Fluxo de Administrador (Cadastro e Exclusão de Filmes)
// =============================================================================
export function adminFlowScenario() {
  // Realiza o login uma única vez por VU de administrador
  if (!adminToken) {
    const loginPayload = JSON.stringify({
      username: 'admin@admin.com',
      password: 'admin123',
    });
    const loginRes = http.post(`${BASE}/api/auth/login`, loginPayload, {
      headers: { 'Content-Type': 'application/json' },
    });

    const loginOk = check(loginRes, {
      'login admin status 200': (r) => r.status === 200,
      'admin token gerado com sucesso': (r) => r.json('token') !== undefined && r.json('token') !== '',
    });

    if (loginOk) {
      adminToken = loginRes.json('token');
    } else {
      sleep(1);
      return;
    }
  }

  const authHeaders = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${adminToken}`,
  };

  // Geramos dados dinâmicos usando identificadores da VU e iteração para evitar duplicidades
  const uniqueId = `${__VU}-${__ITER}-${Math.floor(Math.random() * 100000)}`;
  const movieTitle = `Filme de Teste K6 ${uniqueId}`;

  // 1. Cadastra um novo filme no catálogo (POST)
  const createPayload = JSON.stringify({
    titulo: movieTitle,
    diretor: 'Diretor de Teste K6',
    ano: 2026,
    sinopse: 'Sinopse fictícia gerada para validação de carga pelo k6.',
    genero: 'Performance',
    imagemUrl: 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=500',
  });

  const createRes = http.post(`${BASE}/api/filmes`, createPayload, { headers: authHeaders });
  const createOk = check(createRes, {
    'cadastrar filme status 201': (r) => r.status === 201,
    'id do filme retornado': (r) => r.json('id') !== undefined,
  });

  if (createOk) {
    const newFilmeId = createRes.json('id');

    // 2. Busca o filme recém-criado por ID (GET)
    const getRes = http.get(`${BASE}/api/filmes/${newFilmeId}`);
    check(getRes, {
      'obter filme criado status 200': (r) => r.status === 200,
      'titulo do filme bate com cadastrado': (r) => r.json('titulo') === movieTitle,
    });

    // 3. Limpeza: Remove o filme criado (DELETE) para não poluir o banco de dados
    const deleteRes = http.del(`${BASE}/api/filmes/${newFilmeId}`, null, { headers: authHeaders });
    check(deleteRes, {
      'remover filme status 204': (r) => r.status === 204,
    });
  }

  sleep(1);
}
