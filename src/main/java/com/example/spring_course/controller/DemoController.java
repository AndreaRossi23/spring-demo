package com.example.spring_course.controller;

import com.example.spring_course.controller.model.DemoRequest;
import com.example.spring_course.controller.model.DemoResponse;
import com.example.spring_course.service.DemoService;
import com.example.spring_course.service.dto.DemoRequestDto;
import com.example.spring_course.service.dto.DemoResponseDto;
import com.example.spring_course.service.mapper.MapperDemoDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class DemoController {

    private final DemoService demoService;
    private final MapperDemoDto mapperDemoDto;

    @GetMapping("/hello")
    public String hello() {
        return "Hello World!";
    }

    @PostMapping(
            value = "/demo",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DemoResponse> demo(
            @Valid @RequestBody DemoRequest request) {

        DemoRequestDto requestDto = mapperDemoDto.toRequestDto(request);

        DemoResponseDto responseDto = demoService.callDemoService(requestDto);

        DemoResponse response = mapperDemoDto.toResponse(responseDto);

        return ResponseEntity.ok().body(response);
    }

}
