package com.example.spring_course.repository.mapper;

import com.example.spring_course.repository.model.DemoEntity;
import com.example.spring_course.service.dto.DemoRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class MapperDemoEntity {

    public abstract DemoEntity toEntity(DemoRequestDto requestDto);

}
