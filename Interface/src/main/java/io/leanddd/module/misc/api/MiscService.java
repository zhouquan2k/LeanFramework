package io.leanddd.module.misc.api;

import io.leanddd.component.meta.Meta;
import io.leanddd.component.meta.MetaEntity;
import io.leanddd.component.meta.Metadata.EntityDef;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RequestMapping("/api/misc")
public interface MiscService {

    @GetMapping("version")
    String getVersion() throws Exception;

    @GetMapping("metadata/entity/{entityName}")
    EntityDef getEntityMetadata(@PathVariable String entityName);

    @PostMapping("date-test")
    Object testDate(@RequestBody TestDate date);

    @PostMapping("exception-test")
    void testException();

    @MetaEntity
    public static class TestDate {
        @Meta(Meta.Type.Date)
        public Date inputDate;

        public Date inputDate2;
    }
}
