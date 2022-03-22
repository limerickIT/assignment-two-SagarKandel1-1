/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sd4.controller;

import com.sd4.model.Beer;
import com.sd4.model.Brewery;
import com.sd4.model.Category;
import com.sd4.model.Style;
import com.sd4.service.BeerService;
import com.sd4.service.BreweryService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import static java.nio.file.Files.size;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author sagarkandel
 */
@RestController

public class BeerController {

    @Autowired
    private BeerService beerService;

    @Autowired
    private BreweryService breweryService;

    @GetMapping("/beers/GetAll")
    public List<Beer> getAll() {
        return beerService.findAll();
    }

    //get one by id
    @GetMapping(value = "/beers/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Beer> getOne(@PathVariable long id) {
        Optional<Beer> o = beerService.findOne(id);

        if (!o.isPresent()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } else {
            Link selfLink = Link.of("http://localhost:8888/beers/GetAll");
            o.get().add(selfLink);

            Link beerDetailLink = Link.of("http://localhost:8888/beers/GetBeerDrillDown/" + id);
            o.get().add(beerDetailLink);

            return ResponseEntity.ok(o.get());

        }

    }

    @GetMapping(value = "/beers/GetBeerDrillDown/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getBeerDrillDown(@PathVariable long id) {

        Optional<Beer> o = beerService.findOne(id);
        Optional<Brewery> b = breweryService.findOne(o.get().getBrewery_id());
        if (!o.isPresent()) {

            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } else {
            Map<String, Object> map = new HashMap<>();;
            map.put("Beer Name", o.get().getName());
            map.put("Beer Nescription", o.get().getDescription());
            map.put("Brewery Name", b.get().getName());
            return ResponseEntity.ok(map);
        }

    }

    @GetMapping(value = "/beers/count", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long getCount() {
        return beerService.count();
    }

    @DeleteMapping(value = "/beers/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity delete(@PathVariable long id) {
        beerService.deleteByID(id);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping(value = "/beers/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity add(@RequestBody Beer a) {
        beerService.saveBeer(a);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @PutMapping("/beers/")
    public ResponseEntity edit(@RequestBody Beer a) { //the edit method should check if the Beer object is already in the DB before attempting to save it.
        beerService.saveBeer(a);
        return new ResponseEntity(HttpStatus.OK);
    }

    //Return Images
    @GetMapping(value = "/beers/image/{size}/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<BufferedImage> getBeerImage(@PathVariable("id") long id, @PathVariable("size") String size) throws Exception {
        Optional<Beer> o = beerService.findOne(id);
        if (o.isEmpty()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        String path = "static/assets/images/"
                + ("thumbnail".equalsIgnoreCase(size) ? "thumbs" : "large")
                + "/" + o.get().getImage();
        System.out.println(path);
        final InputStream inputStream = new ClassPathResource(path).getInputStream();
        BufferedImage bufferedImage = ImageIO.read(inputStream);
        return ResponseEntity.ok(bufferedImage);
    }

//Download Images
 
}
