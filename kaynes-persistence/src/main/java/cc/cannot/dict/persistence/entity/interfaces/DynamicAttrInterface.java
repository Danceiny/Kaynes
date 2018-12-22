package cc.cannot.dict.persistence.entity.interfaces;

import com.alibaba.fastjson.JSONObject;

public interface DynamicAttrInterface {

    Boolean getAttrChanged();

    Boolean getAttrLoaded();

    void setAttrChanged(Boolean b);

    void setAttrLoaded(Boolean b);

    JSONObject getAttr();

    void setAttr(JSONObject attr);

    /**
     * 可供外部调用的属性动态更新方法
     */
    default void updateAttr(JSONObject attr) {
        if (!attr.isEmpty()) {
            setAttrChanged(true);
        }
        if (!getAttr().isEmpty()) {
            this.mergeAttr(attr);
        } else {
            this.setAttr(attr);
        }
    }

    // 私有方法，外部请不要调用
    // merge attributes
    // NOTICE: if value is null, then remove it, or else overwrite
    default void mergeAttr(JSONObject attr) {
        attr.forEach((k, v) -> {
            if (v == null) {
                getAttr().remove(k);
            } else {
                getAttr().put(k, v);
            }
        });
    }
}
