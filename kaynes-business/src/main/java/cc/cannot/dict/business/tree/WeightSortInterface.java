package cc.cannot.dict.business.tree;

import cc.cannot.dict.persistence.entity.constants.DictTypeEnum;

public interface WeightSortInterface<T> {
    /**
     * 按照目标排序数组，调整权重值，使得排序生效，即resort含义是对底层数据进行重排序
     */
    void resort(final DictTypeEnum type, T[] sortedBids);

    /**
     * 单个元素的排序位置调整
     */
    void resort(final DictTypeEnum type, T previousBid, T bid, T nextBid);
}
