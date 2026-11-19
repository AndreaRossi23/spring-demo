package com.example.spring_course.service;

import com.example.spring_course.repository.DemoRepository;
import com.example.spring_course.repository.mapper.MapperDemoEntity;
import com.example.spring_course.repository.model.DemoEntity;
import com.example.spring_course.service.dto.DemoRequestDto;
import com.example.spring_course.service.dto.DemoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DemoService {

    private final DemoRepository demoRepository;
    private final MapperDemoEntity mapperDemoEntity;

    public DemoResponseDto callDemoService(DemoRequestDto requestDto){

        validazioneSintattica(requestDto.getIuv());
        validazioneSemantica(requestDto.getIuv());

        DemoEntity entity = mapperDemoEntity.toEntity(requestDto);

        demoRepository.save(entity);

//        Implementazione logica del servizio
//        Se tutto passa senza errori setto la risposta dto da tornare al controller
        DemoResponseDto responseDto = new DemoResponseDto();
        responseDto.setOutcome("OK");
        responseDto.setStatus("ELABORATO");
        return responseDto;
    }

    private void validazioneSemantica(String iuv) {
        //Implementazione validazione semantica e logica (a db)
    }

    private void validazioneSintattica(String iuv) {
        //Implementazione validazione sitattica e logica di validazione della request
    }
}
