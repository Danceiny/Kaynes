package cc.cannot.dict.api.resp;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
public abstract class BaseTreeVO implements FlatVOInterface {
    @ApiModelProperty(value = "业务id")
    private Object bid;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(hidden = true)
    private Object pid;

    @JsonIgnore
    private Short levelOrdinal;

    @ApiModelProperty(value = "排序权重值")
    private Integer weight;

    @ApiModelProperty(value = "层级名称")
    String levelName;

    @ApiModelProperty(value = "其他属性，其中的元素将会被向上打平到VO中。")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private JSONObject attr = new JSONObject();

    @ApiModelProperty(value = "子节点数组（按照weight从小到大排序），可能多级嵌套。")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<BaseTreeVO> children;

    @ApiModelProperty(value = "父节点数组，按照层级从上到下排序，如徐汇区的parentChain可能为：[{中国}, {上海}]。" +
            "<br>该数组的长度等于parentDepth。" +
            "<br>当指定的parentDepth较小时（没有到根节点），比如parentDepth为1时，徐汇区的parentChain是[{上海}]")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<BaseTreeVO> parentChain;

    JSONObject getInitFlatVO() {
        JSONObject vo = getAttr();
        if (bid != null) {
            vo.put("bid", bid);
        }
        if (!StringUtils.isEmpty(name)) {
            vo.put("name", name);
        }
        vo.put("levelName", this.getLevelName());
        if (!CollectionUtils.isEmpty(this.getParentChain())) {
            JSONArray jsonArray = new JSONArray();
            for (BaseTreeVO parent : this.getParentChain()) {
                jsonArray.add((parent).toFlatVO());
            }
            vo.put("parentChain", jsonArray);
        }
        if (!CollectionUtils.isEmpty(this.getChildren())) {
            JSONArray jsonArray = new JSONArray();
            for (BaseTreeVO child : this.getChildren()) {
                jsonArray.add((child).toFlatVO());
            }
            vo.put("children", jsonArray);
        }
        vo.put("weight", weight);
        return vo;
    }
}
