package com.badmintonshop.controller.stringing;

import com.badmintonshop.dto.StringDTO;
import com.badmintonshop.entity.StringProduct;
import com.badmintonshop.service.stringing.StringProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/api/strings")
public class AdminStringProductController {

    @Autowired
    private StringProductService service;

    @PostMapping
    public StringDTO create(@RequestBody StringDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public StringDTO update(
            @PathVariable Long id,
            @RequestBody StringDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
