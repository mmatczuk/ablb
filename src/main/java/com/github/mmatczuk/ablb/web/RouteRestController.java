package com.github.mmatczuk.ablb.web;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.mmatczuk.ablb.dispather.DispatcherService;

/**
 * @author mmatczuk
 */
@RestController
public class RouteRestController {
	private final DispatcherService mDispatcherService;

	@Autowired
	RouteRestController(DispatcherService dispatcherService) {
		mDispatcherService = dispatcherService;
	}

	@RequestMapping(value = "/route", method = RequestMethod.GET)
	public String route(@NotNull @RequestParam("id") final String userId) {
		return mDispatcherService.groupName(userId);
	}
}
