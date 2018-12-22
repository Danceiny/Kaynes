package cc.cannot.dict.persistence.repository;

import com.alibaba.fastjson.JSONObject;
import cc.cannot.dict.persistence.entity.BaseTreeEntity;
import cc.cannot.dict.persistence.entity.constants.DictTypeEnum;
import cc.cannot.dict.persistence.entity.interfaces.DynamicAttrInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.List;

@Component
@Slf4j
public class RawRepository {
    private static final int MAX_COL_LEN = 65535 / 3 - 1000;

    @PersistenceContext
    private EntityManager entityManager;

    public void loadAttr(DictTypeEnum type, final DynamicAttrInterface entity, Object bid) {
        String sql = String.format("SELECT attr FROM %s WHERE 1=1 AND bid = ?1",
                type.tableName());
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, bid);
        try {
            String json = (String) query.getSingleResult();
            JSONObject attr = JSONObject.parseObject(json);
            if (attr == null) {
                attr = new JSONObject();
            }
            entity.setAttr(attr);
            entity.setAttrLoaded(true);
        } catch (Exception e) {
            log.error("load attr failed: {}", e.getMessage());
        }
    }

    /**
     * 这里涉及嵌套事务，暂时没搞明白在spring里怎么搞比较合理，需要调用方catch一下异常，否则调用方那边可能会报事务被标记为回滚，不可提交。
     */
    @Transactional
    public void updateAttr(final DictTypeEnum type, final DynamicAttrInterface entity, Object bid) throws PersistenceException {
        if (entity == null) {
            return;
        }
        String attrJson = entity.getAttr().toJSONString();
        if (attrJson.length() > MAX_COL_LEN) {
            log.error("attr length cannot greater than {}", MAX_COL_LEN);
            return;
        }
        String sql = String.format("UPDATE %s SET attr = ?2 WHERE 1=1 AND bid = ?1",
                type.tableName());
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, bid);
        query.setParameter(2, attrJson);
        query.executeUpdate();
    }

    public int getMaxWeightByPid(DictTypeEnum type, Object pid) {
        String tableName = type.tableName();
        String sql = String.format("SELECT weight FROM %s WHERE 1=1 AND parent_bid = ?1 ORDER BY weight DESC",
                tableName);
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, pid);
        query.setMaxResults(1);
        try {
            return (int) query.getSingleResult();
        } catch (NoResultException e) {
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    public List<? extends BaseTreeEntity> getByParentBid(final DictTypeEnum type, final Object parentBid, final Sort.Direction order) {
        String sql = String.format("SELECT e FROM %s e WHERE 1=1 AND parent_bid = ?1 AND deleted_time = 0 ORDER BY weight %s",
                type.mappedEntityName(), order.toString());
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, parentBid);
        return query.getResultList();
    }
}
