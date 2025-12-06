package com.example.logtracing.controller;

import com.example.logtracing.entity.Span;
import com.example.logtracing.repository.SpanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/spans")
public class SpanController {
    @Autowired
    private SpanRepository spanRepository;

    @GetMapping
    public List<Span> getAllSpans() {
        return spanRepository.findAll();
    }

    @PostMapping
    public Span createSpan(@RequestBody Span span) {
        return spanRepository.save(span);
    }

    @GetMapping("/method/{methodName}")
    public List<Span> getSpansByMethodName(@PathVariable String methodName) {
        return spanRepository.findByMethodName(methodName);
    }
}
