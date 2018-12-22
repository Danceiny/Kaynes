package cc.cannot.dict.business.tree.impl;

import com.alibaba.fastjson.JSONObject;
import cc.cannot.dict.business.tree.TreeRepositoryService;
import cc.cannot.dict.common.utils.TimeUtils;
import cc.cannot.dict.persistence.entity.AreaEntity;
import cc.cannot.dict.persistence.entity.BaseTreeEntity;
import cc.cannot.dict.persistence.entity.CarEntity;
import cc.cannot.dict.persistence.entity.CategoryEntity;
import cc.cannot.dict.persistence.entity.constants.DictTypeEnum;
import cc.cannot.dict.persistence.entity.interfaces.BaseTreeDaoInterface;
import cc.cannot.dict.persistence.repository.AreaRepository;
import cc.cannot.dict.persistence.repository.CarRepository;
import cc.cannot.dict.persistence.repository.CategoryRepository;
import cc.cannot.dict.persistence.repository.RawRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据访问相关逻辑
 */
@Service
@Slf4j
public class TreeRepositoryServiceImpl implements TreeRepositoryService {

    private final CarRepository carRepository;
    private final AreaRepository areaRepository;
    private final CategoryRepository categoryRepository;
    private final RawRepository rawRepository;

    public TreeRepositoryServiceImpl(
            final CarRepository carRepository,
            final AreaRepository areaRepository,
            final CategoryRepository categoryRepository,
            final RawRepository rawRepository
    ) {
        this.carRepository = carRepository;
        this.areaRepository = areaRepository;
        this.categoryRepository = categoryRepository;
        this.rawRepository = rawRepository;
    }

    @Override
    public BaseTreeEntity get(final DictTypeEnum type, Object bid, boolean simple) {
        BaseTreeEntity entity = this.getEntity(type, bid, false);
        if (entity != null && !simple) {
            rawRepository.loadAttr(type, entity, bid);
        }
        return entity;
    }

    private BaseTreeDaoInterface getDao(final DictTypeEnum type) {
        switch (type) {
            case CAR:
                return carRepository;
            case AREA:
                return areaRepository;
            case CATEGORY:
                return categoryRepository;
            default:
                throw new NotImplementedException(type + " not implemented in TreeRepositoryServiceImpl");
        }
    }

    /**
     * @return Entity/null
     */
    @SuppressWarnings("unchecked")
    public BaseTreeEntity getEntity(final DictTypeEnum type, Object bid, boolean withTrashed) {
        BaseTreeDaoInterface dao = this.getDao(type);
        if (withTrashed) {
            return dao.findByBid(bid);
        }
        return dao.findByBidAndDeletedTime(bid, 0);
    }

    @Override
    public void delete(final DictTypeEnum type, final Object bid) {
        // 防一下错误调用
        if (bid instanceof BaseTreeEntity) {
            this.delete((BaseTreeEntity) bid);
            return;
        }
        BaseTreeEntity entity = this.get(type, bid, true);
        this.save(entity);
    }

    @Override
    public void delete(final BaseTreeEntity entity) {
        entity.setDeletedTime(TimeUtils.currentTimeSeconds());
        this.save(entity);
    }

    @Override
    public List<? extends BaseTreeEntity> getByParentBid(final DictTypeEnum type, final Object parentBid, final Sort.Direction order) {
        List<? extends BaseTreeEntity> nullList = new ArrayList<>();
        if (parentBid == null) {
            return nullList;
        }
        return rawRepository.getByParentBid(type, parentBid, order);
    }

    @Override
    public void add(final BaseTreeEntity entity) {
        this.save(entity);
    }

    @Override
    public void update(final BaseTreeEntity entity) {
        this.save(entity);
    }

    @Override
    public int getMaxWeight(final DictTypeEnum type, Object pid) {
        return rawRepository.getMaxWeightByPid(type, pid);
    }

    /**
     * 注：强制类型转换不可缺省
     */
    private void save(final BaseTreeEntity entity) {
        DictTypeEnum type = entity.getType();
        switch (type) {
            case CAR:
                carRepository.save((CarEntity) entity);
                break;
            case AREA:
                areaRepository.save((AreaEntity) entity);
                break;
            case CATEGORY:
                categoryRepository.save((CategoryEntity) entity);
                break;
            default:
                break;
        }
        if (entity.getAttrChanged()) {
            if (!entity.getAttrLoaded()) {
                JSONObject newAttr = (JSONObject) entity.getAttr().clone();
                rawRepository.loadAttr(type, entity, entity.getBid());
                entity.updateAttr(newAttr);
            }
            try {
                rawRepository.updateAttr(type, entity, entity.getBid());
            } catch (PersistenceException e) {
                log.error("failed to update attr");
            }
        }
    }
}
