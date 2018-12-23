package cc.cannot.dict.api.req;

import cc.cannot.common.models.StringDict;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
public class BaseTreeUpdateReq {

    @ApiModelProperty(hidden = true, value = "业务id。对于修改接口，该参数将会被路由参数覆盖")
    protected Object bid;

    @ApiModelProperty(value = "父级的业务id", required = true)
    protected Object pid;

    @ApiModelProperty(value = "层级的枚举值，不推荐使用")
    protected Short level;

    @NotEmpty
    @ApiModelProperty(value = "名称，一般是中文", required = true)
    protected String name;

    @ApiModelProperty(value = "层级的名词，推荐使用。请参考：https://gitlab.baixing.cn/tidy/kaynes/tree/master/kaynes-web#scope-documentation 中的层级树，使用枚举项的字符串表示，如ROOT,BRAND,city等，不区分大小写。", required = true)
    protected String levelName;

    @ApiModelProperty(value = "其他属性，对应于json体中的“未知属性”，即客户端可以传入一个打平的json")
    protected StringDict attr;

    BaseTreeUpdateReq(StringDict dict) {
        // start level staff
        this.setLevelName(dict.getString("levelName"));
        if (levelName == null) {
            this.setLevel(dict.getShort("level"));
            if (level == null && bid != null) {
                throw new IllegalArgumentException("必须通过level或levelName指定层级");
            }
        }
        this.setName(dict.getString("name"));
        // end level staff
        this.setBid(dict.get("bid"));
        this.setPid(dict.get("pid"));
        dict.removeAll("levelName", "bid", "pid", "name");
    }
}
