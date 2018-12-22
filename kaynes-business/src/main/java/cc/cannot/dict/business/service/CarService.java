package cc.cannot.dict.business.service;

import cc.cannot.dict.api.req.CarUpdateReq;
import cc.cannot.dict.api.resp.CarVO;

public interface CarService extends BaseService<Integer> {

    CarVO add(CarUpdateReq req);

    CarVO update(CarUpdateReq req);

    default int validateDepth(int depth) {
        return depth > getMaxDepth() ? getMaxDepth() : depth;
    }

    default int getMaxDepth() {
        return 3;
    }

}
