package cc.cannot.dict.api.req;

import cc.cannot.dict.api.constant.AreaLevelEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.util.StringUtils;
import cc.cannot.common.models.StringDict;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class AreaUpdateReq extends BaseTreeUpdateReq {

    @ApiModelProperty(value = "地区的中文名称对应的拼音")
    private String pinyin;

    @Override
    public Short getLevel() {
        if (!StringUtils.isEmpty(getLevelName())) {
            setLevel((short) AreaLevelEnum.valueOf(getLevelName().toUpperCase()).ordinal());
        }
        return super.getLevel();
    }

    public AreaUpdateReq(StringDict dict) {
        super(dict);
        this.name = dict.getString("name");
        this.pinyin = dict.getString("pinyin");
        dict.removeAll("name", "pinyin");
        this.attr = dict;
    }
}
