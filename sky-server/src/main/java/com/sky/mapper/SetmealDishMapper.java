package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 根据菜品id查询套餐id
     *
     * @param ids
     * @return
     */
//    List<Long> getSetmealIdsByDishIds(List<Long> ids);

    /**
     * 根据菜品id查询套餐id
     *
     * @param id
     * @return
     */
    @Select("select setmeal_id from setmeal_dish where dish_id = #{id}")
    List<Long> getSetmealIdsByDishId(Long id);

    /**
     * 新增套餐菜品关系
     *
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id查询菜品
     *
     * @param id
     * @return
     */
    List<SetmealDish> getBySetmealId(Long id);

    /**
     * 根据套餐id删除菜品-套餐菜品关系
     *
     * @param id
     */
    void deleteBySetmealId(Long id);
}
