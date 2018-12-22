package cc.cannot.dict.persistence.entity;

import com.alibaba.fastjson.annotation.JSONField;
import cc.cannot.dict.api.constant.CarLevelEnum;
import cc.cannot.dict.common.utils.PrimeTypeUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import static cc.cannot.dict.persistence.entity.constants.DictTypeEnum.CAR;

@EqualsAndHashCode(callSuper = true)
@Entity(name = "car")
@Data
@Table(name = "dict_car")
@DynamicInsert
@DynamicUpdate
@ToString(callSuper = true)
public class CarEntity extends BaseTreeEntity<Integer> {
    @JsonIgnore
    @JSONField(serialize = false)
    public static final int DEFAULT_BID = 0;

    @Column(name = "bid", unique = true)
    private Integer bid;

    @Column(name = "parent_bid")
    private Integer parentBid;

    @Column(name = "pinyin")
    private String pinyin;

    public CarLevelEnum getLevelEnum() {
        return CarLevelEnum.values()[PrimeTypeUtils.shortValue(getLevel())];
    }

    public CarEntity() {
        super(CAR, DEFAULT_BID);
    }
}
