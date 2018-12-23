package cc.cannot.dict.persistence.entity.interfaces;

import com.alibaba.fastjson.annotation.JSONField;
import cc.cannot.common.utils.PrimeTypeUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @param <T> bid的类型
 */
public interface BaseTreeInterface<T> {

    T getBid();

    void setBid(T bid);

    T getParentBid();

    void setParentBid(T bid);

    Short getLevel();

    @JsonIgnore
    @JSONField(serialize = false)
    default int getInitWeightInterval() {
        return 1000000;
    }

    @JsonIgnore
    @JSONField(serialize = false)
    default int getLevelWeightInterval() {
        return getInitWeightInterval() / (PrimeTypeUtils.shortValue(getLevel()) + 1);
    }
}
