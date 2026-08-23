package com.hiring4u.controller;

import com.hiring4u.dto.RecruiterDTO;
import com.hiring4u.service.RecRegSer;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecruiterRegistrationTest {

    @Test
    void delegatesRegistrationToService() {
        RecRegSer service = mock(RecRegSer.class);
        RecruiterRegistration controller = new RecruiterRegistration(service);
        RecruiterDTO dto = new RecruiterDTO();

        when(service.processRegistration(dto)).thenReturn(ResponseEntity.ok("registered"));

        ResponseEntity<String> response = controller.RecruiterReg(dto);

        assertThat(response.getBody()).isEqualTo("registered");
        verify(service).processRegistration(dto);
    }
}
