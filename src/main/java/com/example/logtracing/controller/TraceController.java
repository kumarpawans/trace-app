package com.example.logtracing.controller;

import com.example.logtracing.entity.Trace;
import com.example.logtracing.repository.TraceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/traces")
public class TraceController {
    @Autowired
    private TraceRepository traceRepository;

    @GetMapping
    public List<Trace> getAllTraces() {
        return traceRepository.findAll();
    }

    @PostMapping
    public Trace createTrace(@RequestBody Trace trace) {
        return traceRepository.save(trace);
    }

    @GetMapping("/{traceId}")
    public Trace getTraceByTraceId(@PathVariable String traceId) {
        return traceRepository.findByTraceId(traceId);
    }
}
