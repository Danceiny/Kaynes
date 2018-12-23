package cc.cannot.dict.business.service.impl;

import cc.cannot.common.exceptions.NotFoundException;
import cc.cannot.dict.api.req.CarUpdateReq;
import cc.cannot.dict.api.resp.CarVO;
import cc.cannot.dict.business.service.CarService;
import cc.cannot.dict.business.tree.TreeService;
import cc.cannot.dict.persistence.entity.CarEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.stream.Collectors;

import static cc.cannot.dict.persistence.entity.constants.DictTypeEnum.CAR;


@Service
@Slf4j
public class CarServiceImpl implements CarService {
    private final TreeService<Integer> treeService;

    private CarVO transferEntity2VO(CarEntity entity) {
        if (entity == null) {
            return null;
        }
        CarVO vo = new CarVO();
        vo.setName(entity.getName());
        vo.setBid(entity.getBid());
        vo.setLevelOrdinal(entity.getLevel());
        vo.setAttr(entity.getAttr());
        vo.setWeight(entity.getWeight());
        vo.setPid(entity.getParentBid());
        if (!CollectionUtils.isEmpty(entity.getChildren())) {
            vo.setChildren(entity.getChildren().stream().map(child -> transferEntity2VO((CarEntity) child))
                                 .collect(Collectors.toList()));
        }
        if (!CollectionUtils.isEmpty(entity.getParentChain())) {
            vo.setParentChain(entity.getParentChain().stream().map(parent -> transferEntity2VO((CarEntity) parent))
                                    .collect(Collectors.toList()));
        }
        return vo;
    }

    @Autowired
    public CarServiceImpl(TreeService<Integer> treeService) {this.treeService = treeService;}

    @Override
    public CarVO get(Integer bid, int parentDepth, int childrenDepth, boolean loadBrother) {
        CarEntity carEntity = (CarEntity) treeService.get(CAR, bid, validateDepth(parentDepth), validateDepth(childrenDepth));
        if (carEntity == null) {
            throw new NotFoundException();
        }
        return transferEntity2VO(carEntity);
    }


    @Override
    public void delete(Integer bid) {
        treeService.delete(CAR, bid);
    }

    @Override
    public CarVO add(final CarUpdateReq req) {
        CarEntity carEntity = new CarEntity();
        return this.updateProps(carEntity, req);
    }

    @Override
    public CarVO update(final CarUpdateReq req) {
        CarEntity old = (CarEntity) treeService.get(CAR, (int) req.getBid());
        if (old == null) {
            throw new NotFoundException(String.format("not found by bid: %d", (int) req.getBid()));
        }
        return this.updateProps(old, req);
    }

    @Override
    public void resort(final Integer[] bids) {
        treeService.resort(CAR, bids);
    }

    private CarVO updateProps(final CarEntity old, final CarUpdateReq req) {
        old.markOldTreeNode();
        this.setProperties(old, req);
        treeService.save(old);
        return transferEntity2VO(old);
    }

    private void setProperties(final CarEntity old, final CarUpdateReq req) {
        treeService.updateCommonProps(old, req);
        if (req.getName() != null) {
            old.setName(req.getName());
        }
        if (req.getPinyin() != null) {
            old.setPinyin(req.getPinyin());
        }
    }
}
