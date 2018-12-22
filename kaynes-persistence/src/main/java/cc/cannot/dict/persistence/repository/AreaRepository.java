package cc.cannot.dict.persistence.repository;

import cc.cannot.dict.persistence.entity.AreaEntity;
import cc.cannot.dict.persistence.entity.interfaces.BaseTreeDaoInterface;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AreaRepository extends JpaRepository<AreaEntity, Integer>, BaseTreeDaoInterface<Integer> {
}
