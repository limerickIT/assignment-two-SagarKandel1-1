/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sd4.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.sd4.service.BreweryService;
import com.sd4.model.Brewery;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author sagarkandel
 */
@RestController

public class BreweryController {

    @Autowired
    private BreweryService breweryService;

    @GetMapping(value = "/brewery/count", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long getCount() {
        return breweryService.count();
    }

    @DeleteMapping(value = "/brewery/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity delete(@PathVariable long id) {
        breweryService.deleteByID(id);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping(value = "/brewery/add", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity add(@RequestBody Brewery a) {
        breweryService.saveBrewery(a);
        return new ResponseEntity(HttpStatus.CREATED);
    }
    //Getting Brewery MapLoactoion

    @GetMapping(value = "/brewery/map/{id}")
    public ResponseEntity getBreweryMap(@PathVariable long id) {

        Optional<Brewery> b = breweryService.findOne(id);

        if (!b.isPresent()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } else {
            String mapCoordinates = b.get().getName() + "" + b.get().getAddress1() + " "
                    + b.get().getAddress2() + " "
                    + b.get().getCity() + " " + b.get().getCity() + " "
                    + b.get().getCode() + " " + b.get().getCountry();

            return ResponseEntity.ok(
                    "<html><body>"
                    + "<h2>" + "[" + mapCoordinates + "]" + "</h2>"
                    + "<iframe width=\"1000\" height=\"500\" id=\"gmap_canvas\" src=\"https://maps.google.com/maps?width=100%25&amp;height=600&amp;hl=en&amp;q=" + URLEncoder.encode(mapCoordinates, StandardCharsets.UTF_8)
                    + "=&output=embed\"  frameborder=\"0\" scrolling=\"no\" </iframe>");
        }
    }

    //Getting Brewery QRCODE
    @GetMapping(value = "brewery/QRCode/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getCode(@PathVariable("id") long id) throws WriterException, IOException {

        Optional<Brewery> b = breweryService.findOne(id);

        String QRCODEDEATILS = "MECARD:N" + b.get().getName() + ";"
                + "ADR:" + b.get().getAddress1() + " " + b.get().getAddress2() + " " + b.get().getCity() + " " + b.get().getCode() + " " + b.get().getCountry() + ";"
                + "EMAIL:" + b.get().getEmail() + ";"
                + "TEL:" + b.get().getPhone() + ";" + "URL" + b.get().getWebsite() + ";";

        System.out.println(QRCODEDEATILS);

        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = barcodeWriter.encode(QRCODEDEATILS, BarcodeFormat.QR_CODE, 500, 500);

        BufferedImage code = MatrixToImageWriter.toBufferedImage(bitMatrix);
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        ImageIO.write(code, "jpeg", bytearrayoutputstream);
        bytearrayoutputstream.flush();
        byte[] codeInBytes = bytearrayoutputstream.toByteArray();
        bytearrayoutputstream.close();
        return codeInBytes;
    }

}
