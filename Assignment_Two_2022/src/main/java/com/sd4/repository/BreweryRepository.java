/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sd4.repository;

import com.sd4.model.Brewery;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author sagarkandel
 */
public interface BreweryRepository extends CrudRepository<Brewery, Long>{
    
}
