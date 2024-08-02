package com.dcs.api.etax.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	    //  To enable Cross-Origin Resource Sharing (CORS) on the server-side web application, 
		//	allowing the server to accept requests from the specified origin (Client-side/Front-end).
	    @Value("${cors.allowed.origins}")
	    private String allowedOrigins;
	
	 	@Override
	    public void addCorsMappings(CorsRegistry registry) {
	        registry.addMapping("/**")
	                .allowedOrigins(allowedOrigins)
	                .allowedMethods("*")
	                .allowedHeaders("*"); 
	    }
}
