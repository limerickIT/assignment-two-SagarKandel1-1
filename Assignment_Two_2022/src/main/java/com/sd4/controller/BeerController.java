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
import com.sd4.repository.StyleRepository;
import com.sd4.service.BeerService;
import com.sd4.service.BreweryService;
import com.sd4.service.CategoryService;
import com.sd4.service.StyleService;
import java.awt.image.BufferedImage;
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
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    @Autowired
    private StyleService styleService;
    @Autowired
    private CategoryService categoryService;

    @GetMapping(value = "beers/GetAll", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Beer> getAllBeers(@RequestParam(name = "fit", required = false) Integer fit, @RequestParam(name = "page", required = false) Integer page) {
        List<Beer> beersList = beerService.findAll();

        if (fit == null && page == null) {
            fit =  100;
            page = 0;
        }

        List<Beer> pagList = beersList.subList(page, page + fit);

        for (Beer o : pagList) {
            long id = o.getId();
            Link selfLink = linkTo(methodOn(BeerController.class).getOne(id)).withSelfRel();
            o.add(selfLink);
            
        }
        return pagList;
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
            map.put("Beer Description", o.get().getDescription());
            map.put("Brewery Name", b.get().getName());
            return ResponseEntity.ok(map);
        }

    }

    @GetMapping(value = "/beers/count", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long getCount() {
        return beerService.count();
    }

    @DeleteMapping(value = "/beers/delete/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity delete(@PathVariable long id) {
        beerService.deleteByID(id);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping(value = "/beers/add", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity add(@RequestBody Beer a) {
        beerService.saveBeer(a);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @PutMapping("/beers/edit/id")
    public ResponseEntity edit(@RequestBody Beer a) { beerService.saveBeer(a);
        return new ResponseEntity(HttpStatus.OK);
    }

    //Return Images
       
    @GetMapping(value = "/beers/image/{id}/{size}",produces = MediaType.IMAGE_JPEG_VALUE)
     public @ResponseBody byte[] getImagesOfBeer(@PathVariable long id, @PathVariable String size) throws IOException { 
    
            Optional<Beer> b = beerService.findOne(id);
    
        String value = "";   
        if (id < 5) {    
        if(size.contains("large")){
        value = "/static/assets/images/large/"+b.get().getImage();
        }else{
        value = "/static/assets/images/thumbs/"+b.get().getImage();
        }   
        } else {
            value = "/static/assets/images/large/noimage.jpg";
        }
        InputStream in = getClass().getResourceAsStream(value);
        return IOUtils.toByteArray(in);
    }

//Download Images
    @GetMapping(value = "/beers/GetImagesZipFile", produces = "application/zip")
    public void zipImageDownload(HttpServletResponse response) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());
        Resource resource = new ClassPathResource("static/assets/images/");
        InputStream input = resource.getInputStream();
        File fileToZip = resource.getFile();

        FileOutputStream fos = new FileOutputStream("CompressedBeerImages.zip");

        zipFile(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
        fos.close();
        response.setStatus(HttpServletResponse.SC_OK);
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"BeerImagesZip\"");
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
            File[] nextfile = fileToZip.listFiles();
            for (File newFile : nextfile) {
                zipFile(newFile, fileName + "/" + newFile.getName(), zipOut);
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
    @GetMapping(value = "/beers/pdf/{beerId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<PDFGenerator> getPdf(@PathVariable long beerId) throws Exception {

        Optional<Beer> o = beerService.findOne(beerId);
        if (o.isEmpty()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        final Beer b = o.get();

        Optional<Brewery> brewery = breweryService.findOne(b.getBrewery_id());
        Optional<Category> category = categoryService.findOne(b.getCat_id());
        Optional<Style> style = styleService.findOne(b.getStyle_id());

        PDFGenerator beerPdfPrinter = new PDFGenerator(b, brewery.get(), category.get(), style.get());

        final File pdfFile = beerPdfPrinter.generatePdfReport();
        try (final InputStream inputStream = new FileInputStream(pdfFile)) {

            final HttpHeaders responseHeaders = new HttpHeaders();
            final String filename = b.getName() + ".pdf";

            responseHeaders.set("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            return new ResponseEntity(IOUtils.toByteArray(inputStream), responseHeaders, HttpStatus.OK);
        }
    }


}
