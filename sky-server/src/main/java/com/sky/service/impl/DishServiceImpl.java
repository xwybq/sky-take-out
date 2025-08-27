package com.sky.service.impl;


import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.pagehelper.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        // 新增菜品
        dishMapper.insert(dish);
        // 新增菜品口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(flavor -> {
                flavor.setDishId(dish.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        // 1. 校验并修正分页参数
        int pageNum = dishPageQueryDTO.getPage();
        int pageSize = dishPageQueryDTO.getPageSize();

        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        dishPageQueryDTO.setPage(pageNum);
        dishPageQueryDTO.setPageSize(pageSize);

        // 2. 执行分页查询（不提前判断总条数，确保返回完整的分页结构）
        PageHelper.startPage(pageNum, pageSize);
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        System.out.println(page);
        // 3. 处理空数据特殊情况：当总条数为0时，强制返回页码1（避免前端页码异常）
        PageResult pageResult = new PageResult(page.getTotal(), page.getResult());
        System.out.println(page.getTotal());
        //如果当前页数据为空，且总条数为0，手动设置当前页码为1
        if (page.getResult().isEmpty()) {
            // 手动设置当前页码为1，避免前端显示"第2页"但无数据的矛盾状态
            PageHelper.startPage(1, pageSize);
            page = dishMapper.pageQuery(dishPageQueryDTO);
            pageResult = new PageResult(page.getTotal(), page.getResult());
        }
        return pageResult;
    }

    /**
     * 批量删除菜品
     *
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        ids.forEach(id -> {
            DishVO dishVO = dishMapper.getById(id);
            if (dishVO.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishId(id);
            if (!setmealIds.isEmpty()) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
            // 删除菜品
            dishMapper.deleteById(id);
            // 删除菜品口味
            dishFlavorMapper.deleteByDishId(id);
        });
    }

    /**
     * 根据id查询菜品
     *
     * @param id
     * @return
     */
    @Override
    @Transactional
    public DishVO getById(Long id) {
        DishVO dishVO = dishMapper.getById(id);
        dishVO.setFlavors(dishFlavorMapper.getByDishId(id));
        return dishVO;
    }

    /**
     * 修改菜品
     *
     * @param dishDTO
     */
    @Override
    @Transactional
    public void update(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        // 修改菜品
        dishMapper.update(dish);
        // 修改菜品口味
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(flavor -> {
                flavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 修改菜品状态
     *
     * @param status
     * @param id
     */
    @Override
    @Transactional
    public void updateStatus(Integer status, Long id) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        dishMapper.update(dish);
        if (status == StatusConstant.DISABLE) {
            //查询菜品关联的套餐
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishId(id);
            log.info("关联的套餐为：" + setmealIds.toString());
            //停售套餐
            if (!setmealIds.isEmpty()) {
                setmealIds.forEach(setmealId -> {
                    Setmeal setmeal = Setmeal.builder()
                            .id(setmealId)
                            .status(status)
                            .build();
                    setmealMapper.update(setmeal);
                });
            }
        }
    }

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> list(Long categoryId) {
        return dishMapper.list(categoryId);
    }
}
