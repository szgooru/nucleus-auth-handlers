package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.formatter;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.ModelDelegate;
import org.javalite.common.Convert;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.impl.StringEscapeUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 20/1/16. Simple Json formatter is not aware of any
 * parent child relationship and just assumes that formatter is needed to work
 * on the entity provided. The difference with AJ provided Model.toJson() is
 * difference in handling of "jsonb" field type in postgres
 */
class SimpleJsonFormatter implements JsonFormatter {
    private static final String JSONB_TYPE = "jsonb";
    private static final int CAPACITY = 2048;
    private final String[] attributes;
    private final boolean pretty;
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleJsonFormatter.class);

    public SimpleJsonFormatter(boolean pretty, List<String> attributes) {
        this.pretty = pretty;
        this.attributes = (attributes != null && !attributes.isEmpty()) ? lowerCased(attributes) : null;
    }

    @Override
    public <T extends Model> String toJson(T model) {
        StringBuilder sb = new StringBuilder(CAPACITY);
        String indent = "";
        modelToJson(model, sb, indent);
        return sb.toString();
    }

    @Override
    public <T extends Model> String toJson(LazyList<T> modelList) {
        StringBuilder sb = new StringBuilder(CAPACITY);
        sb.append('[');
        if (pretty) {
            sb.append('\n');
        }

        for (int i = 0; i < modelList.size(); i++) {
            if (i > 0) {
                sb.append(',');
                if (pretty) {
                    sb.append('\n');
                }
            }
            T model = modelList.get(i);
            modelToJson(model, sb, (pretty ? "  " : ""));
        }
        if (pretty) {
            sb.append('\n');
        }
        sb.append(']');
        return sb.toString();

    }

    private <T extends Model> void modelToJson(T model, StringBuilder sb, String indent) {
        if (pretty) {
            sb.append(indent);
        }
        sb.append('{');
        String[] names;

        if (this.attributes == null) {
            Set<String> attributeNamesAll = ModelDelegate.attributeNames(model.getClass());
            names = lowerCased(attributeNamesAll);
        } else {
            names = this.attributes;
        }

        for (int i = 0; i < names.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            if (pretty) {
                sb.append("\n  ").append(indent);
            }
            String name = names[i];
            sb.append('"').append(name).append("\":");
            Object v = model.get(name);
            if (v == null) {
                sb.append("null");
            } else if (v instanceof Number || v instanceof Boolean) {
                sb.append(v);
            } else if (v instanceof Date) {
                sb.append('"').append(Convert.toIsoString((Date) v)).append('"');
            } else if (v instanceof PGobject && ((PGobject) v).getType().equalsIgnoreCase(JSONB_TYPE)) {
                sb.append(Convert.toString(v));
            } else if (v instanceof String) {
                sb.append('"');
                try {
                    sb.append(StringEscapeUtils.escapeJava(String.valueOf(v)));
                } catch (Exception e) {
                    LOGGER.warn("Failed to parse value of field '{}', will use default string without conversion ",
                        name);
                    sb.append(Convert.toString(v));
                }
                sb.append('"');
            } else {
                sb.append('"');
                sb.append(Convert.toString(v));
                sb.append('"');
            }
        }
        if (pretty) {
            sb.append('\n').append(indent);
        }
        sb.append('}');
    }

    private static String[] lowerCased(Collection<String> collection) {
        String[] array = new String[collection.size()];
        int i = 0;
        for (String elem : collection) {
            array[i++] = elem.toLowerCase();
        }
        return array;
    }

    @Override
    public JsonObject mapToJson(Map<String, Object> model) {
        JsonObject result = new JsonObject();
        if (model == null) {
            return result;
        }
        Iterator<String> fieldNamesIterator = model.keySet().iterator();
        while (fieldNamesIterator.hasNext()) {
            String fieldName = fieldNamesIterator.next();
            Object value = model.get(fieldName);
            if (value == null) {
                fieldNamesIterator.remove();
            } else {
                result.put(fieldName, getValue(value));
            }
        }
        return result;
    }

    private static Object getValue(Object value) {
        if (value instanceof PGobject) {
            String data = ((PGobject) value).getValue();
            if (data.startsWith("{")) {
                value = new JsonObject(data);
            } else if (data.startsWith("[")) {
                value = new JsonArray(data);
            }
        } else if (value instanceof Timestamp) {
            value = ((Timestamp) value).getTime();
        } else if (value instanceof UUID) {
            value = value.toString();
        } else if (value instanceof Date) {
            value = value.toString();
        }
        return value;
    }
}
