package cc.cannot.dict.api.resp;

import com.alibaba.fastjson.JSONObject;
import cc.cannot.dict.api.constant.CarLevelEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CarVO extends BaseTreeVO {
    @Override
    public String getLevelName() {
        if (levelName == null && getLevelOrdinal() >= 0) {
            levelName = CarLevelEnum.values()[getLevelOrdinal()].name();
        }
        return levelName;
    }

    public JSONObject toFlatVO() {
        return this.getInitFlatVO();
    }
}
