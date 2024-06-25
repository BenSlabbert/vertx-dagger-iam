/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggeriam.web.dto;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.jsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;
import java.util.Set;

@JsonWriter
public record InvalidRequestFieldsResponseDto(String field, String message) {

  public static Builder builder() {
    return new AutoBuilder_InvalidRequestFieldsResponseDto_Builder();
  }

  public static InvalidRequestFieldsResponseDto fromJson(JsonObject json) {
    return InvalidRequestFieldsResponseDto_JsonWriter.fromJson(json);
  }

  public static Set<String> missingRequiredFields(JsonObject json) {
    return InvalidRequestFieldsResponseDto_JsonWriter.missingRequiredFields(json);
  }

  public JsonObject toJson() {
    return InvalidRequestFieldsResponseDto_JsonWriter.toJson(this);
  }

  @AutoBuilder
  public interface Builder {

    Builder field(String field);

    Builder message(String message);

    InvalidRequestFieldsResponseDto build();
  }
}
