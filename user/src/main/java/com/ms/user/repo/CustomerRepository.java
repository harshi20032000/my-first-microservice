/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ms.user.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ms.user.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, String> {

}