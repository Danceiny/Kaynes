package cc.cannot.dict.api.resp;

import com.alibaba.fastjson.JSONObject;
import cc.cannot.dict.api.constant.CategoryLevelEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CategoryVO extends BaseTreeVO {
    @Override
    public JSONObject toFlatVO() {
        return this.getInitFlatVO();
    }

    @Override
    public String getLevelName() {
        if (levelName == null && getLevelOrdinal() >= 0) {
            levelName = CategoryLevelEnum.values()[getLevelOrdinal()].name();
        }
        return levelName;
    }

}
