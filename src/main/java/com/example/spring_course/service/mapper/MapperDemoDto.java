package com.example.spring_course.service.mapper;

import com.example.spring_course.controller.model.DemoRequest;
import com.example.spring_course.controller.model.DemoResponse;
import com.example.spring_course.service.dto.DemoRequestDto;
import com.example.spring_course.service.dto.DemoResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class MapperDemoDto {

    @Mapping(target = "location", expression = "java(mapLocation(request.getCity(), request.getNation()))")
    public abstract DemoRequestDto toRequestDto(DemoRequest request);

    public abstract DemoResponse toResponse(DemoResponseDto responseDto);

    public String mapLocation(String city, String nation){
        return nation + " - " + city;
    }
}
