package cc.cannot.dict.persistence.repository;

import cc.cannot.dict.persistence.entity.CategoryEntity;
import cc.cannot.dict.persistence.entity.interfaces.BaseTreeDaoInterface;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Integer>, BaseTreeDaoInterface<String> {
}
