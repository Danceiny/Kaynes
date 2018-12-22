package cc.cannot.dict.business.tree.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import cc.cannot.dict.business.redis.Redis;
import cc.cannot.dict.business.tree.TreeCacheService;
import cc.cannot.dict.persistence.entity.BaseTreeEntity;
import cc.cannot.dict.persistence.entity.constants.DictTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * redis连接太慢了，连接次数随children层级深度增加而指数级增长，导致childrenDepth较大时响应时间暴涨
 * 可能的解决方案：pids和cids优先走静态缓存？
 */
@Service
@Slf4j
public class TreeCacheServiceImpl implements TreeCacheService {

    private static final int ENTITY_CACHE_DAYS = 30;

    private static final int PARENT = 0;
    private static final int CHILDREN = 1;
    private final Redis redis;

    public TreeCacheServiceImpl(Redis redis) {
        this.redis = redis;
    }

    @Override
    public List<Object> getParentBids(DictTypeEnum type, Object bid) {
        return redis.hget(this.getBranchKey(type, PARENT), String.valueOf(bid), ArrayList.class);
    }

    @Override
    public List<List<Object>> multiGetParentBids(final DictTypeEnum type, final List<String> bids) {
        return this.multiGetBranchBids(type, bids, PARENT);
    }

    @Override
    public List<Object> getChildrenBids(DictTypeEnum type, Object bid) {
        return redis.hget(this.getBranchKey(type, CHILDREN), String.valueOf(bid), ArrayList.class);
    }

    @Override
    public List<List<Object>> multiGetChildrenBids(final DictTypeEnum type, final List<String> bids) {
        return this.multiGetBranchBids(type, bids, CHILDREN);
    }


    @Override
    public void setParentBids(final DictTypeEnum type, final Object bid, final List<Object> pids) {
        this.setBranchBids(type, bid, pids, PARENT);
    }

    @Override
    public void setChildrenBids(final DictTypeEnum type, final Object bid, final List<Object> cids) {
        this.setBranchBids(type, bid, cids, CHILDREN);
    }

    @Override
    public void deleteParentBids(final DictTypeEnum type, final Object bid) {
        this.deleteBranchBidsCache(type, bid, PARENT);
    }

    @Override
    public void deleteChildrenBids(final DictTypeEnum type, final Object bid) {
        this.deleteBranchBidsCache(type, bid, CHILDREN);
    }

    @Override
    public void cacheEntity(BaseTreeEntity entity, boolean simple) {
        JSONObject tmp = entity.getAttr();
        DictTypeEnum type = entity.getType();
        if (type.useHashCache()) {
            redis.hsetAsJson(this.getTableKey(type), String.valueOf(entity.getBid()), entity);
            return;
        }
        int days = ENTITY_CACHE_DAYS / 3;
        if (simple) {
            days = ENTITY_CACHE_DAYS;
            entity.setAttr(null);
        }
        redis.setAsJson(this.getEntityKey(entity.getType(), entity.getBid(), simple), entity, days, TimeUnit.DAYS);
        entity.setAttr(tmp);
    }

    @Override
    public void multiCacheEntity(final List<BaseTreeEntity> entities) {
        DictTypeEnum type = entities.get(0).getType();
        Map<String, String> map = new HashMap<>();
        for (BaseTreeEntity entity : entities) {
            map.put(String.valueOf(entity.getBid()), JSONObject.toJSONString(entity));
        }
        if (type.useHashCache()) {
            redis.hmSet(this.getTableKey(type), map);
        } else {
            redis.mset(map);
        }
    }

    @Override
    public void deleteEntityCache(final DictTypeEnum type, final Object bid, boolean simple) {
        if (type.useHashCache()) {
            redis.hdel(this.getTableKey(type), String.valueOf(bid));
            return;
        }
        redis.remove(this.getEntityKey(type, bid, simple));
    }

    @Override
    public void multiDeleteEntityCache(final DictTypeEnum type, final List<Object> bids, final boolean simple) {
        //TODO
    }

