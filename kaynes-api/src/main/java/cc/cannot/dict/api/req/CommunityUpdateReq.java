package cc.cannot.dict.api.req;

import com.alibaba.fastjson.JSONObject;
import cc.cannot.dict.common.models.StringDict;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CommunityUpdateReq {

    @ApiModelProperty(hidden = true, value = "业务id，修改时该字段将会被覆盖")
    Integer bid;

    @ApiModelProperty(value = "小区名称的拼音")
    String pinyin;

    @ApiModelProperty(value = "小区的中文名称", required = true)
    String name;

    @ApiModelProperty(value = "小区所在城市的业务id", required = true)
    Integer cityBid;

    @ApiModelProperty(value = "小区所在街道/乡镇的业务id")
    Integer townBid;

    @ApiModelProperty(value = "小区地址", required = true)
    String address;

    @ApiModelProperty(value = "小区简介", required = true)
    String desc;

    @ApiModelProperty(value = "其他属性，对应于json体中的“未知属性”，即客户端可以传入一个打平的json")
    JSONObject attr;

    public CommunityUpdateReq(StringDict dict) {
        this.bid = dict.getInteger("bid");
        this.pinyin = dict.getString("pinyin");
        this.name = dict.getString("name");
        this.cityBid = dict.getInteger("cityBid");
        this.townBid = dict.getInteger("townBid");
        this.address = dict.getString("address");
        this.desc = dict.getString("desc");
        dict.removeAll("bid", "pinyin", "name", "cityBid", "townBid", "address", "desc");
        this.attr = dict;
    }
}
