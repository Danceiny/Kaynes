package cc.cannot.dict.persistence.entity;

import cc.cannot.dict.persistence.entity.constants.DictTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "dict_category")
@DynamicInsert
@DynamicUpdate
@ToString(callSuper = true)
public class CategoryEntity extends BaseTreeEntity<String> {

    public CategoryEntity() {
        super(DictTypeEnum.CATEGORY, "");
    }

    @Column(name = "bid", unique = true, nullable = false)
    private String bid;

    @Column(name = "parent_bid")
    private String parentBid;
}

