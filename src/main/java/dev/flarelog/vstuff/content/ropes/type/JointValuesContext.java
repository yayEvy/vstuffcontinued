package dev.flarelog.vstuff.content.ropes.type;

import java.lang.reflect.RecordComponent;
import java.util.HashMap;
import java.util.Map;

public record JointValuesContext(float maxDistance, float spacing, float length, int segments, int index) {
    public static final JointValuesContext SAMPLE = new JointValuesContext(1, 1, 1, 1, 0);   // all non-zero so the dry run doesn't divide by zero

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        try {
            for (RecordComponent component : this.getClass().getRecordComponents()) {
                Object value = component.getAccessor().invoke(this);
                map.put(component.getName(), value);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert record to map", e);
        }
        return map;
    }
}