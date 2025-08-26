package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api(tags = "分类相关接口")
@RestController
@RequestMapping("/admin/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    @ApiOperation(value = "新增分类")
    public Result add(@RequestBody CategoryDTO categoryDTO) {
        log.info("新增分类：{}", categoryDTO);
        categoryService.add(categoryDTO);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation(value = "根据类型查询分类")
    public Result<List<Category>> getByType(Integer type) {
        log.info("根据类型查询分类：{}", type);
        List<Category> list = categoryService.getByType(type);
        return Result.success(list);
    }

    @DeleteMapping
    @ApiOperation(value = "根据id删除分类")
    public Result deleteById(Long id) {
        log.info("根据id删除分类：{}", id);
        categoryService.deleteById(id);
        return Result.success();
    }


    @PostMapping("/status/{status}")
    @ApiOperation(value = "启用禁用分类")
    public Result startOrStop(@PathVariable Integer status, @RequestParam Long id) {
        log.info("启用禁用分类：{}", status, id);
        categoryService.startOrStop(status, id);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation(value = "分类分页查询")
    public Result<PageResult> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        log.info("分类分页查询：{}", categoryPageQueryDTO);
        PageResult pageResult = categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    @PutMapping
    @ApiOperation(value = "修改分类")
    public Result update(@RequestBody CategoryDTO categoryDTO) {
        categoryService.update(categoryDTO);
        return Result.success();
    }

}
