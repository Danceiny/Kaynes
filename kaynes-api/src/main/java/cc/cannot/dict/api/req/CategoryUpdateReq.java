package cc.cannot.dict.api.req;

import cc.cannot.dict.api.constant.CategoryLevelEnum;
import cc.cannot.common.models.StringDict;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.util.StringUtils;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CategoryUpdateReq extends BaseTreeUpdateReq {

    public Short getLevel() {
        if (!StringUtils.isEmpty(this.levelName)) {
            this.level = (short) CategoryLevelEnum.valueOf(getLevelName().toUpperCase()).ordinal();
        }
        return this.level;
    }

    public void validateAdd() {
        if (StringUtils.isEmpty(this.name) || this.bid == null || this.pid == null) {
            throw new IllegalArgumentException("bid/pid/name不能为空");
        }
    }

    public CategoryUpdateReq(StringDict dict) {
        super(dict);
        this.attr = dict;
    }
}
