package com.networknt.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class has two public methods called mergeObject and mergeMap which is
 * used to merge config file with the values generate by ConfigInjection.class.
 * <p>
 * The first method "mergeMap" is used to merge map config with values and
 * return another merged map while the second one "mergeObject" is used to merge
 * map config with values and return a mapping object. the merge logic is based on
 * depth first search.
 * <p>
 * Created by jiachen on 2019-01-08.
 */
public class CentralizedManagement {
    // Merge map config with values generated by ConfigInjection.class and return map
    public static void mergeMap(Map<String, Object> config) {
        merge(config);
    }
    // Merge map config with values generated by ConfigInjection.class and return mapping object
    public static Object mergeObject(Object config, Class clazz) {
        merge(config);
        return convertMapToObj((Map<String, Object>) config, clazz);
    }
    // Search the config map recursively, expand List and Map level by level util no further expand
    private static void merge(Object m1) {
        if (m1 instanceof Map) {
            Iterator<Object> fieldNames = ((Map<Object, Object>) m1).keySet().iterator();
            String fieldName = null;
            while (fieldNames.hasNext()) {
                fieldName = String.valueOf(fieldNames.next());
                Object field1 = ((Map<String, Object>) m1).get(fieldName);
                if (field1 != null) {
                    if (field1 instanceof Map || field1 instanceof List) {
                        merge(field1);
                    // Overwrite previous value when the field1 can not be expanded further
                    } else if (field1 instanceof String) {
                        // Retrieve values from ConfigInjection.class
                        Object injectValue = ConfigInjection.getInjectValue((String) field1);
                        ((Map<String, Object>) m1).put(fieldName, injectValue);
                    }
                }
            }
        } else if (m1 instanceof List) {
            for (int i = 0; i < ((List<Object>) m1).size(); i++) {
                Object field1 = ((List<Object>) m1).get(i);
                if (field1 instanceof Map || field1 instanceof List) {
                    merge(field1);
                // Overwrite previous value when the field1 can not be expanded further
                } else if (field1 instanceof String) {
                    // Retrieve values from ConfigInjection.class
                    Object injectValue = ConfigInjection.getInjectValue((String) field1);
                    ((List<Object>) m1).set(i, injectValue);
                }
            }
        }
    }

    // Method used to convert map to object based on the reference class provided
    private static Object convertMapToObj(Map<String, Object> map, Class clazz) {
        ObjectMapper mapper = new ObjectMapper();
        Object obj = mapper.convertValue(map, clazz);
        return obj;
    }
}
