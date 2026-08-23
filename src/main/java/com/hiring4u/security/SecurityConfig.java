package com.hiring4u.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setDefaultTargetUrl("/?login=success");
        return successHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationSuccessHandler authenticationSuccessHandler) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login",
                                "/login.html",
                                "/signup.html",
                                "/forgot-password.html",
                                "/reset-password.html",
                                "/css/**",
                                "/js/**",
                                "/assets/**",
                                "/images/**",
                                "/favicon.ico",
                                "/actuator/health/**"
                        ).permitAll()

                        .requestMatchers("/candidate/jobs").permitAll()

                        .requestMatchers("/api/auth/password/**").permitAll()

                        .requestMatchers("/profile.html").hasAnyRole("CANDIDATE", "RECRUITER")
                        .requestMatchers("/api/profile/**").hasAnyRole("CANDIDATE", "RECRUITER")

                        .requestMatchers("/can/**").permitAll()
                        .requestMatchers("/rec/**").permitAll()

                        .requestMatchers("/post-job.html").hasRole("RECRUITER")
                        .requestMatchers("/candidate/**").hasRole("CANDIDATE")
                        .requestMatchers("/recruiter/**").hasRole("RECRUITER")
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(authenticationSuccessHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessUrl("/?logout=success")
                        .permitAll())
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler((request, response, exception) -> response.sendRedirect("/?error=recruiter-required")));

        return http.build();
    }
}
