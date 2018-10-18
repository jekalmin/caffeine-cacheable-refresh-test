package com.github.jekalmin.caffeine;

import org.springframework.cache.Cache;

/**
 * Created by jekalmin on 2018. 10. 12..
 */
public class NewsCaffeineCacheManager extends CaffeineCacheManagerCopy {

	public void addCache(String key, Cache cache) {
		this.cacheMap.put(key, cache);
	}
}
