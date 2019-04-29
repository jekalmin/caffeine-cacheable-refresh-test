package com.github.jekalmin.caffeine;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.PropertyResourceBundle;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Created by jekalmin on 2018. 10. 12..
 */
public class CaffeineCacheManagerFactoryBean implements FactoryBean<NewsCaffeineCacheManager>, InitializingBean {

	@Nullable
	protected Resource configLocation;

	@Nullable
	protected NewsCaffeineCacheManager cacheManager;

	protected boolean dynamic = false;

	@Override
	public NewsCaffeineCacheManager getObject() throws Exception {
		return this.cacheManager;
	}

	@Override
	public Class<? extends NewsCaffeineCacheManager> getObjectType() {
		return (this.cacheManager != null ? this.cacheManager.getClass() : NewsCaffeineCacheManager.class);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.cacheManager == null) {
			buildCacheManager();
		}
	}

	private void buildCacheManager() throws IOException {
		this.cacheManager = new NewsCaffeineCacheManager();
		this.cacheManager.dynamic = this.dynamic;

		if (this.configLocation != null) {
			CacheLoader<Object, Object> cacheLoader = new CacheLoader<Object, Object>() {
				@Override
				public Object load(Object key) throws Exception {
					return null;
				}

				@Override
				public Object reload(Object key, Object oldValue) throws Exception {
					TargetAwareKey simpleCacheKey = (TargetAwareKey)key;
					Object target = simpleCacheKey.getTarget();
					Method method = simpleCacheKey.getMethod();
					Object[] params = simpleCacheKey.getParams();
					Object returnValue = method.invoke(target, params);
					if (returnValue != null) {
						return returnValue;
					}
					return oldValue;
				}
			};

			PropertyResourceBundle property = new PropertyResourceBundle(this.configLocation.getInputStream());

			for (Enumeration<String> keys = property.getKeys(); keys.hasMoreElements(); ) {
				String key = keys.nextElement();
				String value = property.getString(key);

				Caffeine<Object, Object> builder = Caffeine.from(value);
				Cache<Object, Object> cache = null;
				if (value.contains("refresh")) {
					cache = builder.build(cacheLoader);
				} else {
					cache = builder.build();
				}
				this.cacheManager.addCache(key, new CaffeineCache(key, cache, true));
			}
		}
	}

	@Nullable
	public Resource getConfigLocation() {
		return configLocation;
	}

	public void setConfigLocation(@Nullable Resource configLocation) {
		this.configLocation = configLocation;
	}

	@Nullable
	public NewsCaffeineCacheManager getCacheManager() {
		return cacheManager;
	}

	public void setCacheManager(@Nullable NewsCaffeineCacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}
}
