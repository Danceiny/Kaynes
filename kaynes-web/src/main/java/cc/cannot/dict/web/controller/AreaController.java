package cc.cannot.dict.web.controller;

import cc.cannot.dict.api.req.AreaUpdateReq;
import cc.cannot.dict.api.resp.AreaVO;
import cc.cannot.dict.api.resp.JsonResp;
import cc.cannot.common.models.StringDict;
import cc.cannot.dict.business.service.AreaService;
import feign.Headers;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/dict/area")
@Slf4j
public class AreaController {
    private final AreaService areaService;

    public AreaController(AreaService areaService) {
        this.areaService = areaService;
    }

    @GetMapping(value = "/{bid}")
    @SuppressWarnings("unchecked")
    @ApiOperation(
            notes = "支持通过childrenDepth，parentDepth，指定向上查询“祖宗十八代”，向下查询整个“子子孙孙”。" +
                    "<br>这两个参数的默认值是0。设为负整数时，表示动态的最大深度。" +
                    "<br><br>支持通过将loadBrother参数设为true，查询周边地区，实现`周边城市`的功能。" +
                    "<br>loadBrother只作用于bid所指的地区本身，其父子节点不会执行loadBrother操作。" +
                    "<br>",
            response = AreaVO.class,
            value = "地区查询"

    )
    JsonResp<AreaVO> getArea(@PathVariable int bid,
                             @RequestParam(value = "parentDepth", defaultValue = "0") int parentDepth,
                             @RequestParam(value = "childrenDepth", defaultValue = "0") int childrenDepth,
                             @RequestParam(value = "loadBrother", defaultValue = "false") boolean loadBrother) {
        return JsonResp.success(areaService.get(bid, parentDepth, childrenDepth, loadBrother).toFlatVO());
    }

    @DeleteMapping(value = "/{bid}")
    @ApiOperation(
            notes = "删除/隐藏。将会递归删除该地区下面的所有地区。",
            value = "地区删除"
    )
    JsonResp deleteArea(@PathVariable int bid) {
        areaService.delete(bid);
        return JsonResp.success();
    }

    @PostMapping(value = "/{bid}")
    @Headers(MediaType.APPLICATION_JSON_UTF8_VALUE)
    @SuppressWarnings("unchecked")
    @ApiOperation(
            notes = "修改地区的名称、层级（县改市等）、父级（行政区划调整等）等，以及其他属性。" +
                    "<br><br>支持的参数列表请参考【Response Class】->【Model】",
            response = AreaUpdateReq.class,
            value = "地区修改"
    )
    JsonResp<AreaVO> updateArea(@PathVariable int bid, @RequestBody StringDict req) {
        req.put("bid", bid);
        return JsonResp.success(areaService.update(new AreaUpdateReq(req)).toFlatVO());
    }

    @PostMapping(value = "")
    @Headers(MediaType.APPLICATION_JSON_UTF8_VALUE)
    @SuppressWarnings("unchecked")
    @ApiOperation(
            notes = "新增地区。洗数据时请传入bid。" +
                    "<br><br>支持的参数列表请参考【Response Class】->【Model】",
            response = AreaUpdateReq.class,
            value = "地区新增"
    )
    JsonResp<AreaVO> addArea(@RequestBody StringDict req) {
        return JsonResp.success(areaService.add(new AreaUpdateReq(req)).toFlatVO());
    }

    @PostMapping(value = "/resort")
    @Headers(MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(
            notes = "对同属某一父级的地区进行排序。" +
                    "<br><br>对于局部排序（即还有部分兄弟姐妹没有指定顺序），则仅保证相对排序是有效的，不保证中间不会插入其他值。" +
                    "<br>比如指定排序是[徐汇,闵行]，即将徐汇排到闵行之前，则不保证松江不会插入到徐汇之后、闵行之前。",
            value = "地区排序"
    )
    JsonResp resortAreas(@ApiParam(value = "业务id数组的顺序，即为排序的结果") @RequestBody Integer[] bids) {
        areaService.resort(bids);
        return JsonResp.success();
    }
}
