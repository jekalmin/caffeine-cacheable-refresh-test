package com.github.jekalmin.foo;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Created by jekalmin on 2018. 10. 18..
 */
@Service
public class FooService {

	int i;

	@Cacheable("foo-cache")
	public String foo() {
		System.out.println("fetching foo" + i);
		try {
			Thread.sleep(5000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("success:" + i);
		return "foo" + i++;
	}

}
