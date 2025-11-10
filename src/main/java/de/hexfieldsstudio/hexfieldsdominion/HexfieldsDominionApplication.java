package de.hexfieldsstudio.hexfieldsdominion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration.class
})
public class HexfieldsDominionApplication {

	public static void main(String[] args) {
        SpringApplication.run(HexfieldsDominionApplication.class, args);
    }

}
