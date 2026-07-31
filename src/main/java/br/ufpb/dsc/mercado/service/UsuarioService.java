package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.dto.AuthRequest;
import br.ufpb.dsc.mercado.dto.AuthResponse;
import br.ufpb.dsc.mercado.dto.RegisterRequest;
import br.ufpb.dsc.mercado.repository.UsuarioRepository;
import br.ufpb.dsc.mercado.config.security.JwtTokenProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final LogAuditoriaService logAuditoriaService;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                          JwtTokenProvider tokenProvider, @Lazy AuthenticationManager authenticationManager,
                          LogAuditoriaService logAuditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.logAuditoriaService = logAuditoriaService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o username: " + username));

        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRole()))
        );
    }

    @Transactional
    public void registrar(RegisterRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("O nome de usuário já está sendo utilizado.");
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        String role = "USER";

        Usuario usuario = new Usuario(request.username(), encodedPassword, role);
        usuarioRepository.save(usuario);
        
        logAuditoriaService.registrarLog(request.username(), "REGISTRO_USUARIO", "Usuário cadastrado com sucesso.");
    }

    @Transactional
    public AuthResponse autenticar(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = tokenProvider.generateToken(authentication);

            Usuario usuario = usuarioRepository.findByUsername(request.username())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + request.username()));

            logAuditoriaService.registrarLog(usuario.getUsername(), "LOGIN_SUCESSO", "Usuário autenticado com sucesso.");

            return new AuthResponse(token, usuario.getUsername(), usuario.getRole());
        } catch (Exception e) {
            logAuditoriaService.registrarLog(request.username(), "LOGIN_FALHA", "Tentativa de login falhou: " + e.getMessage());
            throw e;
        }
    }
}
