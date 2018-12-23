package cc.cannot.dict.business.service.impl;

import cc.cannot.common.exceptions.NotFoundException;
import cc.cannot.dict.api.req.CommunityUpdateReq;
import cc.cannot.dict.api.resp.CommunityVO;
import cc.cannot.dict.business.service.CommunityService;
import cc.cannot.dict.persistence.entity.CommunityEntity;
import cc.cannot.dict.persistence.entity.constants.DictTypeEnum;
import cc.cannot.dict.persistence.repository.CommunityRepository;
import cc.cannot.dict.persistence.repository.RawRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.PersistenceException;

@Service
@Slf4j
public class CommunityServiceImpl implements CommunityService {

    private final CommunityRepository communityRepository;

    private final RawRepository rawRepository;

    public CommunityServiceImpl(CommunityRepository communityRepository, RawRepository rawRepository) {
        this.communityRepository = communityRepository;
        this.rawRepository = rawRepository;
    }

    private CommunityVO transferEntity2VO(CommunityEntity entity) {
        if (entity == null) {
            return null;
        }
        CommunityVO vo = new CommunityVO();
        vo.setName(entity.getName());
        vo.setBid(entity.getBid());
        vo.setAttr(entity.getAttr());
        vo.setAddress(entity.getAddress());
        vo.setCityBid(entity.getCityBid());
        vo.setDesc(entity.getDesc());
        vo.setTownBid(entity.getTownBid());
        return vo;
    }

    @Override
    public CommunityVO get(final int bid, boolean simple) {
        CommunityEntity entity = this.getEntity(bid, false);
        if (entity == null) {
            throw new NotFoundException();
        }
        if (!simple) {
            rawRepository.loadAttr(DictTypeEnum.COMMUNITY, entity, bid);
        }
        return transferEntity2VO(entity);
    }

    private CommunityEntity getEntity(final int bid, boolean withTrashed) {
        if (withTrashed) {
            return communityRepository.findByBid(bid);
        }
        return communityRepository.findByBidAndDeletedTime(bid, 0);
    }

    @Override
    public CommunityVO add(final CommunityUpdateReq req) {
        CommunityEntity entity = new CommunityEntity();
        return updateProps(entity, req);
    }

    @Override
    public CommunityVO update(final CommunityUpdateReq req) {
        CommunityEntity old = this.getEntity(req.getBid(), false);
        if (old == null) {
            throw new NotFoundException(String.format("not found by name: %s", req.getName()));
        }
        return this.updateProps(old, req);
    }

    private CommunityVO updateProps(final CommunityEntity old, final CommunityUpdateReq req) {
        this.setProperties(old, req);
        communityRepository.save(old);
        if (req.getAttr() != null && !req.getAttr().isEmpty()) {
            if (!old.getAttrLoaded()) {
                rawRepository.loadAttr(DictTypeEnum.COMMUNITY, old, old.getBid());
            }
            old.updateAttr(req.getAttr());
            try {
                rawRepository.updateAttr(DictTypeEnum.COMMUNITY, old, old.getBid());
            } catch (PersistenceException e) {
                log.error("failed to update attr");
            }
        }
        return transferEntity2VO(old);
    }

    private void setProperties(final CommunityEntity old, final CommunityUpdateReq req) {
        if (req.getName() != null) {
            old.setName(req.getName());
        }
        if (req.getPinyin() != null) {
            old.setPinyin(req.getPinyin());
        }
        if (req.getCityBid() != null) {
            old.setCityBid(req.getCityBid());
        }
        if (req.getTownBid() != null) {
            old.setTownBid(req.getTownBid());
        }
        if (req.getAddress() != null) {
            old.setAddress(req.getAddress());
        }
        String desc = req.getDesc();
        if (desc != null) {
            if (desc.length() > CommunityEntity.MAX_DESC_LEN) {
                log.warn("description过长，将截断并加上`...`");
                old.setDesc(desc.substring(0, CommunityEntity.MAX_DESC_LEN - 3) + "...");
            } else {
                old.setDesc(desc);
            }
        }
    }

    @Override
    public void delete(final Integer bid) {
        CommunityEntity entity = this.getEntity(bid, false);
        if (entity != null) {
            communityRepository.save(entity);
        }
    }
}