    @Override
    public void multiDeleteParentIds(final DictTypeEnum type, final List<Object> bids) {
        int l = bids.size();
        if (l == 0) {
            return;
        }
        String[] fields = new String[bids.size()];
        for (int i = 0; i < l; i++) {
            fields[i] = String.valueOf(bids.get(i));
        }
        redis.hdel(this.getBranchKey(type, PARENT), fields);
    }

    @Override
    public BaseTreeEntity getEntityCache(final DictTypeEnum type, final Object bid, boolean simple) {
        if (type.useHashCache()) {
            String k = this.getTableKey(type);
            try {
                return redis.hgetObj(k, String.valueOf(bid), type.mappedClass());
            } catch (Exception e) {
                log.warn("hash cache read exception: {}", e);
                redis.hdel(k, String.valueOf(bid));
                return null;
            }
        }
        String k = this.getEntityKey(type, bid, simple);
        try {
            return redis.getObj(k, type.mappedClass());
        } catch (Exception e) {
            log.warn("string cache read exception: {}", e);
            redis.remove(k);
            return null;
        }
    }

    /**
     * Notice: may contain null
     */
    @Override
    public List<BaseTreeEntity> multiGetEntityCache(final DictTypeEnum type, final List<Object> bids, boolean simple) {
        List<BaseTreeEntity> entities = new ArrayList<>();
        List<String> jsons;
        if (type.useHashCache()) {
            jsons = redis.hmGet(this.getTableKey(type), bids);
        } else {
            List<String> keys = new ArrayList<>();
            for (Object bid : bids) {
                keys.add(this.getEntityKey(type, bid, simple));
            }
            jsons = redis.mget(keys);
        }
        for (String json : jsons) {
            entities.add(JSONObject.parseObject(json, type.mappedClass()));
        }
        return entities;
    }

    /**
     * redis string
     */
    private String getEntityKey(final DictTypeEnum type, final Object bid, boolean simple) {
        return String.format("Dict:Entity:%s:%s:%s", type, simple ? "SIMPLE" : "FULL", bid);
    }

    /**
     * redis hash
     */
    private String getTableKey(final DictTypeEnum type) {
        return String.format("Dict:EntityHash:%s", type);
    }

    /**
     * redis key of `branch`
     * Definition of `branch`:
     * entity爷爷 -> entity爸爸 -> entity儿子
     * entity爷爷 -> entity爸爸 -> entity女儿
     * 儿子的ParentBranch => [entity爸爸, entity爷爷] //有序
     * 女儿的ParentBranch => [entity爸爸, entity爷爷] //有序
     * 爸爸的ChildrenBranch => [entity儿子，entity女儿]
     */
    private String getBranchKey(final DictTypeEnum type, int flag) {
        return String.format("Dict:%s:%s", flag, type);
    }

    /**
     * get pids/cids from cache
     *
     * @return List, or null (null意味着缓存未命中，而空数组意味着真的没有）
     */
    private List<List<Object>> multiGetBranchBids(final DictTypeEnum type, final List<String> bids, int flag) {
        List<String> jsons = redis.hmGet(this.getBranchKey(type, flag), bids);
        List<List<Object>> ids = new ArrayList<>();
        for (String json : jsons) {
            ids.add(JSONObject.parseObject(json, new TypeReference<ArrayList<Object>>() {}));
        }
        return ids;
    }

    /**
     * cache all parent bids or first-level children bids
     */
    private void setBranchBids(final DictTypeEnum type, final Object bid, final List<Object> bids, int flag) {
        String k = this.getBranchKey(type, flag);
        redis.hsetAsJson(k, String.valueOf(bid), bids);
    }

    /**
     * delete redis key
     */
    private void deleteBranchBidsCache(final DictTypeEnum type, final Object bid, int flag) {
        String k = this.getBranchKey(type, flag);
        redis.hdel(k, String.valueOf(bid));
    }
}
