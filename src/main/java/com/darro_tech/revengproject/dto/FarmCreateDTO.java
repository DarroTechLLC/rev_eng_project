package com.darro_tech.revengproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FarmCreateDTO {

    private String name;
    private String displayName;
    private String farmType;
    private String tempSourceId;
    private Boolean isTempSource;
}
