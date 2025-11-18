package de.hexfieldsstudio.hexfieldsdominion;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @RequestMapping(path = "/", method = {RequestMethod.GET, RequestMethod.HEAD})
    public String root() {
        return "OK";
    }

}
