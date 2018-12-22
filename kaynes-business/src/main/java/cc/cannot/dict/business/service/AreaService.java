package cc.cannot.dict.business.service;

import cc.cannot.dict.api.req.AreaUpdateReq;
import cc.cannot.dict.api.resp.AreaVO;

public interface AreaService extends BaseService<Integer> {
    AreaVO add(AreaUpdateReq req);

    AreaVO update(AreaUpdateReq req);

    default int validateDepth(int depth) {
        return depth > getMaxDepth() ? getMaxDepth() : depth;
    }

    default int getMaxDepth() {
        return 6;
    }
}
