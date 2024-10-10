package io.leanddd.component.data;

import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConvertBase<VO, DO> {

    @BeanMapping(builder = @Builder)
    DO voToDo(VO source);

    DO voToDo(VO source, @MappingTarget DO target);

    VO doToVo(DO source);

    List<VO> doToVo(List<DO> source);
}
