package be.ap.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.entity.TestBook;
import be.ap.backend.service.TestBookService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("test-book")
@RequiredArgsConstructor
public class TestBookController {

    private final TestBookService testBookService;

    @GetMapping
    public Page<TestBook> getBooks(Pageable pageable) {
        return testBookService.getBooks(pageable);
    }
}