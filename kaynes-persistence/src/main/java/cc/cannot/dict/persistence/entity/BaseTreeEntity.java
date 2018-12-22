package cc.cannot.dict.persistence.entity;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import cc.cannot.dict.persistence.entity.constants.DictTypeEnum;
import cc.cannot.dict.persistence.entity.interfaces.BaseTreeInterface;
import cc.cannot.dict.persistence.entity.interfaces.DynamicAttrInterface;
import cc.cannot.dict.persistence.entity.interfaces.WeightInterface;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 一、继承本类需要做以下事情：
 * 1. 在子类的空构造方法中为type枚举赋值，为defaultBid赋值
 * 2. 酌情修改DictTypeEnum中的各个方法
 * 3. 修改树节点位置（修改level以及parentBid属性）时，须调用markOldTreeNode方法
 *
 * @param <T>
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseTreeEntity<T> extends BaseEntity implements
        BaseTreeInterface<T>, DynamicAttrInterface, WeightInterface {

    public BaseTreeEntity(DictTypeEnum type, T defaultBid) {
        this();
        this.type = type;
        this.defaultBid = defaultBid;
    }

    public BaseTreeEntity() {
        this.attr = new JSONObject();
    }

    @Column(name = "node_level", nullable = false)
    protected Short level;

    @Column(name = "node_name")
    private String name;

    @Column(name = "weight", nullable = false)
    protected Integer weight;

    @Transient
    protected JSONObject attr;

    /**
     * 标记attr是否需要更新到数据库
     */
    @Transient
    protected transient Boolean attrChanged = false;

    /**
     * 标记attr是否已从数据库加载
     */
    @Transient
    protected transient Boolean attrLoaded = false;

    @Transient
    @JsonIgnore
    @JSONField(serialize = false)
    protected transient List<BaseTreeEntity<T>> children = new ArrayList<>();

    /**
     * internal use
     */
    @Transient
    @JsonIgnore
    @JSONField(serialize = false)
    protected transient List<Object> cids = new ArrayList<>();


    @Transient
    @JsonIgnore
    @JSONField(serialize = false)
    protected transient List<BaseTreeEntity<T>> parentChain = new ArrayList<>();

    @JsonIgnore
    @JSONField(serialize = false)
    public BaseTreeEntity getParent() {
        if (parentChain.isEmpty()) {
            return null;
        }
        return parentChain.get(0);
    }

    /**
     * mapper for Entity.class
     */
    @JSONField(serialize = false)
    @Transient
    @JsonIgnore
    protected DictTypeEnum type;

    /**
     * T的零值
     */
    @Transient
    @JsonIgnore
    @JSONField(serialize = false)
    protected transient T defaultBid;

    /**
     * for TreeServiceImpl
     */
    @Transient
    @JsonIgnore
    @JSONField(serialize = false)
    protected transient Short oldLevel;

    private void markOldLevel() {
        oldLevel = level;
    }

    /**
     * for TreeServiceImpl
     */
    @Transient
    @JsonIgnore
    @JSONField(serialize = false)
    protected transient Object oldParentBid;

    private void markOldParentBid() {
        oldParentBid = getParentBid();
    }

    /**
     * 涉及到树节点位置改变（主要是level和parentBid字段）的时候，需要调用本方法
     */
    public void markOldTreeNode() {
        this.markOldLevel();
        this.markOldParentBid();
    }
}

