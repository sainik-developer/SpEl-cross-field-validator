package com.sf.customvalidator.example.api;

import com.sf.customvalidator.example.dto.CustomerDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Log4j2
@Validated
@RestController
@RequestMapping("/rest/classes")
@RequiredArgsConstructor
public class CustomerApiController {

    @PostMapping("/")
    @PreAuthorize("hasAuthority('ADMIN')")
    public CustomerDTO addClass(@RequestBody @Validated CustomerDTO customerDTO) {
      return customerDTO;
    }

    @PutMapping("/{class-id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public CustomerDTO modifySchools(@PathVariable("class-id") final UUID classId,
                                     @RequestBody @Validated CustomerDTO classDTO) {
       return classDTO;
    }
}