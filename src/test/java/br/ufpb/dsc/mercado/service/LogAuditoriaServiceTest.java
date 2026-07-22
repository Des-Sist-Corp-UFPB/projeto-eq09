package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.LogAuditoria;
import br.ufpb.dsc.mercado.repository.LogAuditoriaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogAuditoriaServiceTest {

    @Mock
    private LogAuditoriaRepository logRepository;

    @InjectMocks
    private LogAuditoriaService logAuditoriaService;

    private SecurityContext originalSecurityContext;

    @BeforeEach
    void setUp() {
        originalSecurityContext = SecurityContextHolder.getContext();
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(originalSecurityContext);
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void testRegistrarLog_WithSecurityContextAndRequestHeaders() {
        // Setup Security Context
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("authenticated_user");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Setup Request Context
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.0.1, 10.0.0.1");
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);

        logAuditoriaService.registrarLog("TEST_ACTION", "Detailed logs here");

        verify(logRepository, times(1)).save(argThat(log -> 
            "authenticated_user".equals(log.getUsuario()) &&
            "TEST_ACTION".equals(log.getAcao()) &&
            "Detailed logs here".equals(log.getDetalhes()) &&
            "192.168.0.1".equals(log.getIp())
        ));
    }

    @Test
    void testRegistrarLog_AnonymousUserAndRemoteAddr() {
        // Setup Security Context for Anonymous User
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Setup Request Context with no headers but remote address
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn("");
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn("unknown");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);

        logAuditoriaService.registrarLog("ANONYMOUS_ACTION", "Logs");

        verify(logRepository, times(1)).save(argThat(log -> 
            log.getUsuario() == null &&
            "ANONYMOUS_ACTION".equals(log.getAcao()) &&
            "127.0.0.1".equals(log.getIp())
        ));
    }

    @Test
    void testRegistrarLog_WithExplicitUserAndNoRequestContext() {
        // No Request Context, no Security Context
        logAuditoriaService.registrarLog("explicit_user", "LOGIN_FALHA", "Credentials error");

        verify(logRepository, times(1)).save(argThat(log -> 
            "explicit_user".equals(log.getUsuario()) &&
            "LOGIN_FALHA".equals(log.getAcao()) &&
            "Credentials error".equals(log.getDetalhes()) &&
            log.getIp() == null
        ));
    }

    @Test
    void testRegistrarLog_WithNullAuthentication() {
        SecurityContextHolder.clearContext();
        logAuditoriaService.registrarLog("TEST_ACTION", "Details");
        verify(logRepository, times(1)).save(argThat(log -> log.getUsuario() == null));
    }

    @Test
    void testRegistrarLog_WithUnauthenticatedUser() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        logAuditoriaService.registrarLog("TEST_ACTION", "Details");
        verify(logRepository, times(1)).save(argThat(log -> log.getUsuario() == null));
    }

    @Test
    void testRegistrarLog_WithProxyClientIP() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn("192.168.1.100");
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);

        logAuditoriaService.registrarLog("TEST_ACTION", "Details");
        verify(logRepository, times(1)).save(argThat(log -> "192.168.1.100".equals(log.getIp())));
    }

    @Test
    void testRegistrarLog_WithWLProxyClientIP() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn("");
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn("192.168.1.200");
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);

        logAuditoriaService.registrarLog("TEST_ACTION", "Details");
        verify(logRepository, times(1)).save(argThat(log -> "192.168.1.200".equals(log.getIp())));
    }

    @Test
    void testRegistrarLog_WithSingleIpNoComma() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.50");
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);

        logAuditoriaService.registrarLog("TEST_ACTION", "Details");
        verify(logRepository, times(1)).save(argThat(log -> "192.168.1.50".equals(log.getIp())));
    }
}
