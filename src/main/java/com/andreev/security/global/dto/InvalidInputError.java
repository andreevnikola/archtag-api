package com.andreev.security.global.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class InvalidInputError {
    public Map<String, String> invalidFields;
}
