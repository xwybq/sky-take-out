package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private DishMapper dishMapper;

    @Override
    @Transactional
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmeal.getId());
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        // 1. 校验并修正分页参数
        int pageNum = setmealPageQueryDTO.getPage();
        int pageSize = setmealPageQueryDTO.getPageSize();

        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        setmealPageQueryDTO.setPage(pageNum);
        setmealPageQueryDTO.setPageSize(pageSize);

        // 2. 执行分页查询（不提前判断总条数，确保返回完整的分页结构）
        PageHelper.startPage(pageNum, pageSize);
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        System.out.println(page);
        // 3. 处理空数据特殊情况：当总条数为0时，强制返回页码1（避免前端页码异常）
        PageResult pageResult = new PageResult(page.getTotal(), page.getResult());
        System.out.println(page.getTotal());
        //如果当前页数据为空，且总条数为0，手动设置当前页码为1
        if (page.getResult().isEmpty()) {
            // 手动设置当前页码为1，避免前端显示"第2页"但无数据的矛盾状态
            PageHelper.startPage(1, pageSize);
            page = setmealMapper.pageQuery(setmealPageQueryDTO);
            pageResult = new PageResult(page.getTotal(), page.getResult());
        }
        return pageResult;
    }

    @Override
    @Transactional
    public SetmealVO getById(Long id) {
        SetmealVO setmealVO = setmealMapper.getSetmealById(id);
        setmealVO.setSetmealDishes(setmealDishMapper.getBySetmealId(id));
        return setmealVO;
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException(MessageConstant.DELETE_PARAM_IS_NULL);
        }
        // 1. 校验套餐是否存在
        ids.forEach(id -> {
            SetmealVO setmealVO = setmealMapper.getSetmealById(id);
            if (setmealVO.getStatus() == StatusConstant.DISABLE) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
            setmealMapper.deleteById(id);
            setmealDishMapper.deleteBySetmealId(id);
        });
    }

    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);
        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealDTO.getId());
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    @Override
    public void updateStatus(Integer status, Long id) {
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        if (status == StatusConstant.ENABLE) {
            //判断套餐下是否有菜品
            List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
            setmealDishes.forEach(setmealDish -> {
                if (dishMapper.getById(setmealDish.getDishId()).getStatus() == StatusConstant.DISABLE) {
                    log.info("套餐下有菜品为禁用状态");
                    throw new DeletionNotAllowedException(MessageConstant.SETMEAL_DISH_ON_DISABLE);
                }
            });
        }
        setmealMapper.update(setmeal);
    }

}
