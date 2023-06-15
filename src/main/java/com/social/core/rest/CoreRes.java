package com.social.core.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@CrossOrigin(origins = "*")
public class CoreRes {

    @RequestMapping(value = "/${ms.basepath}/healthy",method = RequestMethod.GET)
    ResponseEntity<String> checkHealthy(){
        return ResponseEntity.ok("Server is healthy");
    }
}
