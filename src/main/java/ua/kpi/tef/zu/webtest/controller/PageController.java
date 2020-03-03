package ua.kpi.tef.zu.webtest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Anton Domin on 2020-03-04
 */
@Controller
public class PageController {
	@RequestMapping("/")
	public String mainPage(){
		return "index.html";
	}
}

