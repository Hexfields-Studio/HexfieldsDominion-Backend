package de.hexfieldsstudio.hexfieldsdominion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration.class
})
@RestController
public class HexfieldsDominionApplication {

	public static void main(String[] args) {
		SpringApplication.run(HexfieldsDominionApplication.class, args);
	}

}
