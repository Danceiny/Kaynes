package cc.cannot.dict.persistence.entity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;
import cc.cannot.dict.api.constant.AreaLevelEnum;
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
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

import static cc.cannot.dict.persistence.entity.constants.DictTypeEnum.AREA;

@EqualsAndHashCode(callSuper = true)
@Entity(name = "area")
@Data
@Table(name = "dict_area")
@DynamicInsert
@DynamicUpdate
@ToString(callSuper = true)
public class AreaEntity extends BaseTreeEntity<Integer> {
    @JsonIgnore
    @JSONField(serialize = false)
    public static final int DEFAULT_BID = 0;

    @Column(name = "bid", unique = true, nullable = false)
    private Integer bid;

    @Column(name = "parent_bid")
    private Integer parentBid;

    @JsonIgnore
    @JSONField(serialize = false)
    public AreaLevelEnum getLevelEnum() {
        return AreaLevelEnum.values()[PrimeTypeUtils.shortValue(getLevel())];
    }

    @Column(name = "pinyin")
    private String pinyin;

    @Transient
    private List<Integer> brotherBids;

    public void setBrotherBids(List<Integer> ids) {
        brotherBids = ids;
        this.getAttr().put("brotherBids", brotherBids);
    }

    @JsonIgnore
    @JSONField(serialize = false)
    public List<Integer> getBrotherBids() {
        if (brotherBids == null) {
            brotherBids = new ArrayList<>();
            JSONArray ja = this.getAttr().getJSONArray("brotherBids");
            if (ja != null && !ja.isEmpty()) {
                ja.forEach(o -> brotherBids.add(Integer.valueOf(String.valueOf(o))));
            }
        }
        return brotherBids;
    }

    public AreaEntity() {
        super(AREA, DEFAULT_BID);
    }
}
