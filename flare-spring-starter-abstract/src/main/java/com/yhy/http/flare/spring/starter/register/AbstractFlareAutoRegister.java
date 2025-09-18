package com.yhy.http.flare.spring.starter.register;

import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.such.interceptor.HttpLoggerInterceptor;
import com.yhy.http.flare.such.ssl.VoidSSLHostnameVerifier;
import com.yhy.http.flare.such.ssl.VoidSSLSocketFactory;
import com.yhy.http.flare.such.ssl.VoidSSLX509TrustManager;
import com.yhy.http.flare.utils.Opt;
import com.yhy.http.flare.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 自动扫描注册器
 * <p>
 * 当注册 @EnableFlare 注解后，自动扫描注册器会自动扫描并注册 @Flare 相关 Bean
 * <p>
 * Created on 2025-09-17 17:56
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractFlareAutoRegister implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {
    protected Environment environment;
    protected ResourceLoader resourceLoader;

    private Class<? extends Annotation> enableAnnotation;
    private Class<? extends Annotation> flareAnnotation;

    // 全局配置
    private String baseURL;
    private Map<String, List<String>> headerMap = new HashMap<>();
    private List<Class<? extends Header.Dynamic>> dynamicHeaderList = new ArrayList<>();
    private List<Class<? extends Interceptor>> interceptorList = new ArrayList<>();
    private List<Class<? extends Interceptor>> netInterceptorList = new ArrayList<>();
    private Boolean logEnabled;
    private Class<? extends Interceptor> loggerInterceptor;
    private long timeout;
    private Dispatcher dispatcher;
    private Class<? extends SSLSocketFactory> sslSocketFactory;
    private Class<? extends X509TrustManager> sslTrustManager;
    private Class<? extends HostnameVerifier> sslHostnameVerifier;

    @Override
    public void registerBeanDefinitions(@NotNull AnnotationMetadata metadata, @NotNull BeanDefinitionRegistry registry) {
        enableAnnotation = enableAnnotation();
        Assert.notNull(enableAnnotation, "The returned value of enableAnnotation() can not be null");
        flareAnnotation = flareAnnotation();
        Assert.notNull(flareAnnotation, "The returned value of flareAnnotation() can not be null");

        // 注册默认配置
        registerDefaultConfiguration(metadata, registry);
        // 注册 httpAgent
        registerHttpAgents(metadata, registry);
    }

    private void registerDefaultConfiguration(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(enableAnnotation.getCanonicalName());
        log.info("Loading global configuration for @{} from @{}: {}", flareAnnotation.getSimpleName(), enableAnnotation.getSimpleName(), attributes);
        if (!CollectionUtils.isEmpty(attributes)) {
            attributes.forEach((name, value) -> log.debug("Loaded global configuration for @{} from @{} {} = {}", flareAnnotation.getSimpleName(), enableAnnotation.getSimpleName(), name, value));
            // 加载...
            // 这里是全局配置
            AnnotationAttributes[] annotationAttributes = (AnnotationAttributes[]) attributes.get("interceptor");
            baseURL = getBaseUrl(attributes);
            headerMap = getHeader(attributes);
            interceptorList = getInterceptors(annotationAttributes, false);
            netInterceptorList = getInterceptors(annotationAttributes, true);
            logEnabled = getLogEnabled(attributes);
            loggerInterceptor = getLoggerInterceptor(attributes);
            timeout = getTimeout(attributes);
            sslSocketFactory = getSSLSocketFactory(attributes);
            sslTrustManager = getSSLTrustManager(attributes);
            sslHostnameVerifier = getSSLHostnameVerifier(attributes);
            dynamicHeaderList = dynamicHeaderList(attributes);
        }
        log.info("The global configuration for @{} from @{} loaded.", flareAnnotation.getSimpleName(), enableAnnotation.getSimpleName());
    }

    private void registerHttpAgents(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(flareAnnotation));

        Set<String> basePackages = getBasePackages(metadata);
        for (String pkg : basePackages) {
            Set<BeanDefinition> candidates = scanner.findCandidateComponents(pkg);
            for (BeanDefinition candidate : candidates) {
                if (candidate instanceof AnnotatedBeanDefinition definition) {
                    AnnotationMetadata meta = definition.getMetadata();
                    Assert.isTrue(meta.isInterface(), "@" + flareAnnotation.getSimpleName() + " can only be specified on an interface.");
                    Map<String, Object> attrs = meta.getAnnotationAttributes(flareAnnotation.getCanonicalName());
                    log.info("Scanning @{} candidate [{}], attrs = {}", flareAnnotation.getSimpleName(), candidate.getBeanClassName(), attrs);

                    registerHttpAgent(registry, meta, attrs);
                }
            }
        }
    }

    private void registerHttpAgent(BeanDefinitionRegistry registry, AnnotationMetadata meta, Map<String, Object> attrs) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(FlareFactoryBean.class);
        String className = meta.getClassName();
        String name = getName(attrs);
        String qualifier = getQualifier(attrs);
        AnnotationAttributes[] interceptors = (AnnotationAttributes[]) attrs.get("interceptor");

        builder.addPropertyValue("flareAnnotation", flareAnnotation);
        builder.addPropertyValue("flareInterface", className);
        builder.addPropertyValue("baseUrl", getBaseUrl(attrs));
        builder.addPropertyValue("headers", getHeader(attrs));
        builder.addPropertyValue("dynamicHeaderList", dynamicHeaderList(attrs));
        builder.addPropertyValue("interceptors", getInterceptors(interceptors, false));
        builder.addPropertyValue("netInterceptors", getInterceptors(interceptors, true));
        builder.addPropertyValue("timeout", getTimeout(attrs));
        builder.addPropertyValue("logEnabled", getLogEnabled(attrs));
        builder.addPropertyValue("loggerInterceptor", getLoggerInterceptor(attrs));
        builder.addPropertyValue("sslSocketFactory", getSSLSocketFactory(attrs));
        builder.addPropertyValue("sslTrustManager", getSSLTrustManager(attrs));
        builder.addPropertyValue("sslHostnameVerifier", getSSLHostnameVerifier(attrs));
        builder.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE);
        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        builder.setPrimary((Boolean) attrs.get("primary"));

        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        String alias = StringUtils.hasText(qualifier) ? qualifier : StringUtils.hasText(name) ? name : className + flareAnnotation.getSimpleName();

        // 注入 bean
        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, className, new String[]{alias});
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }

    private String getBaseUrl(Map<String, Object> attrs) {
        return Opt.ofNullable(resolve((String) attrs.get("baseUrl"))).orElse(baseURL);
    }

    private String getName(Map<String, Object> attrs) {
        return Opt.ofNullable(resolve((String) attrs.get("name"))).or(() -> Opt.ofNullable(resolve((String) attrs.get("value")))).orElse("");
    }

    private String getQualifier(Map<String, Object> attrs) {
        return (String) attrs.get("qualifier");
    }

    private Map<String, List<String>> getHeader(Map<String, Object> attrs) {
        AnnotationAttributes[] headers = (AnnotationAttributes[]) attrs.get("header");
        if (null != headers && headers.length > 0) {
            Map<String, List<String>> temp = Stream.of(headers).map(attr -> attr.getAnnotation("pair")).collect(Collectors.groupingBy(attr -> attr.getString("name"), Collectors.mapping(attr -> resolve(attr.getString("value")), Collectors.toList())));
            if (CollectionUtils.isEmpty(temp)) {
                temp = headerMap;
            } else if (!CollectionUtils.isEmpty(headerMap)) {
                temp.putAll(headerMap);
            }
            return temp;
        }
        return headerMap;
    }

    private List<Class<? extends Interceptor>> getInterceptors(AnnotationAttributes[] interceptors, boolean net) {
        List<Class<? extends Interceptor>> global = net ? netInterceptorList : interceptorList;
        if (null != interceptors && interceptors.length > 0) {
            List<Class<? extends Interceptor>> temp = Stream.of(interceptors).filter(it -> net && it.getBoolean("net") || !net && !it.getBoolean("net")).map(it -> it.<Interceptor>getClass("value")).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(temp)) {
                return global;
            }
            if (!CollectionUtils.isEmpty(global)) {
                // 全局拦截器优先加载
                temp.addAll(global);
            }
            return temp;
        }
        return global;
    }

    private long getTimeout(Map<String, Object> attrs) {
        // 优先使用 @Flare 作用域
        String str = resolve((String) attrs.get("timeout"));
        if (StringUtils.isNumeric(str)) {
            long temp = Long.parseLong(str);
            return temp > 0 ? temp : timeout;
        }
        return timeout;
    }

    private Boolean getLogEnabled(Map<String, Object> attrs) {
        // logEnabled == null 时为初始状态
        // 全局和局部同时开启时才启用
        String str = resolve((String) attrs.get("logEnabled"));
        if (StringUtils.isBoolean(str)) {
            boolean temp = Boolean.parseBoolean(str.toLowerCase());
            return (null == logEnabled || logEnabled) && temp;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Interceptor> getLoggerInterceptor(Map<String, Object> attrs) {
        // 优先使用 @Flare 作用域
        Class<? extends Interceptor> temp = (Class<? extends Interceptor>) attrs.get("loggerInterceptor");
        return null != temp && temp != HttpLoggerInterceptor.class ? temp : loggerInterceptor;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends SSLSocketFactory> getSSLSocketFactory(Map<String, Object> attrs) {
        // 优先使用 @Flare 作用域
        Class<? extends SSLSocketFactory> temp = (Class<? extends SSLSocketFactory>) attrs.get("sslSocketFactory");
        return null != temp && temp != VoidSSLSocketFactory.class ? temp : sslSocketFactory;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends X509TrustManager> getSSLTrustManager(Map<String, Object> attrs) {
        // 优先使用 @Flare 作用域
        Class<? extends X509TrustManager> temp = (Class<? extends X509TrustManager>) attrs.get("sslTrustManager");
        return null != temp && temp != VoidSSLX509TrustManager.class ? temp : sslTrustManager;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends HostnameVerifier> getSSLHostnameVerifier(Map<String, Object> attrs) {
        // 优先使用 @Flare 作用域
        Class<? extends HostnameVerifier> temp = (Class<? extends HostnameVerifier>) attrs.get("sslHostnameVerifier");
        return null != temp && temp != VoidSSLHostnameVerifier.class ? temp : sslHostnameVerifier;
    }

    public List<Class<? extends Header.Dynamic>> dynamicHeaderList(Map<String, Object> attrs) {
        AnnotationAttributes[] headers = (AnnotationAttributes[]) attrs.get("header");
        if (null != headers && headers.length > 0) {
            List<Class<? extends Header.Dynamic>> temp = Stream.of(headers).filter(it -> it.getClass("dynamic") != Header.Dynamic.class).map(it -> it.<Header.Dynamic>getClass("dynamic")).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(temp)) {
                temp = dynamicHeaderList;
            } else if (!CollectionUtils.isEmpty(dynamicHeaderList)) {
                temp.addAll(dynamicHeaderList);
            }
            return temp;
        }
        return dynamicHeaderList;
    }

    @Override
    public void setEnvironment(@NotNull Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(@NotNull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private String resolve(String value) {
        // 解析占位符
        // 判断处理 Spring 配置变量 ${xxx.xxx}
        if (StringUtils.isPlaceholdersPresent(value)) {
            return environment.resolvePlaceholders(value);
        }
        return value;
    }

    private Set<String> getBasePackages(AnnotationMetadata metadata) {
        Set<String> basePackages = new HashSet<>();
        Map<String, Object> attributes = metadata.getAnnotationAttributes(enableAnnotation.getCanonicalName());
        if (null != attributes) {
            Object value = attributes.get("value");
            if (null != value) {
                for (String pkg : (String[]) value) {
                    if (org.springframework.util.StringUtils.hasText(pkg)) {
                        basePackages.add(pkg);
                    }
                }
            }
            value = attributes.get("basePackages");
            if (null != value) {
                for (String pkg : (String[]) attributes.get("basePackages")) {
                    if (org.springframework.util.StringUtils.hasText(pkg)) {
                        basePackages.add(pkg);
                    }
                }
            }
            for (Class<?> clazz : (Class<?>[]) attributes.get("basePackageClasses")) {
                basePackages.add(ClassUtils.getPackageName(clazz));
            }
            if (basePackages.isEmpty()) {
                basePackages.add(ClassUtils.getPackageName(metadata.getClassName()));
            }
        }
        return basePackages;
    }

    private ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(@NotNull AnnotatedBeanDefinition beanDefinition) {
                return beanDefinition.getMetadata().isIndependent() && !beanDefinition.getMetadata().isAnnotation();
            }
        };
    }

    public abstract Class<? extends Annotation> enableAnnotation();

    public abstract Class<? extends Annotation> flareAnnotation();
}
