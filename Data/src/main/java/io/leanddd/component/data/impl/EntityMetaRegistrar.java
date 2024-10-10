package io.leanddd.component.data.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import io.leanddd.component.common.Util;
import io.leanddd.component.framework.Context;
import io.leanddd.component.meta.*;
import io.leanddd.component.meta.Meta.BooleanEx;
import io.leanddd.component.meta.Meta.Type;
import io.leanddd.component.meta.Metadata.*;
import javassist.*;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.leanddd.component.data.impl.MetaDefinitions.*;

class ClassInfo {
    ClassFile ccFile;
    ConstPool constpool;
    CtClass cc;

    ClassInfo(CtClass cc) {
        if (cc.isFrozen())
            cc.defrost();
        this.cc = cc;
        ccFile = cc.getClassFile();
        constpool = ccFile.getConstPool();
    }
}

@Slf4j
public class EntityMetaRegistrar {

    static final String rootPakcge = "io.leanddd";
    static final String swaggerApi = "io.swagger.v3.oas.annotations.tags.Tag";
    static final String swaggerOperation = "io.swagger.v3.oas.annotations.Operation";
    static final String swaggerModel = "io.swagger.v3.oas.annotations.media.Schema";
    static final String DBTypeEnum = "Enum:com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant";

    static final Metadata metadata = new Metadata();
    static {
        metadata.getDictionaries().put("Boolean", List.of(new DictionaryItemDef(true, "", EnumTag.Success.getDesc()),
                new DictionaryItemDef(false, "", EnumTag.Warning.getDesc())));
    }

    private static final Pattern CAMELCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final char UNDERLINE = '_';

    ClassPool pool = ClassPool.getDefault();
    Set<String> processed = new HashSet<String>();

    public static Metadata getMetadata() {
        metadata.setServices(metadata.getServices().stream().sorted(Comparator.comparing(ServiceDef::getOrder))
                .collect(Collectors.toList()));
        return metadata;
    }

    private static String camelCase2UnderlineCase(String camelCase) {
        Matcher matcher = CAMELCASE_PATTERN.matcher(camelCase);
        StringBuilder builder = new StringBuilder(camelCase);
        for (int i = 0; matcher.find(); i++) {
            builder.replace(matcher.start() + i, matcher.end() + i, UNDERLINE + matcher.group().toLowerCase());
        }
        if (builder.charAt(0) == UNDERLINE) {
            builder.deleteCharAt(0);
        }
        return builder.toString();
    }

