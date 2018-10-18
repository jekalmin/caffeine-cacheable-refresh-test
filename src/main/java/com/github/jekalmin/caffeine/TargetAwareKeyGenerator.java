package com.github.jekalmin.caffeine;

import java.lang.reflect.Method;

import org.springframework.cache.interceptor.KeyGenerator;

/**
 * Created by jekalmin on 2018. 10. 12..
 */
public class TargetAwareKeyGenerator implements KeyGenerator {
	@Override
	public Object generate(Object target, Method method, Object... params) {
		return new TargetAwareKey(target, method, params);
	}
}
