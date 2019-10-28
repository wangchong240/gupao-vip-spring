package com.gupaoedu.demo.mvc.action;

import com.gupaoedu.demo.service.IDemoService;
import com.gupaoedu.mvcframework.annotation.WCAutowired;
import com.gupaoedu.mvcframework.annotation.WCController;
import com.gupaoedu.mvcframework.annotation.WCRequestMapping;
import com.gupaoedu.mvcframework.annotation.WCRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WCController
@WCRequestMapping("/demo")
public class DemoAction {

  	@WCAutowired
	private IDemoService demoService;

	@WCRequestMapping("/query")
	public void query(HttpServletRequest req, HttpServletResponse resp,
					  @WCRequestParam("name") String name){
		String result = demoService.get(name);
//		String result = "My name is " + name;
		try {
			resp.getWriter().write(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@WCRequestMapping("/add")
	public void add(HttpServletRequest req, HttpServletResponse resp,
					@WCRequestParam("a") Integer a, @WCRequestParam("b") Integer b){
		try {
			resp.getWriter().write(a + "+" + b + "=" + (a + b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@WCRequestMapping("/remove")
	public void remove(HttpServletRequest req,HttpServletResponse resp,
					   @WCRequestParam("id") Integer id){
	}

}
