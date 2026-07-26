package com.schemadrift.detector.controller;

import com.schemadrift.detector.model.Source;
import com.schemadrift.detector.repository.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sources")
public class SourceController {

    private final SourceRepository sourceRepository;

    @Autowired
    public SourceController(SourceRepository sourceRepository) {
        this.sourceRepository = sourceRepository;
    }

    @GetMapping
    public List<Source> listSources(Authentication auth) {
        Long userId = currentUserId(auth);
        return sourceRepository.findByOwnerId(userId);
    }

    @PostMapping
    public Source createSource(@RequestBody Source source, Authentication auth) {
        source.setOwnerId(currentUserId(auth));
        return sourceRepository.save(source);
    }

    // Placeholder until the JWT filter attaches a real principal with user id.
    // Swap this out once SecurityConfig + JwtAuthFilter are wired up.
    private Long currentUserId(Authentication auth) {
        return Long.valueOf(auth.getName());
    }
}
