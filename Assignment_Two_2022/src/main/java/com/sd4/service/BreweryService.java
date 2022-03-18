/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sd4.service;

 
import com.sd4.model.Brewery;
import com.sd4.repository.BreweryRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author sagarkandel
 */
@Service
public class BreweryService {
     @Autowired
    private BreweryRepository breweryRepo;
 
     public Optional<Brewery> findOne(Long id) {
        return breweryRepo.findById(id);
    }

    public List<Brewery> findAll() {
        return (List<Brewery>) breweryRepo.findAll();
    }

    public long count() {
        return breweryRepo.count();
    }

    public void deleteByID(long ID) {
        breweryRepo.deleteById(ID);
    }

    public void saveBrewery(Brewery b) {
        breweryRepo.save(b);
    }
}
