package com.darro_tech.revengproject.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Primary;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Thymeleaf configuration to enable access to request and session in Thymeleaf
 * templates.
 */
@Configuration
public class ThymeleafConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(ThymeleafConfig.class);

    /**
     * Configure RequestContextFilter to make HttpServletRequest and HttpSession
     * available.
     */
    @Bean
    @Description("Exposes request context to enable session access in Thymeleaf templates")
    public FilterRegistrationBean<RequestContextFilter> requestContextFilter() {
        logger.info("🔧 Registering RequestContextFilter to enable session access in templates");
        FilterRegistrationBean<RequestContextFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestContextFilter());
        registrationBean.setOrder(0);
        return registrationBean;
    }

    /**
     * Primary template resolver for templates in /templates directory
     */
    @Bean
    @Primary
    public SpringResourceTemplateResolver primaryTemplateResolver() {
        logger.info("🔧 Configuring primary template resolver for templates");
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("classpath:/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setOrder(0);
        resolver.setCacheable(false);
        resolver.setCheckExistence(true);
        return resolver;
    }

    /**
     * Set up the SpringTemplateEngine with configured dialects and session
     * support.
     */
    @Bean
    @Description("Configures Thymeleaf template engine with session support")
    public SpringTemplateEngine templateEngine(SpringResourceTemplateResolver templateResolver) {
        logger.info("🔧 Initializing Thymeleaf template engine with session support");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver);
        engine.setEnableSpringELCompiler(true);
        return engine;
    }

    @Bean
    public ViewResolver thymeleafViewResolver(SpringTemplateEngine templateEngine) {
        logger.info("🔧 Configuring Thymeleaf view resolver");
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setContentType("text/html");
        resolver.setViewNames(new String[]{"*"});
        return resolver;
    }
}
