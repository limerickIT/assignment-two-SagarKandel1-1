/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sd4.controller;

 
import com.sd4.controller.PDFGenerator;
import com.sd4.model.Beer;
import com.sd4.model.Brewery;
import com.sd4.model.Category;
import com.sd4.model.Style;
import com.sd4.service.BeerService;
import com.sd4.service.BreweryService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
 
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
 
 

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
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
    @GetMapping(value = "/beers/GetImage/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
public ResponseEntity<BufferedImage> getBeerImage(@PathVariable("id") long id, @PathVariable("size") String size) throws Exception
{
   Optional<Beer> o = beerService.findOne(id);
if (o.isEmpty())
{
return new ResponseEntity(HttpStatus.NOT_FOUND);
} String path = "static/assets/images/"+ ("thumbnail".equalsIgnoreCase(size) ? "thumbs" : "large")
+ "/" + o.get().getImage(); System.out.println(path);
final InputStream inputStream = new ClassPathResource(path).getInputStream(); BufferedImage bufferedImage = ImageIO.read(inputStream);
return ResponseEntity.ok(bufferedImage);
}

//Download Images
  @GetMapping(value = "/beers/GetImagesZipFile", produces = "application/zip")
    public void zipImageDownload(HttpServletResponse response) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());
        Resource resource = new ClassPathResource("static/assets/images/");
        InputStream input = resource.getInputStream();
        File fileToZip = resource.getFile();

        FileOutputStream fos = new FileOutputStream("Compressed.zip");
        

        zipFile(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
        fos.close();
        response.setStatus(HttpServletResponse.SC_OK);
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ImagesZip\"");
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }
    
    //GetPDF
    @GetMapping(value = "/beers/getBeerPDF", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> getBeerPDF(@PathVariable long id) throws Exception {

        Optional<Beer> o = beerService.findOne(id);
        if (!o.isPresent()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } else {
            Beer beer = o.get();
            ByteArrayInputStream bis = PDFGenerator.createPDF(beer);

            HttpHeaders headers = new HttpHeaders();
            String filename = beer.getName() + "_Beer.pdf";

            return ResponseEntity
                    .ok()
                    .body(new InputStreamResource(bis));
        }
    }
}
