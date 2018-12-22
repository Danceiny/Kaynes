package cc.cannot.dict.persistence.entity;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import cc.cannot.dict.persistence.entity.interfaces.DynamicAttrInterface;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@Table(name = "dict_community")
@DynamicInsert
@DynamicUpdate
public class CommunityEntity extends BaseEntity implements DynamicAttrInterface {
    @JsonIgnore
    @JSONField(serialize = false)
    public static final int DEFAULT_BID = 0;

    @JSONField(serialize = false)
    @JsonIgnore
    public static final int MAX_DESC_LEN = 4096;

    @Column(name = "bid", unique = true)
    private int bid;

    @Column(name = "node_name")
    private String name;

    @Column(name = "pinyin")
    private String pinyin;

    @Column(name = "city_bid")
    private int cityBid;

    @Column(name = "town_bid")
    private int townBid;

    @Column(name = "description")
    private String desc;

    @Column(name = "address")
    private String address;

    @Transient
    private transient JSONObject attr = new JSONObject();

    @Transient
    private transient Boolean attrChanged = false;
    @Transient
    private transient Boolean attrLoaded = false;
}
