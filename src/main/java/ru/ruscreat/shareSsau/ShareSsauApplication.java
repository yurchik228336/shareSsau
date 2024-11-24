package ru.ruscreat.shareSsau;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
@SpringBootApplication
public class ShareSsauApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShareSsauApplication.class, args);
	}

}
