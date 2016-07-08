package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.formatter;

import io.vertx.core.json.JsonObject;

import java.util.Map;

import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;

/**
 * Created by ashish on 20/1/16.
 */
public interface JsonFormatter {

    <T extends Model> String toJson(T model);

    <T extends Model> String toJson(LazyList<T> modelList);

    JsonObject mapToJson(Map<String, Object> model);
}
