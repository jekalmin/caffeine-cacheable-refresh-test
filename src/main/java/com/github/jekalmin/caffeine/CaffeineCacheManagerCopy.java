package com.github.jekalmin.caffeine;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;

/**
 * Copy of {@link org.springframework.cache.caffeine.CaffeineCacheManager}
 * just changed private modifier of fields to protected
 *
 * Created by jekalmin on 2018. 10. 12..
 */
public class CaffeineCacheManagerCopy implements CacheManager {

	protected final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>(16);

	protected boolean dynamic = true;

	protected Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder();

	@Nullable
	protected CacheLoader<Object, Object> cacheLoader;

	protected boolean allowNullValues = true;


	/**
	 * Construct a dynamic CaffeineCacheManager,
	 * lazily creating cache instances as they are being requested.
	 */
	public CaffeineCacheManagerCopy() {
	}

	/**
	 * Construct a static CaffeineCacheManager,
	 * managing caches for the specified cache names only.
	 */
	public CaffeineCacheManagerCopy(String... cacheNames) {
		setCacheNames(Arrays.asList(cacheNames));
	}


	/**
	 * Specify the set of cache names for this CacheManager's 'static' mode.
	 * <p>The number of caches and their names will be fixed after a call to this method,
	 * with no creation of further cache regions at runtime.
	 * <p>Calling this with a {@code null} collection argument resets the
	 * mode to 'dynamic', allowing for further creation of caches again.
	 */
	public void setCacheNames(@Nullable Collection<String> cacheNames) {
		if (cacheNames != null) {
			for (String name : cacheNames) {
				this.cacheMap.put(name, createCaffeineCache(name));
			}
			this.dynamic = false;
		}
		else {
			this.dynamic = true;
		}
	}

	/**
	 * Set the Caffeine to use for building each individual
	 * {@link CaffeineCache} instance.
	 * @see #createNativeCaffeineCache
	 * @see com.github.benmanes.caffeine.cache.Caffeine#build()
	 */
	public void setCaffeine(Caffeine<Object, Object> caffeine) {
		Assert.notNull(caffeine, "Caffeine must not be null");
		doSetCaffeine(caffeine);
	}

	/**
	 * Set the {@link CaffeineSpec} to use for building each individual
	 * {@link CaffeineCache} instance.
	 * @see #createNativeCaffeineCache
	 * @see com.github.benmanes.caffeine.cache.Caffeine#from(CaffeineSpec)
	 */
	public void setCaffeineSpec(CaffeineSpec caffeineSpec) {
		doSetCaffeine(Caffeine.from(caffeineSpec));
	}

	/**
	 * Set the Caffeine cache specification String to use for building each
	 * individual {@link CaffeineCache} instance. The given value needs to
	 * comply with Caffeine's {@link CaffeineSpec} (see its javadoc).
	 * @see #createNativeCaffeineCache
	 * @see com.github.benmanes.caffeine.cache.Caffeine#from(String)
	 */
	public void setCacheSpecification(String cacheSpecification) {
		doSetCaffeine(Caffeine.from(cacheSpecification));
	}

	/**
	 * Set the Caffeine CacheLoader to use for building each individual
	 * {@link CaffeineCache} instance, turning it into a LoadingCache.
	 * @see #createNativeCaffeineCache
	 * @see com.github.benmanes.caffeine.cache.Caffeine#build(CacheLoader)
	 * @see com.github.benmanes.caffeine.cache.LoadingCache
	 */
	public void setCacheLoader(CacheLoader<Object, Object> cacheLoader) {
		if (!ObjectUtils.nullSafeEquals(this.cacheLoader, cacheLoader)) {
			this.cacheLoader = cacheLoader;
			refreshKnownCaches();
		}
	}

	/**
	 * Specify whether to accept and convert {@code null} values for all caches
	 * in this cache manager.
	 * <p>Default is "true", despite Caffeine itself not supporting {@code null} values.
	 * An internal holder object will be used to store user-level {@code null}s.
	 */
	public void setAllowNullValues(boolean allowNullValues) {
		if (this.allowNullValues != allowNullValues) {
			this.allowNullValues = allowNullValues;
			refreshKnownCaches();
		}
	}

	/**
	 * Return whether this cache manager accepts and converts {@code null} values
	 * for all of its caches.
	 */
	public boolean isAllowNullValues() {
		return this.allowNullValues;
	}


	@Override
	public Collection<String> getCacheNames() {
		return Collections.unmodifiableSet(this.cacheMap.keySet());
	}

	@Override
	@Nullable
	public Cache getCache(String name) {
		Cache cache = this.cacheMap.get(name);
		if (cache == null && this.dynamic) {
			synchronized (this.cacheMap) {
				cache = this.cacheMap.get(name);
				if (cache == null) {
					cache = createCaffeineCache(name);
					this.cacheMap.put(name, cache);
				}
			}
		}
		return cache;
	}

	/**
	 * Create a new CaffeineCache instance for the specified cache name.
	 * @param name the name of the cache
	 * @return the Spring CaffeineCache adapter (or a decorator thereof)
	 */
	protected Cache createCaffeineCache(String name) {
		return new CaffeineCache(name, createNativeCaffeineCache(name), isAllowNullValues());
	}

	/**
	 * Create a native Caffeine Cache instance for the specified cache name.
	 * @param name the name of the cache
	 * @return the native Caffeine Cache instance
	 */
	protected com.github.benmanes.caffeine.cache.Cache<Object, Object> createNativeCaffeineCache(String name) {
		if (this.cacheLoader != null) {
			return this.cacheBuilder.build(this.cacheLoader);
		}
		else {
			return this.cacheBuilder.build();
		}
	}

	private void doSetCaffeine(Caffeine<Object, Object> cacheBuilder) {
		if (!ObjectUtils.nullSafeEquals(this.cacheBuilder, cacheBuilder)) {
			this.cacheBuilder = cacheBuilder;
			refreshKnownCaches();
		}
	}

	/**
	 * Create the known caches again with the current state of this manager.
	 */
	private void refreshKnownCaches() {
		for (Map.Entry<String, Cache> entry : this.cacheMap.entrySet()) {
			entry.setValue(createCaffeineCache(entry.getKey()));
		}
	}
}
