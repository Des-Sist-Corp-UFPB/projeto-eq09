package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.LogAuditoria;
import br.ufpb.dsc.mercado.repository.LogAuditoriaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class LogAuditoriaService {

    private final LogAuditoriaRepository logRepository;

    public LogAuditoriaService(LogAuditoriaRepository logRepository) {
        this.logRepository = logRepository;
    }

    /**
     * Records an audit log resolving the username and IP address automatically from the context.
     * Uses Propagation.REQUIRES_NEW to ensure logs are saved even if the outer transaction rolls back.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarLog(String acao, String detalhes) {
        String username = getCurrentUsername();
        String ip = getClientIp();
        
        LogAuditoria log = new LogAuditoria(username, acao, detalhes, ip);
        logRepository.save(log);
    }

    /**
     * Records an audit log with an explicitly specified username.
     * Useful for registration or login failure scenarios.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarLog(String username, String acao, String detalhes) {
        String ip = getClientIp();
        
        LogAuditoria log = new LogAuditoria(username, acao, detalhes, ip);
        logRepository.save(log);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return null;
    }

    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            // If comma-separated (multiple proxies), get the first one
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        }
        return null;
    }
}
