/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sd4.controller;

import com.sd4.model.Beer;

import com.itextpdf.text.Document;

import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.sd4.service.BeerService;
import com.sd4.service.BreweryService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author sagarkandel
 */
public class PDFGenerator {
    
     @Autowired
    private BeerService beerService;
 
    @Autowired
    private BreweryService breweryService;
      public static ByteArrayInputStream createPDF(Beer beer ) throws Exception {

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, out);
        document.open();
        Font titleFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 24);
        Font headingFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 18);
        Font bodyFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 14);
        
        document.add(new Paragraph("BROCHURE FOR BEER "+beer.getId(), titleFont));
        document.add(new Paragraph("BEER NAME",headingFont));
        document.add(new Paragraph(beer.getName(), bodyFont));
        
        document.add(new Paragraph("ABV",headingFont));
        document.add(new Paragraph(beer.getAbv().toString(), bodyFont));
        
        document.add(new Paragraph("DESCRIPTION",headingFont));
        if(!beer.getDescription().isBlank())
            document.add(new Paragraph(beer.getDescription(), bodyFont));
        else
            document.add(new Paragraph("No Beer Description available", bodyFont));
        
        Image image = Image.getInstance("src/main/resources/static/assets/images/large/" + beer.getImage());
        
        document.add(image);
        
        document.add(new Paragraph("SELL PRICE",headingFont));
        document.add(new Paragraph(beer.getSell_price().toString()));
        
        document.add(new Paragraph("BREWERY NAME",headingFont));
        document.add(new Paragraph(beer.getBrewery().getName(), bodyFont));
        
        document.add(new Paragraph("WEBSITE",headingFont));
        document.add(new Paragraph(beer.getBrewery().getWebsite(), bodyFont));
        
        document.add(new Paragraph("BEER CATEGORY",headingFont));
        document.add(new Paragraph(beer.getCategory().getCat_name(), bodyFont));
  
        document.add(new Paragraph("BEER STYLE",headingFont));
        document.add(new Paragraph(beer.getStyle().getStyle_name(), bodyFont));
        document.close();

        return new ByteArrayInputStream(out.toByteArray());
    } 
}
