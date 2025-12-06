package com.example.logtracing.repository;

import com.example.logtracing.entity.Trace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TraceRepository extends JpaRepository<Trace, Long> {
    Trace findByTraceId(String traceId);
}
