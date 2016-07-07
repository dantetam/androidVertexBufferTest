package io.github.dantetam.opstrykontest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dante on 7/6/2016.
 */
public class MapModel<K> implements BaseModel {

    private Map<K, RenderEntity> map;

    public MapModel() {
        map = new HashMap<>();
    }

    public Collection<RenderEntity> parts() {
        return map.values();
    }

    public void put(K key, RenderEntity value) {
        map.put(key, value);
    }

    public void release() {
        for (RenderEntity en: map.values()) {
            en.release();
        }
    }

}
