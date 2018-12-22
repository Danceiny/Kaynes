package cc.cannot.dict.business.tree;

import cc.cannot.dict.persistence.entity.BaseTreeEntity;
import cc.cannot.dict.persistence.entity.constants.DictTypeEnum;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * 操作数据实体的工厂: MySQL
 * Cache -> ES -> MySQL
 */
public interface TreeRepositoryService {

    BaseTreeEntity get(final DictTypeEnum type, Object bid, boolean simple);

    void delete(final DictTypeEnum type, Object bid);

    void delete(final BaseTreeEntity entity);

    /**
     * 查找子节点，默认按照权重值升序排列（！！！请注意该接口是默认有序的！！！）
     * 目前没有分页，数据量不应该太大（性能隐患）
     */
    default List<? extends BaseTreeEntity> getByParentBid(final DictTypeEnum type, Object parentBid) {
        return getByParentBid(type, parentBid, Sort.Direction.ASC);
    }

    /**
     * 返回非空
     */
    List<? extends BaseTreeEntity> getByParentBid(final DictTypeEnum type, Object parentBid, Sort.Direction sort);

    void add(final BaseTreeEntity data);

    void update(final BaseTreeEntity entity);

    int getMaxWeight(final DictTypeEnum type, Object pid);
}
