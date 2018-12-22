package cc.cannot.dict.business.service.impl;

import cc.cannot.dict.api.req.AreaUpdateReq;
import cc.cannot.dict.api.resp.AreaVO;
import cc.cannot.dict.business.service.AreaService;
import cc.cannot.dict.business.tree.TreeService;
import cc.cannot.dict.persistence.entity.AreaEntity;
import cc.cannot.ms.springtime.modules.base.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.stream.Collectors;

import static cc.cannot.dict.persistence.entity.constants.DictTypeEnum.AREA;

@Service
@Slf4j
public class AreaServiceImpl implements AreaService {
    private final TreeService<Integer> treeService;

    public AreaServiceImpl(TreeService<Integer> treeService) {
        this.treeService = treeService;
    }

    private AreaVO transferEntity2VO(AreaEntity entity, boolean loadBrother) {
        if (entity == null) {
            return null;
        }
        AreaVO vo = new AreaVO();
        vo.setBid(entity.getBid());
        vo.setName(entity.getName());
        vo.setAttr(entity.getAttr());
        vo.setPid(entity.getParentBid());
        vo.setPinyin(entity.getPinyin());
        vo.setLevelOrdinal(entity.getLevel());
        vo.setWeight(entity.getWeight());
        if (!CollectionUtils.isEmpty(entity.getChildren())) {
            vo.setChildren(entity.getChildren().stream().map(child -> transferEntity2VO((AreaEntity) child, false)).collect(Collectors.toList()));
        }
        if (!CollectionUtils.isEmpty(entity.getParentChain())) {
            vo.setParentChain(entity.getParentChain().stream().map(parent -> transferEntity2VO((AreaEntity) parent, false)).collect(Collectors.toList()));
        }
        if (loadBrother && !CollectionUtils.isEmpty(entity.getBrotherBids())) {
            vo.setBrothers(entity.getBrotherBids().stream().map(brother -> this.get(brother, 0, 0, false)).collect(Collectors.toList()));
        }
        return vo;
    }

    @Override
    public AreaVO add(final AreaUpdateReq req) {
        AreaEntity areaEntity = new AreaEntity();
        this.setProperties(areaEntity, req);
        treeService.save(areaEntity);
        return transferEntity2VO(areaEntity, false);
    }

    @Override
    public AreaVO update(final AreaUpdateReq req) {
        AreaEntity old = (AreaEntity) treeService.get(AREA, (int) req.getBid());
        return updateProps(old, req);
    }

    private AreaVO updateProps(final AreaEntity old, final AreaUpdateReq req) {
        old.markOldTreeNode();
        this.setProperties(old, req);
        treeService.save(old);
        return transferEntity2VO(old, false);
    }

    private void setProperties(final AreaEntity old, final AreaUpdateReq req) {
        treeService.updateCommonProps(old, req);
        if (req.getPinyin() != null) {
            old.setPinyin(req.getPinyin());
        }
        if (req.getName() != null) {
            old.setName(req.getName());
        }
    }

    @Override
    public AreaVO get(final Integer bid, final int parentDepth, final int childrenDepth, boolean loadBrother) {
        AreaEntity areaEntity = (AreaEntity) treeService.get(AREA, bid,
                this.validateDepth(parentDepth), this.validateDepth(childrenDepth));
        if (areaEntity == null) {
            throw new NotFoundException();
        }
        return transferEntity2VO(areaEntity, loadBrother);
    }

    @Override
    public void delete(final Integer bid) {
        treeService.delete(AREA, bid);
    }

    @Override
    public void resort(final Integer[] bids) {
        treeService.resort(AREA, bids);
    }
}
