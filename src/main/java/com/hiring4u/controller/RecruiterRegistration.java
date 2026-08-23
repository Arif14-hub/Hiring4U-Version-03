package com.hiring4u.controller;
import com.hiring4u.dto.RecruiterDTO;
import com.hiring4u.service.RecRegSer;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rec")
public class RecruiterRegistration {

    private final RecRegSer recruitorservice;

    public RecruiterRegistration(RecRegSer recruitorservice) {
        this.recruitorservice = recruitorservice;
    }

    @PostMapping("/registered")
    public ResponseEntity<String> RecruiterReg(@Valid @RequestBody RecruiterDTO dto){
        return recruitorservice.processRegistration(dto);
    }
}
