package com.badmintonshop.controller.stringing;

import com.badmintonshop.dto.StringDTO;
import com.badmintonshop.entity.StringProduct;
import com.badmintonshop.service.stringing.StringProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/strings")
public class StringProductController {

    @Autowired
    private StringProductService service;

    @GetMapping
    public List<StringDTO> getAll() {
        return service.getAllActive();
    }

    @GetMapping("/{id}")
    public StringDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/by-brand/{brandId}")
    public List<StringDTO> getByBrand(@PathVariable Long brandId) {
        return service.getByBrand(brandId);
    }
}

