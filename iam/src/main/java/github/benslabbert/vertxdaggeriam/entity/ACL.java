/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggeriam.entity;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.vertxjsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.common.dsl.ObjectSchemaBuilder;
import java.util.Set;

@JsonWriter
public record ACL(String group, String role, Set<String> permissions) {

  public static Builder builder() {
    return new AutoBuilder_ACL_Builder();
  }

  public static ACL fromJson(JsonObject json) {
    return ACL_JsonWriter.fromJson(json);
  }

  public static ObjectSchemaBuilder schemaBuilder() {
    return ACL_JsonWriter.schemaBuilder();
  }

  public JsonObject toJson() {
    return ACL_JsonWriter.toJson(this);
  }

  @AutoBuilder
  public interface Builder {

    Builder group(String group);

    Builder role(String role);

    Builder permissions(Set<String> permissions);

    ACL build();
  }
}
