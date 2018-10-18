package com.github.jekalmin.caffeine;

import java.lang.reflect.Method;

import org.springframework.cache.interceptor.SimpleKey;

/**
 * Created by jekalmin on 2018. 10. 12..
 */
public class TargetAwareKey extends SimpleKey {
	private static final long serialVersionUID = -1013132832917334168L;
	private Object target;
	private Method method;
	private Object[] params;

	public TargetAwareKey(Object target, Method method, Object... params) {
		super(target, method, params);
		this.target = target;
		this.method = method;
		this.params = params;
	}

	public Object getTarget() {
		return target;
	}

	public Method getMethod() {
		return method;
	}

	public Object[] getParams() {
		return params;
	}
}
