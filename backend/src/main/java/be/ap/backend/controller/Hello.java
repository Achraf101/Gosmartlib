package be.ap.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.controller.dto.PersonDTO;

@RestController
public class Hello {
    @GetMapping(path = "/hello")
    public PersonDTO getHello() { 
        return new PersonDTO("Jefke", "Hallo iedereen");
    }
}