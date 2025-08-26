package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Employee;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void add(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setStatus(0);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.insert(category);
    }

    @Override
    public List<Category> getByType(Integer type) {

        return categoryMapper.list(type);
    }

    @Override
    public void deleteById(Long id) {
        if (dishMapper.selectByCategoryId(id) > 0) {
            throw new IllegalArgumentException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }
        if (setmealMapper.selectByCategoryId(id) > 0) {
            throw new IllegalArgumentException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }
        categoryMapper.delete(id);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Category category = new Category().builder()
                .status(status)
                .id(id)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        categoryMapper.update(category);
    }

    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        // 1. 校验并修正分页参数
        int pageNum = categoryPageQueryDTO.getPage();
        int pageSize = categoryPageQueryDTO.getPageSize();

        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        categoryPageQueryDTO.setPage(pageNum);
        categoryPageQueryDTO.setPageSize(pageSize);

        // 2. 执行分页查询（不提前判断总条数，确保返回完整的分页结构）
        PageHelper.startPage(pageNum, pageSize);
        Page<Category> page = (Page<Category>) categoryMapper.pageQuery(categoryPageQueryDTO);

        // 3. 处理空数据特殊情况：当总条数为0时，强制返回页码1（避免前端页码异常）
        PageResult pageResult = new PageResult(page.getTotal(), page.getResult());
        System.out.println(page.getTotal());
        //如果当前页数据为空，且总条数为0，手动设置当前页码为1
        if (page.getResult().isEmpty()) {
            // 手动设置当前页码为1，避免前端显示"第2页"但无数据的矛盾状态
            PageHelper.startPage(1, pageSize);
            page = categoryMapper.pageQuery(categoryPageQueryDTO);
            pageResult = new PageResult(page.getTotal(), page.getResult());
        }
        return pageResult;
    }

    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.update(category);
    }


}
