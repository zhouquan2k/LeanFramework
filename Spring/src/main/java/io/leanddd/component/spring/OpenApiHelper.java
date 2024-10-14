package io.leanddd.component.spring;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.GroupedOpenApi;

import java.util.*;

public class OpenApiHelper {

    public static GroupedOpenApi getGroupedOpenApi(String name) {
        return GroupedOpenApi.builder()
                .group(name)
                .addOpenApiCustomiser(openApi -> {

                    Set<String> usedSchemas = new HashSet<>();
                    Set<String> usedParameters = new HashSet<>();
                    Set<String> usedResponses = new HashSet<>();

                    Iterator<Map.Entry<String, PathItem>> it = openApi.getPaths().entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, PathItem> entry = it.next();
                        PathItem pathItem = entry.getValue();
                        var count = pathItem.readOperations().stream().filter(operation -> operation.getTags().contains(name)
                                || operation.getTags().stream().anyMatch(tag -> tag.startsWith(name + "."))).count();
                        if (count == 0) {
                            it.remove();
                        }
                    }
                    openApi.getPaths().values().forEach(pathItem -> {
                        pathItem.readOperations().forEach(operation -> {
                            if (operation.getTags().contains(name) || operation.getTags().stream().anyMatch(tag -> tag.startsWith(name + "."))) {
                                collectUsedComponents(operation, usedSchemas, usedParameters, usedResponses);
                            }
                        });
                    });
                    filterComponents(openApi, usedSchemas, usedParameters, usedResponses);
                })
                .build();
    }

    private static void collectUsedComponents(Operation operation, Set<String> usedSchemas, Set<String> usedParameters, Set<String> usedResponses) {
        // Collect schemas from request body and responses
        if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
            operation.getRequestBody().getContent().values().forEach(content -> {
                if (content.getSchema() != null) {
                    extractSchemaRefs(content.getSchema(), usedSchemas);
                }
            });
        }

        // Collect responses schemas
        ApiResponses responses = operation.getResponses();
        responses.forEach((responseCode, response) -> {
            if (response.getContent() != null) {
                response.getContent().values().forEach(content -> {
                    if (content.getSchema() != null) {
                        extractSchemaRefs(content.getSchema(), usedSchemas);
                    }
                });
            }
            usedResponses.add(responseCode);
        });

        // Collect parameters
        if (operation.getParameters() != null) {
            operation.getParameters().forEach(parameter -> {
                usedParameters.add(parameter.getName());
                if (parameter.getSchema() != null) {
                    extractSchemaRefs(parameter.getSchema(), usedSchemas);
                }
            });
        }
    }

    private static void extractSchemaRefs(Schema<?> schema, Set<String> usedSchemas) {
        if (Objects.equals(schema.getType(), "array")) {
            schema = schema.getItems();
        }
        if (schema.get$ref() != null) {
            var ref = schema.get$ref();
            usedSchemas.add(ref.substring(ref.lastIndexOf('/') + 1));
        }
    }

    private static void filterComponents(OpenAPI openApi, Set<String> usedSchemas, Set<String> usedParameters, Set<String> usedResponses) {
        Components components = new Components();

        // Filter schemas
        if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
            openApi.getComponents().getSchemas().forEach((key, schema) -> {
                if (usedSchemas.contains(key)) {
                    components.addSchemas(key, schema);
                }
            });
        }

        // Filter parameters
        if (openApi.getComponents().getParameters() != null) {
            openApi.getComponents().getParameters().forEach((key, parameter) -> {
                if (usedParameters.contains(key)) {
                    components.addParameters(key, parameter);
                }
            });
        }

        // Filter responses
        if (openApi.getComponents().getResponses() != null) {
            openApi.getComponents().getResponses().forEach((key, response) -> {
                if (usedResponses.contains(key)) {
                    components.addResponses(key, response);
                }
            });
        }
        openApi.setComponents(components);
    }
}