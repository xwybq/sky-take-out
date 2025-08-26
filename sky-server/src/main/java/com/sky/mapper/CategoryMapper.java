package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Insert("insert into category(type,name,sort,status,create_time,update_time,create_user,update_user) " +
            "values(#{type},#{name},#{sort},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void insert(Category category);


    @Delete("delete from category where id = #{id}")
    void delete(Long id);


    /**
     * 更新分类
     *
     * @param category
     */
    void update(Category category);


    /**
     * 根据类型查询分类
     *
     * @param type
     * @return
     */
    List<Category> list(Integer type);

    /**
     * 分页查询
     *
     * @param categoryPageQueryDTO
     * @return
     */
    Page<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);
}
