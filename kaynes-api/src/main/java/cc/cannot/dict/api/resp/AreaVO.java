package cc.cannot.dict.api.resp;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import cc.cannot.dict.api.constant.AreaLevelEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AreaVO extends BaseTreeVO {
    @ApiModelProperty(value = "地区名称的拼音")
    private String pinyin;

    @ApiModelProperty(value = "周边地区")
    private List<AreaVO> brothers;

    public JSONObject toFlatVO() {
        JSONObject ret = this.getInitFlatVO();
        ret.remove("brotherBids");
        ret.put("pinyin", pinyin);
        if (!CollectionUtils.isEmpty(brothers)) {
            JSONArray brothersArray = new JSONArray();
            for (AreaVO brother : brothers) {
                brothersArray.add(brother.toFlatVO());
            }
            ret.put("brothers", brothersArray);
        }
        return ret;
    }

    @Override
    public String getLevelName() {
        if (levelName == null && getLevelOrdinal() >= 0) {
            levelName = AreaLevelEnum.values()[getLevelOrdinal()].name();
        }
        return levelName;
    }
}
