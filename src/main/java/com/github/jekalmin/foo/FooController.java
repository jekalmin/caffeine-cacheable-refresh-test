package com.github.jekalmin.foo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by jekalmin on 2018. 10. 18..
 */
@RestController
public class FooController {

	@Autowired
	FooService fooService;

	@GetMapping
	public String foo() {
		return fooService.foo();
	}
}
