package com.dcs.api.etax.service;

import com.dcs.api.etax.model.Invoice;
import com.dcs.api.etax.repository.InvoiceRepository;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class InvoiceService{

    @Autowired
    private InvoiceRepository invoiceRepository;

    public Optional<Invoice> retrieveInvoice(Long id) {
        return invoiceRepository.findById(id);
    }

    public List<Invoice> getAllInvoice() {
    	return invoiceRepository.findAll();
    }
    
	public Invoice createInvoice(Invoice invoice) {
		return invoiceRepository.save(invoice);
	}
	
	public Invoice updateInvoice(Invoice invoice) {
		return invoiceRepository.save(invoice);
	}
	
	public void deleteInvoice(Long id) {
		invoiceRepository.deleteById(id);
	}
	
	public void downloadInvoiceXML(Long id) throws IOException {
		Invoice invObj = invoiceRepository.findById(id).get();
		if(invObj != null) {
			// Generate XML from the Invoice object
	        try {
	        	 // Create a JAXB context for the Book class
		        JAXBContext context = JAXBContext.newInstance(Invoice.class);
	        	// Create a Marshaller
		        Marshaller marshaller = context.createMarshaller();
	        	// To format the XML output nicely
		        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		        ///*****
		        // Specify the file path and name
		        // String filePath = "path/to/your/file.xml";
		        String filePath =  "D:\\temp\\invoice" + id + ".xml";
		        // Create a FileWriter for the specified file
		        FileWriter writer = new FileWriter(filePath);
		        // Marshal the Invoice object to XML and write to the file
		        marshaller.marshal(invObj, writer);
		        // Close the FileWriter
		        writer.close();
		        // ********
		        
		        // Marshal the Book object to XML and print to console
				// marshaller.marshal(invObj, System.out);
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
