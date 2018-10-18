package com.github.jekalmin;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.github.jekalmin.caffeine.CaffeineCacheManagerFactoryBean;
import com.github.jekalmin.caffeine.TargetAwareKeyGenerator;

/**
 * Created by jekalmin on 2018. 10. 18..
 */
@EnableCaching
@Configuration
public class CacheConfig extends CachingConfigurerSupport {

	@Override
	public CacheManager cacheManager() {
		try {
			CaffeineCacheManagerFactoryBean bean = new CaffeineCacheManagerFactoryBean();
			bean.setConfigLocation(new ClassPathResource("caffeine.properties"));
			bean.afterPropertiesSet();
			return bean.getObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public KeyGenerator keyGenerator() {
		return new TargetAwareKeyGenerator();
	}
}