    public void initClasses(String basePackage) {

        var timezone = System.getenv("TZ");
        Util.check(Util.isNotEmpty(timezone), "timezone env TZ is not set");
        log.info("initializing timezone: {}", timezone);
        Util.initDateFormat(timezone);
        Context.setTimezone(timezone);

        try {
            final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
                    false);
            // add include filters which matches all the classes (or use your own)
            provider.addIncludeFilter(new AnnotationTypeFilter(MetaEntity.class));
            provider.addIncludeFilter(new AnnotationTypeFilter(Service.class));

            // get matching classes defined in the package
            final Set<BeanDefinition> classes = provider.findCandidateComponents(basePackage);

            // this is how you can load the class type from BeanDefinition instance
            for (BeanDefinition bean : classes) {
                processClass(bean, bean.getBeanClassName());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void processClass(Object bean, String clsName) throws Exception {
        // check if processed
        if (this.processed.contains(clsName))
            return;

        CtClass cc = pool.getCtClass(clsName);

        // check super classes
        CtClass parent = cc.getSuperclass();
        if (parent != null && !this.processed.contains(parent.getName()))
            processClass(bean, parent.getName());

        if (processClassAnnotations(cc)) {
            this.processed.add(cc.getName());
            return;
        }

        this.processed.add(cc.getName());
        if (!cc.hasAnnotation(MetaEntity.class))
            return;

        // process annotation on class
        ClassInfo ci = new ClassInfo(cc);
        // ci.newAttr(cc.get);
        AnnotationsAttribute attr = (AnnotationsAttribute) cc.getClassFile()
                .getAttribute(AnnotationsAttribute.visibleTag);
        MetaEntity metaEntity = (MetaEntity) cc.getAnnotation(MetaEntity.class);
        EntityDef entityDef = null;
        // base class won't process annotation
        // if (!metaEntity.isBase()) {
        // annotation on entity
        Map<String, Object> clsMetaData = classMetaToMap(cc, metaEntity);
        log.debug("# ...MetaEntity.{}", cc.getSimpleName());
        var metaMap = MetaDefinitions.metaMap;
        processAnnotation(ci, attr, (AnnotationDesc[]) metaMap.get("_class"), clsMetaData);
        entityDef = processMetadata(cc);
        log.debug("... <class> = {}", attr);
        // }

        // Entity fields annotation
        for (CtField field : cc.getDeclaredFields()) {
            Meta meta = (Meta) field.getAnnotation(Meta.class);
            if (meta == null)
                continue;
            Map<String, Object> metaData = metaToMap(cc, field, meta, metaEntity);
            if (entityDef != null && Util.isNotEmpty(entityDef.getIdField())) {
                var idCol = camelCase2UnderlineCase(entityDef.getIdField());
                metaData.put("idCol", idCol);
                metaData.put("refId", metaData.getOrDefault("refData", idCol));
            }
            String metaType = (String) metaData.get("type"); // meta.value().toString();
            AnnotationsAttribute fAttr = (AnnotationsAttribute) field.getFieldInfo()
                    .getAttribute(AnnotationsAttribute.visibleTag);
            if (Util.isNotEmpty(metaType))
                processAnnotation(ci, fAttr, (AnnotationDesc[]) metaMap.get(metaType), metaData);
            processAnnotation(ci, fAttr, (AnnotationDesc[]) metaMap.get("_common"), metaData);

            // field.getFieldInfo().addAttribute(fAttr);
            // log.debug("...metadata: {}", metaData);
            log.debug("...{} = {}", field.getName(), fAttr);
        }

        try {
            cc.toClass();
        } catch (CannotCompileException e) {
            e.printStackTrace();
            log.warn("can't compile:" + cc.getName() + " : " + e.getMessage());
        }

        this.processed.add(clsName);
    }

    private EntityDef processMetadata(CtClass cc) throws Exception {
        MetaEntity metaEntity = (MetaEntity) cc.getAnnotation(MetaEntity.class);
        if (metaEntity == null)
            return null;
        List<FieldDef> fields = processMetadataFields(cc, metaEntity);
        String idField = null;
        for (var field : fields) {
            if (field.getType() == Type.ID || field.getType() == Type.IDStr) {
                idField = field.getName();
                break;
            }
        }

        EntityDef entityDef = new EntityDef(metaEntity.name().length() > 0 ? metaEntity.name() : cc.getSimpleName(),
                metaEntity.label(), cc.getName(), metaEntity.tableName(), idField, fields);
        metadata.getEntities().add(entityDef);
        return entityDef;
    }

    private List<FieldDef> processMetadataFields(CtClass cc, MetaEntity metaEntity) throws Exception {
        List<FieldDef> fields = new Vector<FieldDef>();

        for (CtField field : cc.getDeclaredFields()) {
            Meta meta = (Meta) field.getAnnotation(Meta.class);
            if (meta == null) //field.getAnnotation(JsonIgnore.class) != null
                continue;

            var fieldMeta = metaToMap(cc, field, meta, metaEntity);
            var fieldDef = this._processFieldMeta(fieldMeta);
            fields.add(fieldDef);
        }

        CtClass parent = cc.getSuperclass();
        if (parent.hasAnnotation(MetaEntity.class)) {
            fields.addAll(processMetadataFields(parent, (MetaEntity) parent.getAnnotation(MetaEntity.class)));
        }
        return fields;
    }

    private FieldDef _processFieldMeta(Map<String, Object> fieldMeta) throws Exception {
        String name = (String) fieldMeta.get("name");
        String simpleName = (String) fieldMeta.get("simpleType");
        String className = (String) fieldMeta.get("fullType");
        Type type = Type.valueOf((String) fieldMeta.getOrDefault("type", "Default"));
        if (type == Type.Enum && !simpleName.equals("Boolean")) {
            if (!metadata.getDictionaries().containsKey(simpleName)) {
                List<DictionaryItemDef> dict = new Vector<DictionaryItemDef>();
                Class<?> enumClass = Class.forName(className);
                Method valuesMethod = enumClass.getDeclaredMethod("values");
                Object[] enumValues = (Object[]) valuesMethod.invoke(null);
                if (EnumDescription.class.isAssignableFrom(enumClass)) {
                    for (Object enumValue : enumValues) {
                        var ed = (EnumDescription) enumValue;
                        dict.add(new DictionaryItemDef(enumValue, ed.getDesc(),
                                (ed.getTag() != null) ? ed.getTag().getDesc() : null));
                    }
                    metadata.getDictionaries().put(simpleName, dict);
                }
            }
        }
        var dbColumnName = camelCase2UnderlineCase(name);
        var refData = (String) fieldMeta.get("refData");

        var fieldDef = new FieldDef(name, dbColumnName, (String) fieldMeta.get("label"), type,
                type == Type.Dictionary ? refData : simpleName, className, refData,
                (Boolean) fieldMeta.get("isNull"), (Integer) fieldMeta.getOrDefault("length", -1),
                (Boolean) fieldMeta.getOrDefault("hidden", false),
                (Boolean) fieldMeta.getOrDefault("immutable", false),
                (Boolean) fieldMeta.getOrDefault("persistable", true),
                (Boolean) fieldMeta.get("editable"),
                (Boolean) fieldMeta.get("searchable"),
                (Boolean) fieldMeta.get("listable"), (String) fieldMeta.get("defaultValue"),
                (String) fieldMeta.get("uiType"),
                null, (Integer) fieldMeta.get("colWidth"), 0);
        fieldDef.init();
        return fieldDef;
    }

    private String getGenericElementClassName(String genericSignature) {
        int start = genericSignature.indexOf('<') + 1;
        int end = genericSignature.lastIndexOf('>');

        if (start > 0 && end > start) {
            String elementTypeSignature = genericSignature.substring(start, end);
            if (elementTypeSignature.startsWith("L") && elementTypeSignature.endsWith(";")) {
                String className = elementTypeSignature.substring(1, elementTypeSignature.length() - 1);
                return className.replace('/', '.');
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private boolean processClassAnnotations(CtClass cc) throws Exception {
        var processed = false;
        Service service = (Service) cc.getAnnotation(Service.class);
        if (service == null)
            return false;
        for (var entry : MetaDefinitions.annotationMaps.entrySet()) {
            var annoName = entry.getKey();
            if (cc.hasAnnotation(annoName)) {
                List<AnnotationMap> list = entry.getValue();

                ClassInfo ci = new ClassInfo(cc);

                for (AnnotationMap annoMap : list) {
                    if (Objects.equals(annoMap.myAnnotaion, "_class")) {
                        if (service.type() != Service.Type.Base)
                            processClassAnnotation(ci, annoName, annoMap);
                    } else
                        processMethodAnnotation(ci, annoName, annoMap);
                }
                processed = true;
            }
        }
        if (processed) {
            try {
                cc.toClass();
            } catch (CannotCompileException e) {
                log.warn("can't compile:" + cc.getName() + " : " + e.getMessage());
            }
        }
        // service to metadata

        // if (service.type() == Service.Type.Command || service.type() == Service.Type.Mixed) {
        {
            var permissionCls = service.permissions();
            List<PermissionDef> permissions = null;
            if (permissionCls.isEnum()) {
                permissions = Util.mapToList(Arrays.asList(permissionCls.getEnumConstants()).stream(),
                        permission -> new PermissionDef(((Enum) permission).name()));
                // ((EnumDescription) permission).getDesc()));
            } else {
                var field = permissionCls.getDeclaredField("permissionDefList");
                field.setAccessible(true);
                permissions = (List<PermissionDef>) field.get(null);
            }
            // if (Util.isNotEmpty(service.value())) {
            {
                // to get all the methods with Query/Command annotation
                var methods = cc.getMethods();
                var methodDefs = Arrays.stream(methods).map(method -> {
                    return new MethodDef(method.getName(), method.getName(), null);
                }).collect(Collectors.toList());

                ServiceDef function = new ServiceDef(Util.isNotEmpty(service.name()) ? service.name() : cc.getSimpleName(), cc.getName(), service.value() ,
                        Util.isEmpty(service.permissionDomain()) ? service.name() : service.permissionDomain(), permissions, service.order()
                        , methodDefs);
                metadata.getServices().add(function);
            }
        }
        return processed;
    }

    private void processClassAnnotation(ClassInfo ci, String annoName, AnnotationMap annoMap) throws Exception {
        AnnotationsAttribute attr = (AnnotationsAttribute) ci.ccFile.getAttribute(AnnotationsAttribute.visibleTag);
        Class<?> annoType = Class.forName(annoName);
        var anno = ci.cc.getAnnotation(annoType);
        Map<String, Object> clsMetaData = annotationToMap(annoType, anno);
        log.debug("##### ...Class.{}", ci.cc.getSimpleName());
        processAnnotation(ci, attr, annoMap.destAnnotations, clsMetaData);
        log.debug("...{} <class> = {}", ci.cc.getSimpleName(), attr);

    }

    private void processMethodAnnotation(ClassInfo ci, String annoName, AnnotationMap annoMap) throws Exception {
        for (CtMethod method : ci.cc.getDeclaredMethods()) {
            if (Objects.equals(method.getDeclaringClass().getName(), "java.lang.Object"))
                continue;
            if (!AccessFlag.isPublic(method.getModifiers()))
                continue;
            Map<String, Object> data = null;
            if (Objects.equals("_class_method", annoMap.myAnnotaion)) {
                Class<?> annoType = Class.forName(annoName);
                var anno = ci.cc.getAnnotation(annoType);
                data = annotationToMap(annoType, anno);
            } else {
                var annoType = Class.forName(annoMap.myAnnotaion);
                var anno = method.getAnnotation(annoType);
                if (anno == null)
                    continue;
                data = annotationToMap(annoType, anno);
            }
            var methodInfo = method.getMethodInfo();
            AnnotationsAttribute mAttr = (AnnotationsAttribute) methodInfo
                    .getAttribute(AnnotationsAttribute.visibleTag);
            if (mAttr == null) {
                mAttr = new AnnotationsAttribute(ci.constpool, AnnotationsAttribute.visibleTag);
                methodInfo.addAttribute(mAttr);
            }
            // log.trace("...{}.{}: {}", ci.cc.getSimpleName(), method.getName(), data);

            boolean processed = processAnnotation(ci, mAttr, annoMap.destAnnotations, data);
            if (processed)
                log.debug("...{}.{} = {}", ci.cc.getSimpleName(), method.getName(), mAttr);
        }
    }

    Map<String, Object> classMetaToMap(CtClass cls, MetaEntity meta) {
        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("name", cls.getSimpleName());

        ret.put("label", Util.isEmpty(meta.label()) ? cls.getSimpleName() : meta.label());
        ret.put("tableName", meta.tableName());
        return ret;
    }

    Map<String, Object> annotationToMap(Class<?> annoType, Object anno) {
        return Arrays.asList(annoType.getDeclaredMethods()).stream().collect(Collectors.toMap(m -> m.getName(), m -> {
            try {
                return m.invoke(anno);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        }));
    }

    Map<String, Object> metaToMap(CtClass cls, CtField field, Meta meta, MetaEntity metaEntity) throws Exception {
        Map<String, Object> ret = new HashMap<String, Object>();
        // 1. default values
        ret.put("name", field.getName());
        ret.put("label", field.getName());

        ret.put("listable", true);
        ret.put("editable", metaEntity.defaultUpdatable());
        ret.put("searchable", false);
        ret.put("isNull", true);

        // 2. from category
        var category = meta.category().name();
        if (meta.category() != Meta.Category.None) {
            var categoryMap = Categories.get(category);
            if (categoryMap != null)
                ret.putAll(categoryMap);
        }

        // 3. type
        if (meta.value() != Type.Default)
            ret.put("type", meta.value().toString());
        var type = Type.valueOf((String) ret.getOrDefault("type", "Default"));

        // 4. from type defaults
        if (meta.category() == Meta.Category.None) { // only take effect when not using Category
            var typeDefaults = AllTypeDefaults.get(type.name());
            if (typeDefaults != null)
                ret.putAll(typeDefaults);
        }

        // 5. from other meta properties
        if (meta.length() > 0)
            ret.put("length", meta.length());
        if (meta.nullable() != BooleanEx.Default)
            ret.put("isNull", meta.nullable() == BooleanEx.True);
        /* TODO
        if (!Util.isEmpty(meta.label()))
            ret.put("label", meta.label());
         */
        ret.put("unique", meta.unique());
        ret.put("refData", meta.refData());
        if (meta.hidden() != BooleanEx.Default)
            ret.put("hidden", meta.hidden() == BooleanEx.True);
        if (meta.editable() != BooleanEx.Default)
            ret.put("editable", meta.editable() == BooleanEx.True);
        if (meta.listable() != BooleanEx.Default)
            ret.put("listable", meta.listable() == BooleanEx.True);
        if (meta.searchable() != BooleanEx.Default)
            ret.put("searchable", meta.searchable() == BooleanEx.True);
        ret.put("immutable", meta.immutable());
        if (meta.colWidth() >= 0)
            ret.put("colWidth", meta.colWidth());

        // 4. concludes
        ret.put("simpleType", field.getType().getSimpleName());
        ret.put("fullType", field.getType().getName());
        if (type == Type.ToMany) {
            String signature = field.getGenericSignature();
            ret.put("fullType", getGenericElementClassName(signature));
        }
        ret.put("entityName", cls.getSimpleName());
        if (meta.persistable() == BooleanEx.False) {
            ret.put("persistable", false);
            ret.put("editable", false);
        }
        return ret;
    }

    boolean processAnnotation(ClassInfo ci, AnnotationsAttribute attr, AnnotationDesc[] annotationDescs,
                              Map<String, Object> metaData) throws Exception {

        boolean processed = false;

        List<AnnotationDesc> annotationDescList = new ArrayList<>(Arrays.asList(annotationDescs));

        // not persistant
        if (metaData.containsKey("persistable")) {
            var persistable = (Boolean) metaData.get("persistable");
            if (!persistable) {
                // not a database field
                annotationDescList = annotationDescList.stream().filter(desc -> !List.of("com.gitee.sunchenbin.mybatis.actable.annotation.Column").contains(desc.getClassName()))
                        .collect(Collectors.toList());
                annotationDescList.addAll(Arrays.asList(NoPersistent));
            }
        }

        // for each annotation
        for (AnnotationDesc ad : annotationDescList) {
            // if this annotation already exists,continue;
            if (ad.enable != null) {
                if (!ad.enable.test(metaData))
                    continue;
            }
            if (attr.getAnnotation(ad.className) != null)
                continue;
            processed = true;

            String annotationName = ad.className;
            Annotation annot = new Annotation(annotationName, ci.constpool);

            // 取值规则描述 propName="常量" propName="$来自Meta属性:默认值"，当无默认值表示不生成此属性 支持json
            Map<String, Object> props = new HashMap<String, Object>();
            // for each annotation's property
            for (AnnotationProperty ap : ad.properties) {
                Object value = null;
                if (ap.metaProp != null && metaData.containsKey(ap.metaProp))
                    value = metaData.get(ap.metaProp);
                else {

                    if (ap.value instanceof String) {
                        String str = (String) ap.value;
                        if (str.indexOf("${") >= 0) { // reference to a meta property
                            value = JSON.parse(Util.format(str, metaData));
                        } else
                            value = ap.value;

                    } else
                        value = ap.value;

                    // value=ap.value;
                }
                if (value != null)
                    props.put(ap.name, value);

                MemberValue mv = null;
                if (ap.metaProp != null && ap.metaProp.startsWith("Enum:")) {
                    EnumMemberValue mve = new EnumMemberValue(ci.constpool);
                    mve.setType(ap.metaProp.substring(5));
                    mve.setValue((String) value);
                    mv = mve;
                } else if (ap.metaProp != null && ap.metaProp.startsWith("Class:")) {
                    mv = new ClassMemberValue(ap.metaProp.substring(6), ci.constpool);
                } else if (value instanceof String)
                    mv = new StringMemberValue((String) value, ci.constpool);
                else if (value instanceof String[]) {
                    var amv = new ArrayMemberValue(ci.constpool);
                    MemberValue[] array = Arrays.stream((String[]) value)
                            .map(i -> new StringMemberValue(i, ci.constpool)).toArray(MemberValue[]::new);
                    amv.setValue(array);
                    mv = amv;
                } else if (value instanceof Integer)
                    mv = new IntegerMemberValue(ci.constpool, ((Integer) value).intValue());
                else if (value instanceof Boolean)
                    mv = new BooleanMemberValue((boolean) value, ci.constpool);
                else if (value instanceof JSONArray) {
                    var jsonArray = (JSONArray) value;
                    var amv = new ArrayMemberValue(ci.constpool);
                    MemberValue[] array = jsonArray.toJavaList(String.class).stream()
                            .map(i -> new StringMemberValue(i, ci.constpool)).toArray(MemberValue[]::new);
                    amv.setValue(array);
                    mv = amv;
                } else
                    Util.check(false, "%s.%s invalid type %s", annotationName, ap.name,
                            value != null ? value.getClass().getName() : "<null>");
                annot.addMemberValue(ap.name, mv);
            }
            attr.addAnnotation(annot);
        }
        return processed;

    }

    public void serviceAnnotationProcess(Object o) {
    }
}

