package cc.cannot.dict.persistence.entity;

import cc.cannot.common.utils.TimeUtils;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
public class BaseEntity implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "deleted_time")
    private Integer deletedTime;

    @Column(name = "created_time")
    private Integer createdTime;

    @Column(name = "modified_time")
    private Integer modifiedTime;

    @PrePersist
    protected void prePersist() {
        if (this.createdTime == null) {
            this.createdTime = TimeUtils.currentTimeSeconds();
        }

        if (this.modifiedTime == null) {
            this.modifiedTime = TimeUtils.currentTimeSeconds();
        }
    }

    @PreUpdate
    protected void preUpdate() {
        this.modifiedTime = TimeUtils.currentTimeSeconds();
    }

    @PreRemove
    protected void preRemove() {
        this.deletedTime = TimeUtils.currentTimeSeconds();
    }
}
