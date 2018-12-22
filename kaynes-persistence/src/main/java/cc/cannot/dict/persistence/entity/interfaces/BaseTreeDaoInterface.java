package cc.cannot.dict.persistence.entity.interfaces;

import cc.cannot.dict.persistence.entity.BaseTreeEntity;

public interface BaseTreeDaoInterface<T> {

    BaseTreeEntity<T> findByBid(T bid);

    BaseTreeEntity<T> findByBidAndDeletedTime(T bid, int deletedTime);
}
