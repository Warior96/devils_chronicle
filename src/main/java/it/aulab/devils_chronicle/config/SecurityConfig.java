package it.aulab.devils_chronicle.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import it.aulab.devils_chronicle.services.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private CustomUserDetailsService customUserDetailsService;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(
                                                (authorize) -> authorize
                                                                // Static resources - permit all
                                                                .requestMatchers("/css/**", "/js/**", "/images/**",
                                                                                "/static/**")
                                                                .permitAll()
                                                                .requestMatchers("/register/**").permitAll()
                                                                // Admin routes - only for ADMIN role
                                                                .requestMatchers("/admin/dashboard",
                                                                                "/categories/create",
                                                                                "/categories/edit/{id}",
                                                                                "/categories/delete/{id}")
                                                                .hasRole("ADMIN")
                                                                // Revisor routes - only for REVISOR role
                                                                .requestMatchers("/revisor/dashboard",
                                                                                "/revisor/detail/{id}", "/accept")
                                                                .hasRole("REVISOR")
                                                                // Writer routes - only for WRITER role
                                                                .requestMatchers("/writer/dashboard",
                                                                                "/articles/create",
                                                                                "/articles/update/{id}",
                                                                                "/articles/delete/{id}")
                                                                .hasRole("WRITER")
                                                                // Public routes - permit all
                                                                .requestMatchers("/register", "/", "/articles",
                                                                                "/images/**", "/articles/detail/**",
                                                                                "/categories/search/{id}",
                                                                                "/search/{id}", "/articles/search")
                                                                .permitAll()
                                                                // Any other request - authenticated users
                                                                .anyRequest().authenticated())
                                .formLogin(form -> form.loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .defaultSuccessUrl("/")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                                .permitAll())
                                .exceptionHandling(exception -> exception.accessDeniedPage("/error/403"))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                                .maximumSessions(1)
                                                .expiredUrl("/login?session-expired=true"));
                return http.build();
        }

        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
                auth.userDetailsService(customUserDetailsService)
                                .passwordEncoder(passwordEncoder);
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
                        throws Exception {
                return authenticationConfiguration.getAuthenticationManager();
        }

}
