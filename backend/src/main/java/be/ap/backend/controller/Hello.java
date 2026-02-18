package be.ap.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.dto.HelloDto;

@RestController
@RequestMapping("hello")
public class Hello {

    @GetMapping
    public HelloDto[] getHello() {
        return new HelloDto[] { new HelloDto("Backend en database werkt") };
    }
}