package io.leanddd.component.data;

import org.mapstruct.MappingTarget;

import java.util.List;

public interface ConvertBase<VO, DO> {

    DO voToDo(VO source);

    DO voToDo(VO source, @MappingTarget DO target);

    VO doToVo(DO source);

    List<VO> doToVo(List<DO> source);
}
