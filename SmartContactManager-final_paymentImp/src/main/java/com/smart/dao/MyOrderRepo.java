package com.smart.dao;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smart.entity.MyOrder;

public interface MyOrderRepo extends JpaRepository<MyOrder, Serializable> {

	public MyOrder findByOrderId(String orderId);
}
