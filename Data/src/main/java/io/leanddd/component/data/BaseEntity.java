package io.leanddd.component.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.leanddd.component.common.Util;
import io.leanddd.component.meta.Meta;
import io.leanddd.component.meta.MetaEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;

import java.io.Serializable;

import static io.leanddd.component.meta.Meta.BooleanEx.True;
import static io.leanddd.component.meta.Meta.Type;

@Getter
@Setter(AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@SuperBuilder(toBuilder = true)
@MetaEntity(isBase = true)
@NoArgsConstructor
public class BaseEntity<T> implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    @Meta(value = Type.RefID, immutable = true)
    protected java.lang.String createdBy;

    @JsonIgnore
    @Meta(value = Type.Timestamp, hidden = True, immutable = true)
    protected java.util.Date createdTime;

    @JsonIgnore
    @Meta(value = Type.RefID)
    protected java.lang.String updatedBy;

    @JsonIgnore
    @Meta(value = Type.Timestamp, hidden = True)
    protected java.util.Date updatedTime;

    @JsonIgnore
    @Meta(value = Type.RefIDStr, immutable = true)
    protected String legacyId;

    @JsonIgnore
    @Version
    @Meta(value = Type.Integer, hidden = True, immutable = true)
    protected Integer version;
    @Transient
    @JsonIgnore
    protected Boolean delFlag;

    public void init() {

    }

    @SuppressWarnings("unchecked")
    @Override
    public T clone() {
        try {
            return (T) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @JsonIgnore
    public boolean isDel() {
        return delFlag != null && delFlag == true;
    }

    @JsonIgnore
    public boolean isNew() {
        return version == null;
    }

    // copy data from input param, businessless. input param may be a vo
    public void update(Object obj) {
        Util.notSupport();
    }
}

