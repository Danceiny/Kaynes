package cc.cannot.dict.api;

import cc.cannot.dict.api.req.AreaUpdateReq;
import cc.cannot.dict.api.req.CarUpdateReq;
import cc.cannot.dict.api.req.CategoryUpdateReq;
import cc.cannot.dict.api.req.CommunityUpdateReq;
import cc.cannot.dict.api.resp.*;
import feign.Headers;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * @author huangzhen
 */
@RequestMapping({"api/dict"})
public interface DictServiceApi {
    /*** Start Category ***/
    @GetMapping(value = "/category/{name}")
    JsonResp<CategoryVO> getCategory(@PathVariable String name,
                                     @RequestParam(value = "parent", defaultValue = "false") boolean parent,
                                     @RequestParam(value = "children", defaultValue = "false") boolean children);

    @DeleteMapping(value = "/category/{name}")
    JsonResp deleteCategory(@PathVariable String name);

    @PostMapping(value = "/category/{name}")
    @Headers(MediaType.APPLICATION_JSON_UTF8_VALUE)
    JsonResp<CategoryVO> updateCategory(@RequestBody CategoryUpdateReq req);

    @PostMapping(value = "/category")
    @Headers(MediaType.APPLICATION_JSON_UTF8_VALUE)
    JsonResp<CategoryVO> addCategory(@RequestBody CategoryUpdateReq req);

    /*** End Category ***
     *** Start Area   ***/
    @GetMapping(value = "/area/{bid}")
    JsonResp<AreaVO> getArea(@PathVariable int bid,
                             @RequestParam(value = "parent", defaultValue = "false") boolean parent,
                             @RequestParam(value = "children", defaultValue = "false") boolean children);

    @DeleteMapping(value = "/area/{bid}")
    JsonResp deleteArea(@PathVariable int bid);

    @PostMapping(value = "/area/{bid}")
    @Headers(MediaType.APPLICATION_JSON_UTF8_VALUE)
    JsonResp<AreaVO> updateArea(@RequestBody AreaUpdateReq req);

    @PostMapping(value = "/area")
    @Headers(MediaType.APPLICATION_JSON_UTF8_VALUE)
    JsonResp<AreaVO> addArea(@RequestBody AreaUpdateReq req);

    /*** End Area        ***
     *** Start Community ***/
    @GetMapping(value = "/community/{bid}")
    JsonResp<CommunityVO> getCommunity(@PathVariable int bid);

    @DeleteMapping(value = "/community/{bid}")
    JsonResp deleteCommunity(@PathVariable int bid);

    @PostMapping(value = "/community/{bid}")
    @Headers(MediaType.APPLICATION_JSON_UTF8_VALUE)
    JsonResp<CommunityVO> updateCommunity(@RequestBody CommunityUpdateReq req);

    @PostMapping(value = "/community")
    @Headers(MediaType.APPLICATION_JSON_UTF8_VALUE)
    JsonResp<CommunityVO> addCommunity(@RequestBody CommunityUpdateReq req);

    /*** End Community ***
     *** Start Car     ***/
    @GetMapping(value = "/car/{bid}")
    JsonResp<CarVO> getCar(@PathVariable int bid,
                           @RequestParam(value = "parent", defaultValue = "false") boolean parent,
                           @RequestParam(value = "children", defaultValue = "false") boolean children);

    @DeleteMapping(value = "/car/{bid}")
    JsonResp deleteCar(@PathVariable int bid);

    @PostMapping(value = "/car/{bid}")
    @Headers(MediaType.APPLICATION_JSON_UTF8_VALUE)
    JsonResp<CarVO> updateCar(@RequestBody CarUpdateReq req);

    @PostMapping(value = "/car")
    @Headers(MediaType.APPLICATION_JSON_UTF8_VALUE)
    JsonResp<CarVO> addCar(@RequestBody CarUpdateReq req);
    /* End Car */
}
