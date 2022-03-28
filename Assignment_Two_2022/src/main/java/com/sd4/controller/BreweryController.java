/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sd4.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.sd4.model.Beer;
import com.sd4.service.BreweryService;
import com.sd4.model.Brewery;
import com.sd4.controller.QRGenerator;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.property.Address;
import ezvcard.property.Email;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;
import java.awt.image.BufferedImage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author sagarkandel
 */
@RestController

public class BreweryController {
    
    @Autowired
    private BreweryService breweryService;
    
    
   
   
    
    @GetMapping(value ="/brewery/count", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long getCount() {
        return breweryService.count();
    }
    
    @DeleteMapping(value ="/brewery/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity delete(@PathVariable long id) {
        breweryService.deleteByID(id);
        return new ResponseEntity(HttpStatus.OK);
    }
    
    @PostMapping(value ="/brewery/add", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity add(@RequestBody Brewery a) {
        breweryService.saveBrewery(a);
        return new ResponseEntity(HttpStatus.CREATED);
    }
  @GetMapping(value = "/brewery/map/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public String getAddress(@PathVariable long id) {

        Optional<Brewery> o = breweryService.findOne(id);
        if (!o.isPresent()) {
            return "<h1>ERROR BREWERY NOT FOUND";
        } else {
        return null; 
        }
    }
     @GetMapping(value = "/brewery/qr/{id}", produces = MediaType.TEXT_HTML_VALUE)
 public ResponseEntity<BufferedImage> generateQRCodeImage(@PathVariable long id) throws Exception  {
    
        Optional<Brewery> o = breweryService.findOne(id);
        if (!o.isPresent()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } else {
            
            VCard vcard = new VCard();

            StructuredName n = new StructuredName();
            n.setGiven(o.get().getName());
            vcard.setStructuredName(n);

            Address vCardAddress = new Address();
            vCardAddress.setCountry(o.get().getCountry());
            vCardAddress.setRegion(o.get().getState());
            vCardAddress.setLocality(o.get().getCity());
            vCardAddress.setPostalCode(o.get().getCode());
            vCardAddress.setStreetAddress(o.get().getAddress1() + " " + o.get().getAddress2());
            vcard.addAddress(vCardAddress);

            Telephone tel = new Telephone(o.get().getPhone());
            vcard.addTelephoneNumber(tel);
            Email email = new Email(o.get().getEmail());
            vcard.addEmail(email);

            vcard.addUrl(o.get().getWebsite());
            String str = Ezvcard.write(vcard).version(VCardVersion.V4_0).go();

            return okResponse( QRGenerator.generateQRCodeImage(str));
        }
    }

    private ResponseEntity<BufferedImage> okResponse(BufferedImage image) {
        return new ResponseEntity<>(image, HttpStatus.OK);
    }

}
