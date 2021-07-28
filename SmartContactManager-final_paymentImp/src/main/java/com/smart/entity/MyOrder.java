package com.smart.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "TRANSACTION_TABLE")
public class MyOrder {

	@GeneratedValue(strategy = GenerationType.AUTO)
	@Id
	@Column(name = "TRANSACTION_ID")
	private Integer id;
	
	
	private String orderId;
	
	private String amount;
	
	private String receipt;
	
	private String status;
	
	//one user multiple trasaction so
	//user table it is OneToMany relation is there so it normal take ManyToOne
	
	@ManyToOne
	@JsonIgnore
	private User user;
	
	private String paymetId;

	//toString
	@Override
	public String toString() {
		return "MyOrder [id=" + id + ", orderId=" + orderId + ", amount=" + amount + ", receipt=" + receipt
				+ ", status=" + status + ", paymetId=" + paymetId + "]";
	}
	
	
	
	
}//class
