package io.leanddd;

import io.leanddd.component.data.impl.MetadataProviderImpl;
import io.leanddd.component.framework.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import javax.annotation.Resource;
import java.util.Locale;

@Configuration
@EnableAutoConfiguration
public class CommonConfig {

    @Resource
    ApplicationContext context;

    @Resource
    Environment env;

    @Value("${app.locale:en}")
    String locale;

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
        // messageSource.setDefaultLocale(Locale.ENGLISH);
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean("SessionLocalResolver")
    public LocaleResolver localeResolver() {
        var locale = Locale.forLanguageTag(this.locale);
        Locale.setDefault(locale);
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(locale);
        return slr;
    }

    @Bean
    @DependsOn("_Context")
    Object Init0() {
        System.out.println("Init0...");
        return new Object();
    }

    //init以前：创建表
    @Bean
    @DependsOn({"Init0", "SessionLocalResolver", "MetadataProvider"})
    // "taskExecutor"
    Object Init() {
        System.out.println("Init1...");
        return new Object();
    }

    //init2 以后的是可以后期初始化的，不被依赖，如loadData
    @Bean
    @DependsOn({"Init", "startUpHandlerImpl"})
    //userMapper
    Object Init2() {
        System.out.println("Init2...");
        return new Object();
    }

    @Bean("MetadataProvider")
    MetadataProvider metadataProvider(MessageSource messageSource) {
        return new MetadataProviderImpl(messageSource);
    }
}

