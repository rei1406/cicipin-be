package com.cicipin.userservice.common.versioning;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;
import java.util.Locale;

@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {

    @Override
    protected RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
        return new VersionRequestMappingHandlerMapping();
    }

    @Override
    public AcceptHeaderLocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver() {
            @Override
            public Locale resolveLocale(HttpServletRequest request) {
                String lang = request.getHeader("X-Locale");
                if (lang != null && !lang.isEmpty()) {
                    return Locale.forLanguageTag(lang);
                }
                return super.resolveLocale(request);
            }
        };
        resolver.setSupportedLocales(List.of(new Locale("en"), new Locale("id")));
        resolver.setDefaultLocale(Locale.ENGLISH);
        return resolver;
    }
}
