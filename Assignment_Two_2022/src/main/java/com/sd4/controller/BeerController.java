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
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
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
    @Autowired
    private StyleService styleService;
    @Autowired
    private CategoryService categoryService;

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
            map.put("Beer Description", o.get().getDescription());
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

    @PostMapping(value = "/beers/add", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity add(@RequestBody Beer a) {
        beerService.saveBeer(a);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @PutMapping("/beers/edit")
    public ResponseEntity edit(@RequestBody Beer a) { //the edit method should check if the Beer object is already in the DB before attempting to save it.
        beerService.saveBeer(a);
        return new ResponseEntity(HttpStatus.OK);
    }

    //Return Images
    @GetMapping(value = "/beers/image/{id}/{size}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<BufferedImage> getImagesOfBeer(@PathVariable("beerId") long beerId, @PathVariable("size") String size) throws Exception {
        Optional<Beer> o = beerService.findOne(beerId);
        if (o.isEmpty()) {
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
