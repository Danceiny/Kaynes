package cc.cannot.dict.web.controller;

import cc.cannot.dict.api.req.CarUpdateReq;
import cc.cannot.dict.api.resp.CarVO;
import cc.cannot.dict.api.resp.JsonResp;
import cc.cannot.dict.business.service.CarService;
import cc.cannot.dict.common.models.StringDict;
import feign.Headers;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/dict/car")
@Slf4j
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping(value = "/{bid}")
    JsonResp getCar(@PathVariable int bid,
                    @RequestParam(value = "parentDepth", defaultValue = "0") int parentDepth,
                    @RequestParam(value = "childrenDepth", defaultValue = "0") int childrenDepth) {
        return JsonResp.success((carService.get(bid, parentDepth, childrenDepth, false).toFlatVO()));
    }

    @DeleteMapping(value = "/{bid}")
    JsonResp deleteCar(@PathVariable int bid) {
        carService.delete(bid);
        return JsonResp.success();
    }

    @PostMapping(value = "/{bid}")
    @Headers(MediaType.APPLICATION_JSON_UTF8_VALUE)
    @SuppressWarnings("unchecked")
    @ApiOperation(
            notes = "新增车型。洗数据时请传入bid。" +
                    "<br><br>支持的参数列表请参考【Response Class】->【Model】",
            response = CarUpdateReq.class,
            value = "车型库修改"
    )
    JsonResp<CarVO> updateCar(@PathVariable int bid, @RequestBody StringDict reqMap) {
        reqMap.put("bid", bid);
        return JsonResp.success(carService.update(new CarUpdateReq(reqMap)).toFlatVO());
    }

    @PostMapping(value = "")
    @Headers(MediaType.APPLICATION_JSON_UTF8_VALUE)
    @SuppressWarnings("unchecked")
    @ApiOperation(
            notes = "修改车的名称、层级、父级等，以及其他属性。" +
                    "<br><br>支持的参数列表请参考【Response Class】->【Model】",
            response = CarUpdateReq.class,
            value = "车修改"
    )
    JsonResp<CarVO> addCar(@RequestBody StringDict reqMap) {
        return JsonResp.success(carService.add(new CarUpdateReq(reqMap)).toFlatVO());
    }

    @PostMapping(value = "/resort")
    @Headers(MediaType.APPLICATION_JSON_UTF8_VALUE)
    JsonResp resortAreas(@RequestBody Integer[] bids) {
        carService.resort(bids);
        return JsonResp.success();
    }
}
