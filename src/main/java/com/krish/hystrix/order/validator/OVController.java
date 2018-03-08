package com.krish.hystrix.order.validator;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.time.StopWatch;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

@RestController
@EnableHystrix
@EnableCircuitBreaker
@EnableHystrixDashboard
public class OVController {
	@RequestMapping("/validate")
	@Produces({ MediaType.TEXT_PLAIN })
	/*
	 * HystrixProperty(name="circuitBreaker.errorThresholdPercentage", value="20")
	 * Over 20 % failure rate in 10 sec period, open breaker
	 * 
	 * @HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds",
	 * value="1000") After 1 second , close the circuit breaker
	 * 
	 * @HystrixCommand(fallbackMethod = "autoValidate", commandProperties = {
			@HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "20"),
			@HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "1000") })
	 */
	@HystrixCommand(fallbackMethod = "autoValidate")
	public String validateOrder() {
		String response = "";
		try {
			StopWatch watch = new StopWatch();
			HttpUriRequest request = new HttpGet("http://localhost:" + "" + "9092" + "/eo/address");
			HttpResponse httpResponse;
			watch.start();
			httpResponse = HttpClientBuilder.create().build().execute(request);
			watch.stop();
			response = EntityUtils.toString(httpResponse.getEntity());
			System.out.println(">>>>>>>Response:(Time)" + response + "("+System.currentTimeMillis()+")"+  "Response time(hh:mm:SS:mS): " + watch.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (response.contains("Address Validated"))
			return "Order Validated!!";
		else
			throw new InternalServerErrorException();
	}

	public String autoValidate() {
		System.out.println("Auto validated at : "+System.currentTimeMillis());
		return "Order Validated Automatically with defaults";
	}
}