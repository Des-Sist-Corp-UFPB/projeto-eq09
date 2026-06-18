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

        // Inicializa Filmes padrão se não existirem
        if (filmeRepository.count() == 0) {
            filmeRepository.save(new Filme(
                    "Inception",
                    "Christopher Nolan",
                    2010,
                    "Um ladrão que rouba segredos corporativos por meio do uso de tecnologia de compartilhamento de sonhos tem a chance de limpar seu histórico criminal.",
                    "Ficção Científica",
                    "https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=500&auto=format&fit=crop"
            ));

            filmeRepository.save(new Filme(
                    "O Poderoso Chefão",
                    "Francis Ford Coppola",
                    1972,
                    "O patriarca idoso de uma dinastia do crime organizado transfere o controle de seu império clandestino para seu filho relutante.",
                    "Drama",
                    "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=500&auto=format&fit=crop"
            ));

            filmeRepository.save(new Filme(
                    "Interestelar",
                    "Christopher Nolan",
                    2014,
                    "Uma equipe de exploradores viaja através de um buraco de minhoca no espaço na tentativa de garantir a sobrevivência da humanidade.",
                    "Ficção Científica",
                    "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=500&auto=format&fit=crop"
            ));

            filmeRepository.save(new Filme(
                    "Clube da Luta",
                    "David Fincher",
                    1999,
                    "Um funcionário de escritório insone e um criador de sabão descompromissado formam um clube de luta underground que evolui para algo muito maior.",
                    "Drama",
                    "https://images.unsplash.com/photo-1594909122845-11baa439b7bf?q=80&w=500&auto=format&fit=crop"
            ));
        }
    }
}
