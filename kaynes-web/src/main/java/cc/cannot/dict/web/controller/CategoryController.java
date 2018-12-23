package cc.cannot.dict.web.controller;

import cc.cannot.dict.api.req.CategoryUpdateReq;
import cc.cannot.dict.api.resp.CategoryVO;
import cc.cannot.dict.api.resp.JsonResp;
import cc.cannot.dict.business.service.CategoryService;
import cc.cannot.common.models.StringDict;
import feign.Headers;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/dict/category")
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping(value = "/{name}")
    @SuppressWarnings("unchecked")
    JsonResp<CategoryVO> getCategory(@PathVariable String name,
                                     @RequestParam(value = "parentDepth", defaultValue = "0") int parentDepth,
                                     @RequestParam(value = "childrenDepth", defaultValue = "0") int childrenDepth) {
        return JsonResp.success(categoryService.get(name, parentDepth, childrenDepth, false).toFlatVO());
    }

    @DeleteMapping(value = "/{name}")
    JsonResp deleteCategory(@PathVariable String name) {
        this.categoryService.delete(name);
        return JsonResp.success();
    }

    @PostMapping(value = "/{name}")
    @Headers(MediaType.APPLICATION_JSON_UTF8_VALUE)
    @SuppressWarnings("unchecked")
    @ApiOperation(
            notes = "修改类目的中文名称、层级、父级等，以及其他属性。" +
                    "<br><br>支持的参数列表请参考【Response Class】->【Model】",
            response = CategoryUpdateReq.class,
            value = "类目修改"
    )
    JsonResp<CategoryVO> updateCategory(@PathVariable String name, @RequestBody StringDict jsonObject) {
        jsonObject.put("name", name);
        CategoryUpdateReq req = this.makeUpdateReq(jsonObject);
        return JsonResp.success(categoryService.update(req).toFlatVO());
    }

    @PostMapping(value = "")
    @Headers(MediaType.APPLICATION_JSON_UTF8_VALUE)
    @SuppressWarnings("unchecked")
    @ApiOperation(
            notes = "新增类目。" +
                    "<br><br>支持的参数列表请参考【Response Class】->【Model】",
            response = CategoryUpdateReq.class,
            value = "类目新增"
    )
    JsonResp<CategoryVO> addCategory(@RequestBody StringDict jsonObject) {
        CategoryUpdateReq req = this.makeUpdateReq(jsonObject);
        return JsonResp.success(categoryService.insert(req).toFlatVO());
    }

    @PostMapping(value = "/resort")
    @Headers(MediaType.APPLICATION_JSON_UTF8_VALUE)
    JsonResp resort(@RequestBody String[] names) {
        categoryService.resort(names);
        return JsonResp.success();
    }

    private CategoryUpdateReq makeUpdateReq(StringDict jsonObject) {
        return new CategoryUpdateReq(jsonObject);
    }
}
