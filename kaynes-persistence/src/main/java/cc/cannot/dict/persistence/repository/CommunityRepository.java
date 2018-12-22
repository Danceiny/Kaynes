package cc.cannot.dict.persistence.repository;

import cc.cannot.dict.persistence.entity.CommunityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityRepository extends JpaRepository<CommunityEntity, Integer> {
    CommunityEntity findByBid(int bid);
    CommunityEntity findByBidAndDeletedTime(int bid, int deletedTime);
}
