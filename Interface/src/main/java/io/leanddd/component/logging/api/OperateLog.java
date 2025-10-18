package io.leanddd.component.logging.api;

import io.leanddd.component.meta.Meta;
import io.leanddd.component.meta.Meta.BooleanEx;
import io.leanddd.component.meta.Meta.Category;
import io.leanddd.component.meta.Meta.Type;
import io.leanddd.component.meta.MetaEntity;
import lombok.*;

import java.util.Date;

@MetaEntity(tableName = "t_operate_log")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperateLog {
    private static final long serialVersionUID = 1L;

    @Meta(value = Type.ID)
    String logId;

    @Meta(value = Type.String, searchable = BooleanEx.True)
    private String module;

    @Meta(value = Type.String, searchable = BooleanEx.True)
    private String operateName;

    @Meta(value = Type.RefIDStr)
    private String resourceId;

    @Meta(value = Type.RefIDStr)
    private String traceId;

    @Meta(value = Type.String)
    private String content;

    @Meta(value = Type.JSON)
    private String operateParams;

    @Meta(value = Type.Enum)
    private Boolean success;

    @Meta(value = Type.String, length = 1000, listable = BooleanEx.False)
    private String resultMsg;
    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg.length() > 1000 ? resultMsg.substring(0, 1000) : resultMsg;
    }

    @Meta(category = Category.PersonName)
    private String username;

    @Meta(value = Type.RefID)
    private String userId;

    @Meta(value = Type.Timestamp)
    private Date timestamp;

    @Meta(value = Type.Integer) // milliseconds
    private Integer duration;

}
