package cc.cannot.dict.business.service;

import cc.cannot.dict.api.resp.BaseTreeVO;
import org.apache.commons.lang3.NotImplementedException;

public interface BaseService<T> {

    default BaseTreeVO get(T bid, int parentDepth, int childrenDepth, boolean loadBrother) {
        return null;
    }

    default void delete(T bid) {
        throw new NotImplementedException("暂不支持删除");
    }

    default void resort(T[] bids) {
        throw new NotImplementedException("暂不支持排序");
    }
}
