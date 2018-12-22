package cc.cannot.dict.business.service;

import cc.cannot.dict.api.req.CommunityUpdateReq;
import cc.cannot.dict.api.resp.CommunityVO;

public interface CommunityService extends BaseService<Integer> {
    default CommunityVO get(int bid) {
        return this.get(bid, false);
    }

    CommunityVO get(int bid, boolean simple);

    CommunityVO add(final CommunityUpdateReq req);

    CommunityVO update(final CommunityUpdateReq req);
}
