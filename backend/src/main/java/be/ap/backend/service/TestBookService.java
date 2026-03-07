package be.ap.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import be.ap.backend.entity.TestBook;
import be.ap.backend.repository.TestBookRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TestBookService {

    private final TestBookRepository testBookRepository;

    public Page<TestBook> getBooks(Pageable pageable) {
        return testBookRepository.findAll(pageable);
    }
}