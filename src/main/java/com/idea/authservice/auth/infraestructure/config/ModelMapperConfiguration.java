/* Copyright 2020 the original author or authors. All rights reserved. */
package com.idea.authservice.auth.infraestructure.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import com.idea.authservice.api.model.Match;
import com.idea.authservice.api.dtos.MatchDTO;
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
        modelMapper.getConfiguration().setCollectionsMergeEnabled(false);

        // Mapping Match -> MatchDTO
        modelMapper.addMappings(new PropertyMap<Match, MatchDTO>() {
            @Override
            protected void configure() {
                map().setCreatorId(source.getCreatorId());
            }
        });

        // Mapping MatchDTO -> Match
        modelMapper.addMappings(new PropertyMap<MatchDTO, Match>() {
            @Override
            protected void configure() {
                map().setCreatorId(source.getCreatorId());
            }
        });

        return modelMapper;
    }
}
