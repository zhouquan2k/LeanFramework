package io.leanddd.component.spring;

import io.leanddd.component.framework.BeanMgr;
import io.leanddd.component.framework.Context;
import io.leanddd.component.framework.Event;
import io.leanddd.component.framework.SecurityUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import javax.annotation.Resource;
import java.util.Locale;

@Configuration
public class CommonConfig {

    @Resource
    ApplicationContext context;

    @Resource
    Environment env;

    @Bean("_Context")
    Context createContext(SecurityUtil securityUtil) {
        return new Context(securityUtil, new BeanMgr() {

            @Override
            public <T> T getBean(Class<T> cls) {
                return context.getBean(cls);
            }

            @Override
            public <T> T getBean(String name, Class<T> cls) {
                return context.getBean(name, cls);
            }

            @Override
            public void publishEvent(Event event) {
                context.publishEvent(event);
            }

            @Override
            public String getProperty(String name) {
                return env.getProperty(name);
            }
        });
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultLocale(Locale.ENGLISH);
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean("SessionLocalResolver")
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.ENGLISH);
        return slr;
    }
}

