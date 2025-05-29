package com.darro_tech.revengproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FarmCreateDTO {

    private String name;
    private String displayName;
    private String farmType;
    private String tempSourceId;
    private Boolean isTempSource;
}
