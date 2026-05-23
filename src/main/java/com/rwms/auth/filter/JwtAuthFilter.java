package com.rwms.auth.filter;

import com.rwms.auth.util.JwtUtil;
import com.rwms.user.entity.User;
import com.rwms.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired token");
            return;
        }

        String email = jwtUtil.extractEmail(token);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            userRepository.findByEmail(email).ifPresent(user -> {

                if (user.getStatus() != User.Status.ACTIVE) {
                    return;
                }

                String role = "ROLE_" + user.getRole().name();
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of(new SimpleGrantedAuthority(role)));
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            });
        }

        filterChain.doFilter(request, response);
    }
}
