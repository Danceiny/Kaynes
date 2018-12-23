package cc.cannot.dict.api.req;

import cc.cannot.dict.api.constant.CarLevelEnum;
import cc.cannot.common.models.StringDict;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.util.StringUtils;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CarUpdateReq extends BaseTreeUpdateReq {

    @ApiModelProperty(value = "车的中文名称对应的拼音")
    String pinyin;

    @Override
    public Short getLevel() {
        if (!StringUtils.isEmpty(super.getLevelName())) {
            setLevel((short) CarLevelEnum.valueOf(getLevelName().toUpperCase()).ordinal());
        }
        return super.getLevel();
    }

    public CarUpdateReq(StringDict dict) {
        super(dict);
        this.setName(dict.getString("name"));
        this.setPinyin(dict.getString("pinyin"));
        dict.removeAll("name", "pinyin");
        this.attr = dict;
    }
}
