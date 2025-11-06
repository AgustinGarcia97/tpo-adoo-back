/* Copyright 2020 the original author or authors. All rights reserved. */
package com.idea.authservice.auth.infraestructure.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.modelmapper.convention.MatchingStrategies.STRICT;

@Configuration
public class ModelMapperConfiguration {

    @Bean
    public ModelMapper modelMapper() {
        final ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration().setMatchingStrategy(STRICT);
        modelMapper.getConfiguration().setSkipNullEnabled(true);

        // Mapping Match -> MatchDTO
        modelMapper.addMappings(new PropertyMap<com.idea.authservice.model.Match, com.idea.authservice.dtos.MatchDTO>() {
            @Override
            protected void configure() {
                map().setCreatorId(source.getCreatorId());
                map().setStatus(source.getStatus().name());
            }
        });

        // Mapping MatchDTO -> Match
        modelMapper.addMappings(new PropertyMap<com.idea.authservice.dtos.MatchDTO, com.idea.authservice.model.Match>() {
            @Override
            protected void configure() {
                map().setCreatorId(source.getCreatorId());
                when(ctx -> source.getStatus() != null).map().setStatus(com.idea.authservice.model.enums.MatchStatus.valueOf(source.getStatus()));
            }
        });

        return modelMapper;
    }
}
