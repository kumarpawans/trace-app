package com.example.logtracing.repository;

import com.example.logtracing.entity.Span;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpanRepository extends JpaRepository<Span, Long> {
    List<Span> findByMethodName(String methodName);
}
