package com.cafe.democom.cafe.dao;

import com.cafe.democom.cafe.POJO.User;
import com.cafe.democom.cafe.wrapper.UserWrapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface UserDao extends JpaRepository<User, Integer> { //integer es el tipo del id

    User findByEmailId(@Param("email") String email);

    List<UserWrapper> getAllUser();

    List<String> getAllAdmin();

    @Transactional
    @Modifying
    Integer updateStatus(@Param("status") String status, @Param("id") Integer id);

    User findByEmail(String email); //automatically implemented by JPA because email is a column in POJO/User
}
