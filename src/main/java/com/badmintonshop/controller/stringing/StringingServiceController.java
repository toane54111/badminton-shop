package com.badmintonshop.controller.stringing;

import com.badmintonshop.dto.StringServiceDTO;
import com.badmintonshop.entity.StringingService;
import com.badmintonshop.service.stringing.StringingServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stringing-services")
public class StringingServiceController {

    @Autowired
    private StringingServiceService service;

    @GetMapping
    public List<StringServiceDTO> getAll() {
        return service.getAllActive();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StringServiceDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }
}