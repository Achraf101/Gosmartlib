package be.ap.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.entity.Hello;
import be.ap.backend.repository.HelloRepository;

@RestController
@RequestMapping("hello")
public class HelloController {

    private final HelloRepository helloRepository;

    public HelloController(HelloRepository helloRepository) {
        this.helloRepository = helloRepository;
    }

    @GetMapping
    public List<Hello> getHello() {
        return helloRepository.findFirstByOrderByIdAsc();
    }
}