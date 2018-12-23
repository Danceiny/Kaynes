package cc.cannot.dict.persistence.entity.interfaces;

import cc.cannot.common.utils.PrimeTypeUtils;


public interface WeightInterface {
    Integer getWeight();

    default boolean gt(WeightInterface o) {
        return compareTo(o) > 0;
    }

    default boolean lt(WeightInterface o) {
        return compareTo(o) < 0;
    }

    default boolean eq(WeightInterface o) {
        return compareTo(o) == 0;
    }

    default int compareTo(WeightInterface o) {
        if (o != null) {
            if (PrimeTypeUtils.lt(this.getWeight(), o.getWeight())) {
                return 1;
            } else if (PrimeTypeUtils.gt(this.getWeight(), o.getWeight())) {
                return -1;
            }
            return 0;
        }
        return -1;
    }
}
