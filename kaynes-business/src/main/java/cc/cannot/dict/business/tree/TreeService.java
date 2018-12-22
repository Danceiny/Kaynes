package cc.cannot.dict.business.tree;

import cc.cannot.dict.api.req.BaseTreeUpdateReq;
import cc.cannot.dict.persistence.entity.BaseTreeEntity;
import cc.cannot.dict.persistence.entity.constants.DictTypeEnum;

public interface TreeService<T> extends WeightSortInterface<T> {
    /**
     * @param type          DictTypeEnum
     * @param bid           business id
     * @param simple        是否去掉一些属性
     * @param parentDepth   -1 => all, 0 => none
     * @param childrenDepth -1 => all, 0 => none
     */
    BaseTreeEntity get(final DictTypeEnum type, T bid, boolean simple, int parentDepth, int childrenDepth);

    default BaseTreeEntity get(final DictTypeEnum type, T bid) {
        return this.get(type, bid, false, 0, 0);
    }

    default BaseTreeEntity get(final DictTypeEnum type, T bid, int parentDepth, int childrenDepth) {
        return this.get(type, bid, false, parentDepth, childrenDepth);
    }


    void delete(final DictTypeEnum type, T bid);

    /**
     * 新增或更新，通过entity.id是否为空来判断
     */
    void save(final BaseTreeEntity entity);

    /**
     * 一些通用属性的赋值
     */
    void updateCommonProps(final BaseTreeEntity entity, final BaseTreeUpdateReq req);

    /**
     * 根据入参的顺序，调整他们的权重值，使权重值契合排序
     * 注意，入参是`可排序集合`中的一个子集，对于非完全子集，将不保证该子集排序是“紧密有序”的（即中间可能乱入`可排序集合`中的其他元素）
     * `可排序集合`的定义是：具有相同pid的所有元素集合
     * 这个接口暴露的意义在于，可以让各个继承的service自由实现`entity列表的获取`
     *
     * @param sortedEntities 目标排序结果，这里使用[] 而非List，是为了避免泛型问题
     */
    void adjustSortedWeight(final BaseTreeEntity[] sortedEntities);
}
