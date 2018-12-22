package cc.cannot.dict.business.service.impl;

import cc.cannot.dict.api.req.CategoryUpdateReq;
import cc.cannot.dict.api.resp.BaseTreeVO;
import cc.cannot.dict.api.resp.CategoryVO;
import cc.cannot.dict.business.service.CategoryService;
import cc.cannot.dict.business.tree.TreeService;
import cc.cannot.dict.persistence.entity.CategoryEntity;
import cc.cannot.ms.springtime.modules.base.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.stream.Collectors;

import static cc.cannot.dict.persistence.entity.constants.DictTypeEnum.CATEGORY;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final TreeService<String> treeService;

    public CategoryServiceImpl(TreeService<String> treeService) {
        this.treeService = treeService;
    }

    @Override
    public CategoryVO insert(CategoryUpdateReq req) {
        req.validateAdd();
        CategoryEntity entity = new CategoryEntity();
        entity.setBid(req.getBid().toString());
        entity.setLevel(req.getLevel());
        entity.setParentBid(req.getPid().toString());
        entity.setName(req.getName());
        entity.setAttr(req.getAttr());
        this.treeService.save(entity);
        return this.transferEntity2VO(entity);
    }

    @Override
    public CategoryVO update(CategoryUpdateReq req) {
        CategoryEntity entity = (CategoryEntity) treeService.get(CATEGORY, (String) req.getBid());
        if (entity == null) {
            throw new NotFoundException(String.format("not found by bid: %s", req.getBid()));
        }
        entity.markOldTreeNode();
        entity.setLevel(req.getLevel());
        entity.setParentBid(req.getPid().toString());
        entity.setName(req.getName());
        entity.updateAttr(req.getAttr());
        this.treeService.save(entity);
        return this.transferEntity2VO(entity);
    }

    @Override
    public BaseTreeVO get(final String bid, final int parentDepth, final int childrenDepth, boolean loadBrother) {
        CategoryEntity entity = (CategoryEntity) treeService.get(CATEGORY, bid,
                this.validateDepth(parentDepth), this.validateDepth(childrenDepth));
        if (entity == null) {
            throw new NotFoundException();
        }
        return this.transferEntity2VO(entity);
    }

    private CategoryVO transferEntity2VO(CategoryEntity entity) {
        if (entity == null) {
            return null;
        }
        CategoryVO vo = new CategoryVO();
        vo.setBid(entity.getBid());
        vo.setName(entity.getName());
        vo.setLevelOrdinal(entity.getLevel());
        vo.setWeight(entity.getWeight());
        vo.setAttr(entity.getAttr());
        if (!CollectionUtils.isEmpty(entity.getChildren())) {
            vo.setChildren(entity.getChildren().stream().map(child -> transferEntity2VO((CategoryEntity) child)).collect(Collectors.toList()));
        }
        if (!CollectionUtils.isEmpty(entity.getParentChain())) {
            vo.setParentChain(entity.getParentChain().stream().map(parent -> transferEntity2VO((CategoryEntity) parent)).collect(Collectors.toList()));
        }
        return vo;
    }

    @Override
    public void resort(final String[] bids) {
        treeService.resort(CATEGORY, bids);
    }

    @Override
    public void delete(String bid) {
        this.treeService.delete(CATEGORY, bid);
    }

}
