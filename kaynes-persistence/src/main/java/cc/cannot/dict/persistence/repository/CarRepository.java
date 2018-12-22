package cc.cannot.dict.persistence.repository;

import cc.cannot.dict.persistence.entity.CarEntity;
import cc.cannot.dict.persistence.entity.interfaces.BaseTreeDaoInterface;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<CarEntity, Integer>, BaseTreeDaoInterface<Integer> {

}
