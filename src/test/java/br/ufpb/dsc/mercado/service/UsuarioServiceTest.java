package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.dto.AuthRequest;
import br.ufpb.dsc.mercado.dto.AuthResponse;
import br.ufpb.dsc.mercado.dto.RegisterRequest;
import br.ufpb.dsc.mercado.repository.UsuarioRepository;
import br.ufpb.dsc.mercado.config.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private LogAuditoriaService logAuditoriaService;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("john_doe", "encrypted_pass", "USER");
        usuario.setId(1L);
    }

    @Test
    void testLoadUserByUsername_Success() {
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = usuarioService.loadUserByUsername("john_doe");

        assertNotNull(userDetails);
        assertEquals("john_doe", userDetails.getUsername());
        assertEquals("encrypted_pass", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(usuarioRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            usuarioService.loadUserByUsername("unknown");
        });
    }

    @Test
    void testRegistrar_Success() {
        RegisterRequest request = new RegisterRequest("new_user", "password123");
        when(usuarioRepository.existsByUsername("new_user")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_new_pass");

        usuarioService.registrar(request);

        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(logAuditoriaService, times(1)).registrarLog("new_user", "REGISTRO_USUARIO", "Usuário cadastrado com sucesso.");
    }

    @Test
    void testRegistrar_DuplicateUsername() {
        RegisterRequest request = new RegisterRequest("john_doe", "password123");
        when(usuarioRepository.existsByUsername("john_doe")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.registrar(request);
        });

        assertEquals("O nome de usuário já está sendo utilizado.", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void testAutenticar_Success() {
        AuthRequest request = new AuthRequest("john_doe", "password123");
        Authentication authentication = mock(Authentication.class);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt_token_abc");
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));

        AuthResponse response = usuarioService.autenticar(request);

        assertNotNull(response);
        assertEquals("jwt_token_abc", response.token());
        assertEquals("john_doe", response.username());
        assertEquals("USER", response.role());
        
        verify(logAuditoriaService, times(1)).registrarLog("john_doe", "LOGIN_SUCESSO", "Usuário autenticado com sucesso.");
    }

    @Test
    void testAutenticar_Failure() {
        AuthRequest request = new AuthRequest("john_doe", "wrong_pass");
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> {
            usuarioService.autenticar(request);
        });

        verify(logAuditoriaService, times(1)).registrarLog("john_doe", "LOGIN_FALHA", "Tentativa de login falhou: Bad credentials");
    }
}
