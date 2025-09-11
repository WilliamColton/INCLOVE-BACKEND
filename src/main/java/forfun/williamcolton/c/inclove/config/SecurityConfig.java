package forfun.williamcolton.c.inclove.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import forfun.williamcolton.c.inclove.filter.JwtAuthFilter;
import forfun.williamcolton.c.inclove.global.GlobalApiResponse;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static forfun.williamcolton.c.inclove.exception.AuthErrorCode.NO_PERMISSION;
import static forfun.williamcolton.c.inclove.exception.AuthErrorCode.UNAUTHORIZED;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }


    @Bean
    public ApplicationRunner debugFilters(ApplicationContext ctx) {
        return args -> {
            System.out.println("=== All Filters in Spring Context ===");
            ctx.getBeansOfType(Filter.class).forEach((name, filter) -> {
                System.out.printf("bean=%s, class=%s%n", name, filter.getClass().getName());
            });

            System.out.println("=== All FilterRegistrationBeans (with order) ===");
            ctx.getBeansOfType(FilterRegistrationBean.class).forEach((name, frb) -> {
                System.out.printf("reg=%s, filter=%s, order=%d%n",
                        name,
                        frb.getFilter().getClass().getName(),
                        frb.getOrder());
            });
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((_, res, _) -> {
                            res.setStatus(HttpServletResponse.SC_OK);
                            res.setContentType("application/json;charset=UTF-8");
                            var body = GlobalApiResponse.error(UNAUTHORIZED.getCode(), UNAUTHORIZED.getMessage());
                            res.getWriter().write(objectMapper.writeValueAsString(body));
                        })
                        .accessDeniedHandler((_, res, _) -> {
                            res.setStatus(HttpServletResponse.SC_OK);
                            res.setContentType("application/json;charset=UTF-8");
                            var body = GlobalApiResponse.error(NO_PERMISSION.getCode(), NO_PERMISSION.getMessage());
                            res.getWriter().write(objectMapper.writeValueAsString(body));
                        })
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOriginPatterns(List.of(
                "*"
        ));
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }

}
