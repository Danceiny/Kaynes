package cc.cannot.dict.business.service;

import cc.cannot.dict.api.req.CategoryUpdateReq;
import cc.cannot.dict.api.resp.CategoryVO;

public interface CategoryService extends BaseService<String> {

    CategoryVO insert(CategoryUpdateReq req);

    CategoryVO update(CategoryUpdateReq req);

    default int validateDepth(int depth) {
        return depth > getMaxDepth() ? getMaxDepth() : depth;
    }

    default int getMaxDepth() {
        return 5;
    }
}
