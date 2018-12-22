package cc.cannot.dict.web.controller;


import cc.cannot.dict.api.req.CommunityUpdateReq;
import cc.cannot.dict.api.resp.CommunityVO;
import cc.cannot.dict.api.resp.JsonResp;
import cc.cannot.dict.business.service.CommunityService;
import cc.cannot.dict.common.models.StringDict;
import feign.Headers;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/dict/community")
@Slf4j
public class CommunityController {
    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping(value = "/{bid}")
    @SuppressWarnings("unchecked")
    JsonResp<CommunityVO> getCommunity(@PathVariable int bid) {
        return JsonResp.success(communityService.get(bid).toFlatVO());
    }

    @DeleteMapping(value = "/{bid}")
    JsonResp deleteCommunity(@PathVariable int bid) {
        communityService.delete(bid);
        return JsonResp.success();
    }

    @PostMapping(value = "/{bid}")
    @Headers(MediaType.APPLICATION_JSON_UTF8_VALUE)
    @SuppressWarnings("unchecked")
    @ApiOperation(
            notes = "修改小区的名称、地址等，以及其他属性。" +
                    "<br><br>支持的参数列表请参考【Response Class】->【Model】",
            response = CommunityUpdateReq.class,
            value = "小区修改"
    )
    JsonResp<CommunityVO> updateCommunity(@PathVariable int bid, @RequestBody StringDict req) {
        req.put("bid", bid);
        return JsonResp.success(communityService.update(new CommunityUpdateReq(req)).toFlatVO());
    }

    @PostMapping(value = "")
    @Headers(MediaType.APPLICATION_JSON_UTF8_VALUE)
    @SuppressWarnings("unchecked")
    @ApiOperation(
            notes = "新增小区。洗数据时请传入bid" +
                    "<br><br>支持的参数列表请参考【Response Class】->【Model】",
            response = CommunityUpdateReq.class,
            value = "小区新增"
    )
    JsonResp<CommunityVO> addCommunity(@RequestBody StringDict req) {
        return JsonResp.success(communityService.add(new CommunityUpdateReq(req)).toFlatVO());
    }
}
