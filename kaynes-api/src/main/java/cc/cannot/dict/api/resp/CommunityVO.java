package cc.cannot.dict.api.resp;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CommunityVO implements FlatVOInterface {
    @ApiModelProperty(value = "业务id")
    int bid;
    @ApiModelProperty(value = "小区名称")
    String name;
    @ApiModelProperty(value = "小区简介")
    String desc;
    @ApiModelProperty(value = "小区地址")
    String address;
    @ApiModelProperty(value = "小区所在城市的业务id")
    Integer cityBid;
    @ApiModelProperty(value = "小区所在街道/乡镇的业务id")
    Integer townBid;

    @ApiModelProperty(value = "其他属性", notes = "其中的元素将会被向上打平到VO中")
    @JsonIgnore
    @JSONField(serialize = false)
    private JSONObject attr = new JSONObject();

    @Override
    public JSONObject toFlatVO() {
        JSONObject flatJo = attr;
        flatJo.put("bid", bid);
        flatJo.put("name", name);
        flatJo.put("desc", desc);
        flatJo.put("address", address);
        flatJo.put("cityBid", cityBid);
        flatJo.put("townBid", townBid);
        return flatJo;
    }
}
