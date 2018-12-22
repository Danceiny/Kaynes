package cc.cannot.dict.business.tree;

import cc.cannot.dict.persistence.entity.BaseTreeEntity;
import cc.cannot.dict.persistence.entity.constants.DictTypeEnum;

import java.util.List;

public interface TreeCacheService {
    // parent

    /**
     * 返回空list表示是真的没有pids，不需要再查DB啦；返回null表示缓存里没有，还是得查DB
     */
    List<Object> getParentBids(DictTypeEnum type, Object bid);

    List<List<Object>> multiGetParentBids(DictTypeEnum type, List<String> bids);

    void setParentBids(final DictTypeEnum type, final Object bid, final List<Object> pids);

    void deleteParentBids(final DictTypeEnum type, final Object bid);

    // children

    /**
     * 返回空list表示是真的没有cids，不需要再查DB啦；返回null表示缓存里没有，还是得查DB
     */
    List<Object> getChildrenBids(DictTypeEnum type, Object bid);

    List<List<Object>> multiGetChildrenBids(DictTypeEnum type, List<String> bids);

    void setChildrenBids(final DictTypeEnum type, final Object bid, final List<Object> cids);

    void deleteChildrenBids(final DictTypeEnum type, final Object bid);

    // entity cache
    void cacheEntity(BaseTreeEntity entity, boolean simple);

    void multiCacheEntity(List<BaseTreeEntity> entities);

    default void cacheEntity(BaseTreeEntity entity) {
        this.cacheEntity(entity, false);
        this.cacheEntity(entity, true);
    }

    void deleteEntityCache(final DictTypeEnum type, final Object bid, boolean simple);

    void multiDeleteEntityCache(final DictTypeEnum type, final List<Object> bids, boolean simple);

    void multiDeleteParentIds(final DictTypeEnum type, final List<Object> bids);


    default void deleteEntityCache(final DictTypeEnum type, final Object bid) {
        deleteEntityCache(type, bid, true);
        deleteEntityCache(type, bid, false);
    }


    BaseTreeEntity getEntityCache(final DictTypeEnum type, final Object bid, boolean simple);

    List<BaseTreeEntity> multiGetEntityCache(final DictTypeEnum type, final List<Object> bid, boolean simple);

    default BaseTreeEntity getEntityCache(final DictTypeEnum type, final Object bid) {
        return this.getEntityCache(type, bid, false);
    }

}
