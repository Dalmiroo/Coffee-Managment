package com.cafe.democom.cafe.service;

import com.cafe.democom.cafe.POJO.Bill;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface BillService {

    ResponseEntity<String> generateReport(Map<String,Object> requestMap);
    ResponseEntity<List<Bill>> getBills();

    ResponseEntity<byte[]> getPdf(Map<String,Object> requestMap);

    ResponseEntity<String> deleteBill(Integer id);
}
