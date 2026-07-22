package br.ufpb.dsc.mercado.config;

import br.ufpb.dsc.mercado.domain.Filme;
import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.repository.FilmeRepository;
import br.ufpb.dsc.mercado.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final FilmeRepository filmeRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, FilmeRepository filmeRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.filmeRepository = filmeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Remove usuário admin antigo se existir
        usuarioRepository.findByUsername("admin").ifPresent(usuarioRepository::delete);

        // Inicializa Usuários padrão se não existirem
        if (!usuarioRepository.existsByUsername("admin@admin.com")) {
            Usuario admin = new Usuario("admin@admin.com", passwordEncoder.encode("admin123"), "ADMIN");
            usuarioRepository.save(admin);
        }

        if (!usuarioRepository.existsByUsername("user")) {
            Usuario user = new Usuario("user", passwordEncoder.encode("user123"), "USER");
            usuarioRepository.save(user);
        }

        // Inicializa Filmes padrão de forma robusta e com validação anti-duplicação
        checkAndSaveFilme(
                "Inception",
                "Christopher Nolan",
                2010,
                "Um ladrão que rouba segredos corporativos por meio do uso de tecnologia de compartilhamento de sonhos tem a chance de limpar seu histórico criminal.",
                "Ficção Científica",
                "https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=500&auto=format&fit=crop"
        );

        checkAndSaveFilme(
                "O Poderoso Chefão",
                "Francis Ford Coppola",
                1972,
                "O patriarca idoso de uma dinastia do crime organizado transfere o controle de seu império clandestino para seu filho relutante.",
                "Drama",
                "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=500&auto=format&fit=crop"
        );

        checkAndSaveFilme(
                "Interestelar",
                "Christopher Nolan",
                2014,
                "Uma equipe de exploradores viaja através de um buraco de minhoca no espaço na tentativa de garantir a sobrevivência da humanidade.",
                "Ficção Científica",
                "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=500&auto=format&fit=crop"
        );

        checkAndSaveFilme(
                "Clube da Luta",
                "David Fincher",
                1999,
                "Um funcionário de escritório insone e um criador de sabão descompromissado formam um clube de luta underground que evolui para algo muito maior.",
                "Drama",
                "https://images.unsplash.com/photo-1594909122845-11baa439b7bf?q=80&w=500&auto=format&fit=crop"
        );

        checkAndSaveFilme(
                "Pulp Fiction: Tempo de Violência",
                "Quentin Tarantino",
                1994,
                "As vidas de dois assassinos da máfia, um boxeador, a esposa de um gângster e um par de bandidos se entrelaçam em quatro histórias de violência e redenção.",
                "Crime / Suspense",
                "https://images.unsplash.com/photo-1585647347483-22b66260dfff?q=80&w=500&auto=format&fit=crop"
        );

        checkAndSaveFilme(
                "Batman: O Cavaleiro das Trevas",
                "Christopher Nolan",
                2008,
                "Quando o vilão conhecido como o Coringa espalha o caos e a desordem na cidade de Gotham, Batman deve aceitar um dos maiores testes psicológicos e físicos de sua habilidade.",
                "Ação / Policial",
                "https://images.unsplash.com/photo-1509248961158-e54f6934749c?q=80&w=500&auto=format&fit=crop"
        );

        checkAndSaveFilme(
                "Matrix",
                "Lana Wachowski, Lilly Wachowski",
                1999,
                "Quando uma bela desconhecida leva o programador Neo a um submundo proibido, ele descobre a terrível verdade: a vida que ele conhece é uma simulação sofisticada.",
                "Ficção Científica",
                "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?q=80&w=500&auto=format&fit=crop"
        );

        checkAndSaveFilme(
                "A Viagem de Chihiro",
                "Hayao Miyazaki",
                2001,
                "Durante a mudança de sua família para o subúrbio, uma garota de 10 anos vagueia por um mundo governado por deuses, bruxas e espíritos, onde os humanos viram animais.",
                "Animação / Fantasia",
                "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?q=80&w=500&auto=format&fit=crop"
        );

        checkAndSaveFilme(
                "O Senhor dos Anéis: A Sociedade do Anel",
                "Peter Jackson",
                2001,
                "Um hobbit dócil do Condado e oito companheiros partem em uma jornada para destruir o poderoso Um Anel e salvar a Terra-média do Lorde das Trevas Sauron.",
                "Fantasia / Aventura",
                "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?q=80&w=500&auto=format&fit=crop"
        );

        checkAndSaveFilme(
                "Parasita",
                "Bong Joon Ho",
                2019,
                "Toda a família de Ki-taek está desempregada, vivendo num porão sujo. Uma oportunidade faz o filho começar a dar aulas de inglês na mansão da rica família Park.",
                "Drama / Suspense",
                "https://images.unsplash.com/photo-1568605114967-8130f3a36994?q=80&w=500&auto=format&fit=crop"
        );

        checkAndSaveFilme(
                "Cidade de Deus",
                "Fernando Meirelles, Kátia Lund",
                2002,
                "Nas favelas do Rio de Janeiro, dois caminhos de jovens se separam: um luta para se tornar fotógrafo e documentar o local, o outro se torna um traficante de armas.",
                "Drama / Crime",
                "https://images.unsplash.com/photo-1518156677180-95a2893f3e9f?q=80&w=500&auto=format&fit=crop"
        );

        checkAndSaveFilme(
                "Forrest Gump: O Contador de Histórias",
                "Robert Zemeckis",
                1994,
                "Os presidenciais, a Guerra do Vietnã, o movimento Watergate e outros eventos históricos revelam-se sob a perspectiva de um homem do Alabama com um QI de 75.",
                "Drama / Romance",
                "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?q=80&w=500&auto=format&fit=crop"
        );

        checkAndSaveFilme(
                "Whiplash: Em Busca da Perfeição",
                "Damien Chazelle",
                2014,
                "Um jovem baterista promissor matricula-se em um conservatório de música de elite onde seus sonhos de grandeza são instruídos por um professor implacável.",
                "Drama / Música",
                "https://images.unsplash.com/photo-1511192336575-5a79af67a629?q=80&w=500&auto=format&fit=crop"
        );

        checkAndSaveFilme(
                "O Show de Truman: O Show da Vida",
                "Peter Weir",
                1998,
                "Um vendedor de seguros descobre que toda a sua vida é na verdade um reality show de televisão filmado por câmeras ocultas 24 horas por dia.",
                "Drama / Comédia",
                "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=500&auto=format&fit=crop"
        );

        checkAndSaveFilme(
                "Ilha do Medo",
                "Martin Scorsese",
                2010,
                "Em 1954, um xerife federal investiga o desaparecimento de uma assassina que escapou de um hospital psiquiátrico localizado em uma ilha isolada.",
                "Suspense / Mistério",
                "https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=500&auto=format&fit=crop"
        );

        checkAndSaveFilme(
                "Brilho Eterno de uma Mente sem Lembranças",
                "Michel Gondry",
                2004,
                "Quando seu relacionamento começa a desmoronar, um casal se submete a um procedimento médico experimental para apagar todas as lembranças um do outro.",
                "Romance / Ficção Científica",
                "https://images.unsplash.com/photo-1516339901601-2e1b62dc0c45?q=80&w=500&auto=format&fit=crop"
        );
    }

    private void checkAndSaveFilme(String titulo, String diretor, int ano, String sinopse, String genero, String imagemUrl) {
        if (!filmeRepository.existsByTitulo(titulo)) {
            filmeRepository.save(new Filme(titulo, diretor, ano, sinopse, genero, imagemUrl));
        }
    }
}
