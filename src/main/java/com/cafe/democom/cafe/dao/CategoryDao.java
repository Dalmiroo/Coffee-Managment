package com.cafe.democom.cafe.dao;

import com.cafe.democom.cafe.POJO.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryDao extends JpaRepository<Category,Integer> {

    List<Category> getAllCategory();
}
