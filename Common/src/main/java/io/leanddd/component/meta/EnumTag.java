package io.leanddd.component.meta;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EnumTag {
    Success("success"), Info(""), Warning("warning"), Danger("danger"), Gray("info"), Invisible("invisible");

    String desc;

}
