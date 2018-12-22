package cc.cannot.dict.persistence.entity.constants;

import cc.cannot.dict.persistence.entity.AreaEntity;
import cc.cannot.dict.persistence.entity.CarEntity;
import cc.cannot.dict.persistence.entity.CategoryEntity;
import cc.cannot.dict.persistence.entity.CommunityEntity;
import org.apache.commons.lang3.NotImplementedException;

import java.lang.reflect.Type;

/**
 * 实体类型
 */
public enum DictTypeEnum {
    COMMON,
    CATEGORY,
    AREA,
    COMMUNITY,
    CAR,
    ;

    public Type mappedClass() {
        switch (this) {
            case AREA:
                return AreaEntity.class;
            case CAR:
                return CarEntity.class;
            case CATEGORY:
                return CategoryEntity.class;
            case COMMUNITY:
                return CommunityEntity.class;
            default:
                return Void.class;
        }
    }

    public String tableName() {
        switch (this) {
            case AREA:
                return "dict_area";
            case CAR:
                return "dict_car";
            case CATEGORY:
                return "dict_category";
            case COMMUNITY:
                return "dict_community";
            default:
                throw new NotImplementedException("数据库表不存在：" + this.toString());
        }
    }

    public boolean useHashCache() {
        return this.equals(CATEGORY);
    }

    public String mappedEntityName() {
        return this.mappedClass().getTypeName();
    }
}
