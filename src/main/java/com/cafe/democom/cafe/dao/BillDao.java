package com.cafe.democom.cafe.dao;

import com.cafe.democom.cafe.POJO.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BillDao extends JpaRepository<Bill,Integer> {


    List<Bill> getAllBills();  //query

    List<Bill> getBillByUserName(@Param("username") String username); //query
}
