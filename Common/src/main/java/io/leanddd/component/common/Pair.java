package io.leanddd.component.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pair {
    private String key;
    private Object value;
}