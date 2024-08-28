package com.dcs.api.etax.controller;
import com.dcs.api.etax.model.Invoice;
import com.dcs.api.etax.service.InvoiceService;
import com.helger.schematron.ISchematronResource;
import com.helger.schematron.SchematronResourceHelper;
import com.helger.schematron.svrl.SVRLFailedAssert;
import com.helger.schematron.svrl.SVRLHelper;
import com.helger.schematron.svrl.jaxb.SchematronOutputType;
import com.helger.schematron.xslt.SchematronResourceSCH;

import xades4j.algorithms.EnvelopedSignatureTransform;
import xades4j.production.DataObjectReference;
import xades4j.production.SignedDataObjects;
import xades4j.production.XadesBesSigningProfile;
import xades4j.production.XadesSigner;
import xades4j.properties.DataObjectDesc;
import xades4j.properties.DataObjectFormatProperty;
import xades4j.providers.KeyingDataProvider;
import xades4j.providers.impl.FileSystemKeyStoreKeyingDataProvider;
import xades4j.providers.impl.KeyStoreKeyingDataProvider.KeyStorePasswordProvider;
import xades4j.providers.impl.KeyStoreKeyingDataProvider.SigningCertificateSelector;
import xades4j.providers.impl.KeyStoreKeyingDataProvider.KeyEntryPasswordProvider;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.File;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;
// DOM imports for generating XML
import org.w3c.dom.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/api/v1")
public class InvoiceController {

	
    //private final Path fileStorageLocation = Paths.get("src/main/resources/signed-ETDA-invoice.xml").toAbsolutePath().normalize();
    private final Path fileStorageLocation;

    public InvoiceController() throws IOException {
	   // Load the resource from the classpath
	   try (InputStream resourceStream = getClass().getResourceAsStream("/signed-ETDA-invoice.xml")) {
	        if (resourceStream == null) {
		        throw new FileNotFoundException("signed-ETDA-invoice.xml file not found in classpath.");
	        }
	        // Write the resource content to a temporary file
	        Path tempFile = Files.createTempFile("signed-ETDA-invoice", ".xml");
	        Files.copy(resourceStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
	        fileStorageLocation = tempFile.toAbsolutePath().normalize();
	   }
    }

    public Path getFileStorageLocation() {
		return fileStorageLocation;
    }

	
    @Autowired
    private InvoiceService invoiceService;

    @GetMapping("/invoices")
    public List<Invoice> getAllInvoice() {
        return invoiceService.getAllInvoice();
    }

    @PostMapping("/invoices")
    public ResponseEntity<Invoice> createInvoice(@RequestBody Invoice invoice) {
        Invoice newInvoice = invoiceService.createInvoice(invoice);
        return ResponseEntity.ok(newInvoice);
    }
    
    @PutMapping("/invoices")
    public Invoice updateInvoice(@RequestBody Invoice invoice) {
    	return invoiceService.updateInvoice(invoice);
    }

    @DeleteMapping("/invoices/{id}")
	public void deleteInvoice(@PathVariable Long id) {
		invoiceService.deleteInvoice(id);
	}
    
    @GetMapping("/invoices/{id}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Long id) {
        Optional<Invoice> inv = invoiceService.retrieveInvoice(id);
        return inv.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
	
	 @GetMapping("/invoices/{id}/download")
	 public ResponseEntity<String> downloadXml(@PathVariable Long id) throws Exception {
		 	// Invoice invObj = invoiceService.retrieveInvoice(id).get();
		 	System.out.println("========= downloadXml() =========");
		 	Invoice invObj = null;
		 	Optional<Invoice> optionalInvObj = invoiceService.retrieveInvoice(id);
		 	if (optionalInvObj.isPresent()) {
		 	    invObj = optionalInvObj.get();
		 	} 
		 	
		 	StringWriter writer = new StringWriter();
		 	String xmlString = "";
		 	if(invObj != null) {
			   try {
				    // Create a new DocumentBuilder
			        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			        DocumentBuilder db = dbf.newDocumentBuilder();
			        Document document = db.newDocument();
			        
			        // ############# Start : Generating XML tag ################
			        // ============================================================
			        // ######################################################### //
			        //    Create root element : Invoice_CrossIndustryInvoice     //
			        // ######################################################### //
			       /*
			        Element root = document.createElementNS("urn:etda:uncefact:data:standard:TaxInvoice_CrossIndustryInvoice:2", "rsm:Invoice_CrossIndustryInvoice");
					root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ram", "urn:etda:uncefact:data:standard:TaxInvoice_ReusableAggregateBusinessInformationEntity:2");
					root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:rsm", "urn:etda:uncefact:data:standard:TaxInvoice_CrossIndustryInvoice:2");
					root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
					root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation", "urn:etda:uncefact:data:standard:TaxInvoice_CrossIndustryInvoice:2");
					*/
			        
					// Create the root element
			        Element root = document.createElementNS("urn:etda:uncefact:data:standard:TaxInvoice_CrossIndustryInvoice:2", "rsm:TaxInvoice_CrossIndustryInvoice");
			        // Add namespaces
			        root.setAttribute("xmlns:ram", "urn:etda:uncefact:data:standard:TaxInvoice_ReusableAggregateBusinessInformationEntity:2");
			        root.setAttribute("xmlns:rsm", "urn:etda:uncefact:data:standard:TaxInvoice_CrossIndustryInvoice:2");
			        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			        root.setAttribute("xsi:schemaLocation", "urn:etda:uncefact:data:standard:TaxInvoice_CrossIndustryInvoice:2");

					// ######################################################### //
			        //    Create sub-root element (1) : ExchangedDocumentContext //
			        // ######################################################### //
			        
			        Element exchangedDocumentContext = document.createElement("rsm:ExchangedDocumentContext");
			        // Create GuidelineSpecifiedDocumentContextParameter element under ExchangedDocumentContext
			        Element guidelineParameter = document.createElement("ram:GuidelineSpecifiedDocumentContextParameter");
			        exchangedDocumentContext.appendChild(guidelineParameter);
			        // Append sub-root to root
			        root.appendChild(exchangedDocumentContext);
			        // Create ID element under GuidelineSpecifiedDocumentContextParameter
			        Element guideLineId = document.createElement("ram:ID");
			        guideLineId.appendChild(document.createTextNode("ER3-2560"));
			        guideLineId.setAttribute("schemeAgencyID", "ETDA");
			        guideLineId.setAttribute("schemeVersionID", "v2.0");
			        guidelineParameter.appendChild(guideLineId);
			        
			        // ################################################## //
			        // Create sub-root element (2) : ExchangedDocument    //
			        // ################################################## //
			        
			        Element exchangedDocument = document.createElement("rsm:ExchangedDocument");
			        // Create ID element under ExchangedDocument
			        Element exchangeID = document.createElement("ram:ID");
			        exchangeID.appendChild(document.createTextNode("INV01-2564"));
			        exchangedDocument.appendChild(exchangeID);
			        
			        // Create Name element under ExchangedDocument
			        Element exchangeName = document.createElement("ram:Name");
			        exchangeName.appendChild(document.createTextNode("ใบกำกับภาษี"));
			        exchangedDocument.appendChild(exchangeName);
			       
			        // Create TypeCode element under ExchangedDocument
			        Element exchangeTypeCode = document.createElement("ram:TypeCode");
			        exchangeTypeCode.appendChild(document.createTextNode("380"));
			        exchangedDocument.appendChild(exchangeTypeCode);
			        
			        // Create IssueDateTime element under ExchangedDocument
			        Element exchangeIssueDateTime = document.createElement("ram:IssueDateTime");
			        exchangeIssueDateTime.appendChild(document.createTextNode("2023-05-04T00:00:00.0"));
			        exchangedDocument.appendChild(exchangeIssueDateTime);
			        
			        // Create IssueDateTime element under ExchangedDocument
			        Element exchangeCreationDateTime = document.createElement("ram:CreationDateTime");
			        exchangeCreationDateTime.appendChild(document.createTextNode("2023-05-04T00:00:00.0"));
			        exchangedDocument.appendChild(exchangeCreationDateTime);
			        
				    root.appendChild(exchangedDocument);
				    
				    // ############################################################## //
			        // Create sub-root element (3) : SupplyChainTradeTransaction      //
			        // ############################################################## //
				    
			        Element supplyChainTradeTransaction = document.createElement("rsm:SupplyChainTradeTransaction");
			        
			        // Create ApplicableHeaderTradeAgreement element
			        Element applicableHeaderTradeAgreement = document.createElement("ram:ApplicableHeaderTradeAgreement");
			        supplyChainTradeTransaction.appendChild(applicableHeaderTradeAgreement);
			        
			        // ########################################### //
			        // >>> 1.  Create SellerTradeParty element <<< //
			        // ########################################### //
			        Element sellerTradeParty = document.createElement("ram:SellerTradeParty");
			        applicableHeaderTradeAgreement.appendChild(sellerTradeParty);
			        

			        // Create Name element under SellerTradeParty 
			        Element name = document.createElement("ram:Name");
			        name.appendChild(document.createTextNode(invObj.getSellerName())); // 
			        sellerTradeParty.appendChild(name);

			        /////////// ###### Seller - SpecifiedTaxRegistration ###### ///////////
			        // Create SpecifiedTaxRegistration element
			        Element taxRegistration = document.createElement("ram:SpecifiedTaxRegistration");
			        sellerTradeParty.appendChild(taxRegistration);
			        
			        // Tax ID element under SpecifiedTaxRegistration
			        Element sellerTaxID = document.createElement("ram:ID");
			        sellerTaxID.appendChild(document.createTextNode(invObj.getSellerTaxId() + "" + invObj.getSellerBranchId()));
			        sellerTaxID.setAttribute("schemeID", "TXID");
			        taxRegistration.appendChild(sellerTaxID);
			        
			        /////////// ###### Seller - DefinedTradeContact ###### ///////////
					// Create DefinedTradeContact element
					Element sellerTradeContact = document.createElement("ram:DefinedTradeContact");
					sellerTradeParty.appendChild(sellerTradeContact);
	
					///************************///
					// Create EmailURIUniversalCommunication element under DefinedTradeContact
					Element sellerEmailURI = document.createElement("ram:EmailURIUniversalCommunication");
					sellerTradeContact.appendChild(sellerEmailURI);
	
					// Create URIID element under EmailURIUniversalCommunication
					Element sellerUriId = document.createElement("ram:URIID");
					sellerUriId.appendChild(document.createTextNode(invObj.getSellerEmail()));
					sellerEmailURI.appendChild(sellerUriId);
					
					// Create TelephoneUniversalCommunication element under DefinedTradeContact
					Element sellerTelephone = document.createElement("ram:TelephoneUniversalCommunication");
					sellerTradeContact.appendChild(sellerTelephone);
					
					Element sellerCompleteNO = document.createElement("ram:CompleteNumber");
					Text phoneNumber = document.createTextNode("XXX-SELLER-XX"); // Replace with actual phone number
					sellerCompleteNO.appendChild(phoneNumber);
					sellerTelephone.appendChild(sellerCompleteNO);
					///************************///
					
				    /////////// ###### Seller - PostalTradeAddress ###### ///////////
			        // Create PostalTradeAddress element
				    Element sellerTradeAddress = document.createElement("ram:PostalTradeAddress");
				    sellerTradeParty.appendChild(sellerTradeAddress);
				    
				    // Create PostcodeCode element under PostalTradeAddress
				    Element sellerPostcode = document.createElement("ram:PostcodeCode");
				    sellerPostcode.appendChild(document.createTextNode(invObj.getSellerPostCode()));
				    sellerTradeAddress.appendChild(sellerPostcode);
	
				    // Create BuildingName element under PostalTradeAddress
				    Element sellerBuildingName = document.createElement("ram:LineOne");
				    sellerBuildingName.appendChild(document.createTextNode("เดอะมอลล์บางกะปิ"));
				    sellerTradeAddress.appendChild(sellerBuildingName);
	
				    // Create CityName element under PostalTradeAddress
				    Element sellerCityName = document.createElement("ram:CityName");
				    sellerCityName.appendChild(document.createTextNode("1006"));
				    sellerTradeAddress.appendChild(sellerCityName);
	
				    // Create CitySubDivisionName element under PostalTradeAddress
				    Element sellerCitySubDivisionName = document.createElement("ram:CitySubDivisionName");
				    sellerCitySubDivisionName.appendChild(document.createTextNode("100601"));
				    sellerTradeAddress.appendChild(sellerCitySubDivisionName);
	
				    // Create CountryID element under PostalTradeAddress
				    Element sellerCountryID = document.createElement("ram:CountryID");
				    sellerCountryID.appendChild(document.createTextNode("TH"));
				    sellerCountryID.setAttribute("schemeID", "3166-1 alpha-2");
				    sellerTradeAddress.appendChild(sellerCountryID);
				    
				    // Create CountrySubDivisionID element under PostalTradeAddress
				    Element sellerCountrySubDivisionID = document.createElement("ram:CountrySubDivisionID");
				    sellerCountrySubDivisionID.appendChild(document.createTextNode("86"));
				    sellerTradeAddress.appendChild(sellerCountrySubDivisionID);
				    
				    // Create BuildingNumber element under PostalTradeAddress
				    Element sellerBuildingNumber = document.createElement("ram:BuildingNumber");
				    sellerBuildingNumber.appendChild(document.createTextNode("7/99"));
				    sellerTradeAddress.appendChild(sellerBuildingNumber);
				    
				    // ########################################### //
			        // >>> 2.  Create BuyerTradeParty element <<<  //
				    // ########################################### //
				    
			        Element buyerTradeParty = document.createElement("ram:BuyerTradeParty");
			        applicableHeaderTradeAgreement.appendChild(buyerTradeParty);
			      
			        // Create Name element under BuyerTradeParty 
			        Element buyerName = document.createElement("ram:Name");
			        buyerName.appendChild(document.createTextNode(invObj.getBuyerName()));
			        buyerTradeParty.appendChild(buyerName);
			        
			        // Create SpecifiedTaxRegistration element under BuyerTradeParty
			        Element buyerTaxReg = document.createElement("ram:SpecifiedTaxRegistration");
			        buyerTradeParty.appendChild(buyerTaxReg);
			        
			        // ID element under SpecifiedTaxRegistration
			        Element buyerTaxID = document.createElement("ram:ID");
			        buyerTaxID.appendChild(document.createTextNode(invObj.getBuyerTaxId() + "" + invObj.getBuyerBranchId()));
			        buyerTaxID.setAttribute("schemeID", "TXID");
			        buyerTaxReg.appendChild(buyerTaxID);
			        
			        // ############ BuyerTradeParty - DefinedTradeContact ############
			        // Create DefinedTradeContact element
			        Element tradeContact = document.createElement("ram:DefinedTradeContact");
			        buyerTradeParty.appendChild(tradeContact);

			        // Create EmailURIUniversalCommunication element under DefinedTradeContact
			        Element emailURI = document.createElement("ram:EmailURIUniversalCommunication");
			        tradeContact.appendChild(emailURI);
			        // Create URIID element under EmailURIUniversalCommunication
			        Element uriId = document.createElement("ram:URIID");
			        uriId.appendChild(document.createTextNode(invObj.getBuyerEmail()));
			        emailURI.appendChild(uriId);

			        // Create TelephoneUniversalCommunication element under DefinedTradeContact
					Element buyerPhone = document.createElement("ram:TelephoneUniversalCommunication");
					tradeContact.appendChild(buyerPhone);
					// Create CompleteNumber element under TelephoneUniversalCommunication
					Element buyerCompleteNO = document.createElement("ram:CompleteNumber");
					buyerCompleteNO.appendChild(document.createTextNode(invObj.getBuyerPhoneNo()));
					buyerPhone.appendChild(sellerCompleteNO);
			        
			        // ############ BuyerTradeParty - PostalTradeAddress ############
				    // Create PostalTradeAddress element
				    Element postalTradeAddress = document.createElement("ram:PostalTradeAddress");
				    buyerTradeParty.appendChild(postalTradeAddress);
	
				    // Create PostcodeCode element under PostalTradeAddress
				    Element postcodeCode = document.createElement("ram:PostcodeCode");
				    postcodeCode.appendChild(document.createTextNode(invObj.getBuyerPostCode()));
				    postalTradeAddress.appendChild(postcodeCode);
	
				    // Create BuildingName element under PostalTradeAddress
				   //  Element buildingName = document.createElement("ram:BuildingName");
				    Element buildingName = document.createElement("ram:LineOne");
				    buildingName.appendChild(document.createTextNode("อาคารเบญจจินดา"));
				    postalTradeAddress.appendChild(buildingName);
	
				    // Create CityName element under PostalTradeAddress
				    Element cityName = document.createElement("ram:CityName");
				    cityName.appendChild(document.createTextNode("1030"));
				    postalTradeAddress.appendChild(cityName);
	
				    // Create CitySubDivisionName element under PostalTradeAddress
				    Element citySubDivisionName = document.createElement("ram:CitySubDivisionName");
				    citySubDivisionName.appendChild(document.createTextNode("103001"));
				    postalTradeAddress.appendChild(citySubDivisionName);
	
				    // Create CountryID element under PostalTradeAddress
				    Element countryID = document.createElement("ram:CountryID");
				    countryID.setAttribute("schemeID", "3166-1 alpha-2");
				    countryID.appendChild(document.createTextNode("TH"));
				    postalTradeAddress.appendChild(countryID);
				    
				    // Create CountrySubDivisionID element under PostalTradeAddress
				    Element countrySubDivisionID = document.createElement("ram:CountrySubDivisionID");
				    countrySubDivisionID.appendChild(document.createTextNode("10"));
				    postalTradeAddress.appendChild(countrySubDivisionID);
	
				    // Create BuildingNumber element under PostalTradeAddress
				    Element buildingNumber = document.createElement("ram:BuildingNumber");
				    buildingNumber.appendChild(document.createTextNode("40/20"));
				    postalTradeAddress.appendChild(buildingNumber);
				    
					// ####################################################### //
			        // >>> 3.  Create ApplicableHeaderTradeDelivery element << //
				    // ####################################################### //
					
					// Create ApplicableHeaderTradeDelivery element
					Element applicableHeaderTradeDelivery = document.createElement("ram:ApplicableHeaderTradeDelivery");
					supplyChainTradeTransaction.appendChild(applicableHeaderTradeDelivery);
					
					// Create ShipToTradeParty element under ApplicableHeaderTradeDelivery
					Element shipToTradeParty = document.createElement("ram:ShipToTradeParty");
					applicableHeaderTradeDelivery.appendChild(shipToTradeParty);
					
					// Create DefinedTradeContact element under ShipToTradeParty
					Element definedTradeContact = document.createElement("ram:DefinedTradeContact");
					shipToTradeParty.appendChild(definedTradeContact);
					// Create PersonName element under DefinedTradeContact
					Element deliveryPersonName = document.createElement("ram:PersonName");
					deliveryPersonName.appendChild(document.createTextNode(invObj.getDeliveryPerson()));
					definedTradeContact.appendChild(deliveryPersonName);

					/*
					// Create DepartmentName element under DefinedTradeContact
					Element deliveryDepartmentName = document.createElement("ram:DepartmentName");
					deliveryDepartmentName.appendChild(document.createTextNode("ฝ่ายขาย"));
					definedTradeContact.appendChild(deliveryDepartmentName);
					*/
					// ####################################################### //
			        // >>> 4.  Create ApplicableHeaderTradeSettlement element <<< //
				    // ####################################################### //
					
					// Create ApplicableHeaderTradeDelivery element
					Element headerTradeSettlement = document.createElement("ram:ApplicableHeaderTradeSettlement");
					supplyChainTradeTransaction.appendChild(headerTradeSettlement);
					
					// Create InvoiceCurrencyCode element under ApplicableHeaderTradeSettlement
					Element invCurrencyCode = document.createElement("ram:InvoiceCurrencyCode");
					invCurrencyCode.setAttribute("listID", "ISO 4217 3A");
					invCurrencyCode.appendChild(document.createTextNode("THB"));
					headerTradeSettlement.appendChild(invCurrencyCode);
					
					/// ###### Start : ApplicableTradeTax ###### ///
					// Create ApplicableTradeTax element under ApplicableHeaderTradeSettlement
					Element applicableTradeTax = document.createElement("ram:ApplicableTradeTax");
					headerTradeSettlement.appendChild(applicableTradeTax);
					// Create TypeCode element under ApplicableTradeTax
					Element typeCode = document.createElement("ram:TypeCode");
					typeCode.appendChild(document.createTextNode("VAT"));
					applicableTradeTax.appendChild(typeCode);
					// Create CalculatedRate element under ApplicableTradeTax
					Element calculatedRate = document.createElement("ram:CalculatedRate");
					calculatedRate.appendChild(document.createTextNode("7"));
					applicableTradeTax.appendChild(calculatedRate);
					// Create BasisAmount element under ApplicableTradeTax
					Element basisAmount = document.createElement("ram:BasisAmount");
					basisAmount.appendChild(document.createTextNode("1000"));
					applicableTradeTax.appendChild(basisAmount);
					// Create CalculatedAmount element under ApplicableTradeTax
					Element calculatedAmount = document.createElement("ram:CalculatedAmount");
					calculatedAmount.appendChild(document.createTextNode("699.93"));
					applicableTradeTax.appendChild(calculatedAmount);
					/// ###### Finish : ApplicableTradeTax ###### ///
					
					/// ###### Start : SpecifiedTradeSettlementHeaderMonetarySummation ###### ///
					// Create SpecifiedTradeSettlementHeaderMonetarySummation element under ApplicableHeaderTradeSettlement
					Element specifiedTradeSettlementHeaderMonetarySummation = document.createElement("ram:SpecifiedTradeSettlementHeaderMonetarySummation");
					headerTradeSettlement.appendChild(specifiedTradeSettlementHeaderMonetarySummation);

					// Create LineTotalAmount element under SpecifiedTradeSettlementHeaderMonetarySummation
					Element lineTotalAmount = document.createElement("ram:LineTotalAmount");
					lineTotalAmount.appendChild(document.createTextNode("1000.00"));
					specifiedTradeSettlementHeaderMonetarySummation.appendChild(lineTotalAmount);

					// Create AllowanceTotalAmount element under SpecifiedTradeSettlementHeaderMonetarySummation
					Element allowanceTotalAmount = document.createElement("ram:TaxBasisTotalAmount");
					allowanceTotalAmount.appendChild(document.createTextNode("10"));
					specifiedTradeSettlementHeaderMonetarySummation.appendChild(allowanceTotalAmount);

					// Create TaxTotalAmount element under SpecifiedTradeSettlementHeaderMonetarySummation
					Element taxTotalAmount = document.createElement("ram:TaxTotalAmount");
					taxTotalAmount.appendChild(document.createTextNode("70.00"));
					specifiedTradeSettlementHeaderMonetarySummation.appendChild(taxTotalAmount);

					// Create GrandTotalAmount element under SpecifiedTradeSettlementHeaderMonetarySummation
					Element grandTotalAmount = document.createElement("ram:GrandTotalAmount");
					grandTotalAmount.appendChild(document.createTextNode("1070.00"));
					specifiedTradeSettlementHeaderMonetarySummation.appendChild(grandTotalAmount);
					/// ###### Finish : SpecifiedTradeSettlementHeaderMonetarySummation ###### ///
					
					// ####################################################### //
			        // >>> 5.1  Create IncludedSupplyChainTradeLineItem element <<< //
				    // ####################################################### //
					
					// Create ApplicableHeaderTradeDelivery element
					Element includedSupplyChainTradeLineItem = document.createElement("ram:IncludedSupplyChainTradeLineItem");
					supplyChainTradeTransaction.appendChild(includedSupplyChainTradeLineItem);
					
					// Create AssociatedDocumentLineDocument element under IncludedSupplyChainTradeLineItem
					Element associatedDocumentLineDocument = document.createElement("ram:AssociatedDocumentLineDocument");
					includedSupplyChainTradeLineItem.appendChild(associatedDocumentLineDocument);
					// Create LineID element under AssociatedDocumentLineDocument
					Element lineID = document.createElement("ram:LineID");
					lineID.appendChild(document.createTextNode("1"));
					associatedDocumentLineDocument.appendChild(lineID);

					// Create SpecifiedTradeProduct element under IncludedSupplyChainTradeLineItem
					Element specifiedTradeProduct = document.createElement("ram:SpecifiedTradeProduct");
					includedSupplyChainTradeLineItem.appendChild(specifiedTradeProduct);
					// Create Name element under SpecifiedTradeProduct
					Element tradeProdName = document.createElement("ram:Name");
					tradeProdName.appendChild(document.createTextNode("สินค้าทดสอบ"));
					specifiedTradeProduct.appendChild(tradeProdName);
					
					
					// Create SpecifiedLineTradeAgreement element under IncludedSupplyChainTradeLineItem
					Element specifiedLineTradeAgreement = document.createElement("ram:SpecifiedLineTradeAgreement");
					includedSupplyChainTradeLineItem.appendChild(specifiedLineTradeAgreement);
					// Create Name element under SpecifiedTradeProduct
					Element grossPrice = document.createElement("ram:GrossPriceProductTradePrice");
					Element chargeAmount = document.createElement("ram:ChargeAmount");
					chargeAmount.appendChild(document.createTextNode("9999"));
					grossPrice.appendChild(chargeAmount);
					specifiedLineTradeAgreement.appendChild(grossPrice);
					
					// Create SpecifiedLineTradeDelivery element under IncludedSupplyChainTradeLineItem
					Element specifiedLineTradeDelivery = document.createElement("ram:SpecifiedLineTradeDelivery");
					includedSupplyChainTradeLineItem.appendChild(specifiedLineTradeDelivery);
					// Create Name element under SpecifiedTradeProduct
					Element billedQuantity = document.createElement("ram:BilledQuantity");
					billedQuantity.appendChild(document.createTextNode("1"));
					billedQuantity.setAttribute("unitCode", "AS");
					specifiedLineTradeDelivery.appendChild(billedQuantity);
					
					// Create SpecifiedLineTradeSettlement element under IncludedSupplyChainTradeLineItem
					Element specifiedLineTradeSettlement = document.createElement("ram:SpecifiedLineTradeSettlement");
					includedSupplyChainTradeLineItem.appendChild(specifiedLineTradeSettlement);
					// SpecifiedTradeSettlementLineMonetarySummation under SpecifiedLineTradeSettlement
					Element specifiedTradeLineMonetarySum = document.createElement("ram:SpecifiedTradeSettlementLineMonetarySummation");
					specifiedLineTradeSettlement.appendChild(specifiedTradeLineMonetarySum);
					// NetLineTotalAmount
					Element netLineTotalAmount = document.createElement("ram:NetLineTotalAmount");
					netLineTotalAmount.appendChild(document.createTextNode("9999"));
					specifiedTradeLineMonetarySum.appendChild(netLineTotalAmount);
					// NetIncludingTaxesLineTotalAmount
					Element netIncludingTaxesLineTotalAmount = document.createElement("ram:NetIncludingTaxesLineTotalAmount");
					netIncludingTaxesLineTotalAmount.appendChild(document.createTextNode("10698.93"));
					specifiedTradeLineMonetarySum.appendChild(netIncludingTaxesLineTotalAmount);
					
					// Append sub root, rsm:SupplyChainTradeTransaction, to root
					root.appendChild(supplyChainTradeTransaction); 
					document.appendChild(root); // Append root to document
					
					System.out.println("========= Create xml SchemaFactory and Schema =========");
					
					// Create SchemaFactory and Schema
				   /*
			            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			            Schema schema = schemaFactory.newSchema(new File("src/main/resources/TaxInvoice_CrossIndustryInvoice_2p0.xsd"));
			            // Create Validator
			            Validator validator = schema.newValidator();
			            DOMSource source = new DOMSource(document);
			            // Validate the XML Document
			            validator.validate(source);
			            System.out.println("XML is valid against the XSD.");
	       			*/

				// Load XSD Schema from classpath
			        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			        try (InputStream xsdStream = getClass().getResourceAsStream("/TaxInvoice_CrossIndustryInvoice_2p0.xsd")) {
			            if (xsdStream == null) {
			                throw new FileNotFoundException("XSD file not found in classpath.");
			            }
			            Schema schema = schemaFactory.newSchema(new StreamSource(xsdStream));
			            // Create Validator
			            Validator validator = schema.newValidator();
			            DOMSource source = new DOMSource(document);
			            // Validate the XML Document
			            validator.validate(source);
			            System.out.println("===== XML is valid against the XSD.=====");
			        }

				   
		            System.out.println("========= validing xml schematron =========");
					
		            // Load Schematron Schema from classpath
		            ISchematronResource aResSCH = SchematronResourceSCH.fromClassPath("/TaxInvoice_Schematron_2p0.sch");
		            if (!aResSCH.isValidSchematron()) {
		                throw new IllegalArgumentException("Invalid Schematron!");
		            }

			    System.out.println("========= validing xml document =========");
		            // Validate the XML document
		            SchematronOutputType aSOT = aResSCH.applySchematronValidationToSVRL(new DOMSource(document));

		            // Get all failed assertions
		            List<SVRLFailedAssert> aFAs = SVRLHelper.getAllFailedAssertions(aSOT);

		            // Handle validation errors
		            for (SVRLFailedAssert aFA : aFAs) {
		                System.out.println("Validation error: " + aFA.getText());
		            }

		            if (aFAs.isEmpty()) {
		                System.out.println("XML document is valid.");
		            }
		            
					// ############# Finish : Generating XML tag #############
					// ==================================================================
					// ############# Start : Implementing Digital Signature #############
					
					/*
					// Implement KeyStorePasswordProvider
					KeyStorePasswordProvider storePasswordProvider = new KeyStorePasswordProvider() {
						@Override
						public char[] getPassword() {
							return "password".toCharArray(); // Replace with your actual KeyStore password
						}
					};
					
					// Implement KeyEntryPasswordProvider
					KeyEntryPasswordProvider entryPasswordProvider =
							new KeyEntryPasswordProvider() {
								@Override
								public char[] getPassword(String alias, X509Certificate certificate) {
									return "password".toCharArray(); 
								}
							};
					
					// Load the KeyStore
					KeyStore keyStore = KeyStore.getInstance("PKCS12"); // Specify the KeyStore type
					
					// Load the KeyStore file
					File keyStoreFile = new File("src/main/resources/key.p12");
					
					// Define the signing key/certificate
					KeyingDataProvider keyProvider = FileSystemKeyStoreKeyingDataProvider.builder(keyStore.getType(),
							 keyStoreFile.getAbsolutePath(),
                             SigningCertificateSelector.single()).entryPassword(entryPasswordProvider).storePassword(storePasswordProvider).build();
					
					// Define the signed object
					DataObjectDesc obj = new DataObjectReference("")
					        .withTransform(new EnvelopedSignatureTransform())
					        .withDataObjectFormat(new DataObjectFormatProperty("text/xml"));
					
					// Create the signature
					XadesSigner signer = new XadesBesSigningProfile(keyProvider).newSigner();
					signer.sign(new SignedDataObjects(obj), document.getDocumentElement());
					*/
					// ==================================================================
					
					TransformerFactory tf = TransformerFactory.newInstance();
			        Transformer transformer = tf.newTransformer();
			        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			        // Output XML to StringWriter
			        transformer.transform(new DOMSource(document), new StreamResult(writer));
			        xmlString = writer.getBuffer().toString();


			        // ############# Finish : Transform Document to XML String ############
				  
			    } catch (ParserConfigurationException | TransformerException e) {
			    	e.printStackTrace();
			    } catch (DOMException ex) {
			       ex.printStackTrace();
			    }
			}
		 	
	        // Return XML as response
	        return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(xmlString);
		 	// return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(xmlString.replace("&#13;", ""));
	 }
	
	
	 @GetMapping("/invoices/{id}/export")
	 public ResponseEntity<Resource> export(@PathVariable Long id) {
		 System.out.println("========= export() =========");
	        try {
	        	
	            Path filePath = fileStorageLocation;
	            Resource resource = new UrlResource(filePath.toUri());
	            if (resource.exists()) {
	                return ResponseEntity.ok()
	                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
	                        .header(HttpHeaders.CONTENT_TYPE, "application/xml")
	                        .body(resource);
	            } else {
	                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	            }
	        } catch (MalformedURLException e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	        }
	 }
	 
	 
	 @GetMapping("/invoices/{id}/export1")
	 public String exportXml(@PathVariable Long id) throws Exception {
		 	// Invoice invObj = invoiceService.retrieveInvoice(id).get();
		 	System.out.println("========= exportXml() =========");
		 	Invoice invObj = null;
		 	Optional<Invoice> optionalInvObj = invoiceService.retrieveInvoice(id);
		 	if (optionalInvObj.isPresent()) {
		 	    invObj = optionalInvObj.get();
		 	} 
		 	
		 	StringWriter writer = new StringWriter();
		 	String xmlString = "";
		 	if(invObj != null) {
			   try {
				    // Create a new DocumentBuilder
			        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			        DocumentBuilder db = dbf.newDocumentBuilder();
			        Document document = db.newDocument();
			        
			        // ############# Start : Generating XML tag ################
			        // ============================================================
			        // ######################################################### //
			        //    Create root element : Invoice_CrossIndustryInvoice     //
			        // ######################################################### //
			        
			        //  Element root = document.createElement("rsm:Invoice_CrossIndustryInvoice");
					Element root = document.createElementNS("urn:etda:uncefact:data:standard:TaxInvoice_CrossIndustryInvoice:2", "rsm:Invoice_CrossIndustryInvoice");
					root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ram", "urn:etda:uncefact:data:standard:TaxInvoice_ReusableAggregateBusinessInformationEntity:2");
					root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:rsm", "urn:etda:uncefact:data:standard:TaxInvoice_CrossIndustryInvoice:2");
					root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
					root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation", "urn:etda:uncefact:data:standard:TaxInvoice_CrossIndustryInvoice:2");
					
					// ######################################################### //
			        //    Create sub-root element (1) : ExchangedDocumentContext //
			        // ######################################################### //
			        
			        Element exchangedDocumentContext = document.createElement("rsm:ExchangedDocumentContext");
			        // Create GuidelineSpecifiedDocumentContextParameter element under ExchangedDocumentContext
			        Element guidelineParameter = document.createElement("ram:GuidelineSpecifiedDocumentContextParameter");
			        exchangedDocumentContext.appendChild(guidelineParameter);
			        // Append sub-root to root
			        root.appendChild(exchangedDocumentContext);
				     
			        // Create ID element under GuidelineSpecifiedDocumentContextParameter
			        Element guideLineId = document.createElement("ram:ID");
			        guideLineId.appendChild(document.createTextNode("EDR3-2558"));
			        guideLineId.setAttribute("schemeName", "ETDA");
			        guideLineId.setAttribute("schemeVersionID", "v1-0");
			        guidelineParameter.appendChild(guideLineId);
			        
			        // ################################################## //
			        // Create sub-root element (2) : ExchangedDocument    //
			        // ################################################## //
			        
			        Element exchangedDocument = document.createElement("rsm:ExchangedDocument");
			        // Create ID element under ExchangedDocument
			        Element exchangeID = document.createElement("ram:ID");
			        exchangeID.appendChild(document.createTextNode("423-612"));
			        exchangedDocument.appendChild(exchangeID);
			        
			        // Create Name element under ExchangedDocument
			        Element exchangeName = document.createElement("ram:Name");
			        exchangeName.appendChild(document.createTextNode("ใบแจ้งหนี้"));
			        exchangedDocument.appendChild(exchangeName);
			       
			        // Create TypeCode element under ExchangedDocument
			        Element exchangeTypeCode = document.createElement("ram:TypeCode");
			        exchangeTypeCode.appendChild(document.createTextNode("380"));
			        exchangedDocument.appendChild(exchangeTypeCode);
			        
			        // Create IssueDateTime element under ExchangedDocument
			        Element exchangeIssueDateTime = document.createElement("ram:IssueDateTime");
			        exchangeIssueDateTime.appendChild(document.createTextNode("2006-05-04T00:00:00.0"));
			        exchangedDocument.appendChild(exchangeIssueDateTime);
			        
			        // Create Purpose element under ExchangedDocument
			        Element exchangePurpose = document.createElement("ram:Purpose");
			        exchangePurpose.appendChild(document.createTextNode("pZJSMeL"));
			        exchangedDocument.appendChild(exchangePurpose);
			        
			        // Create PurposeCode element under ExchangedDocument
			        Element exchangePurposeCode = document.createElement("ram:PurposeCode");
			        exchangePurposeCode.appendChild(document.createTextNode("42"));
			        exchangedDocument.appendChild(exchangePurposeCode);
			        
			        // Create GlobalID element under ExchangedDocument
			        Element globeID = document.createElement("ram:GlobalID");
			        globeID.setAttribute("schemeID", "OID");
			        globeID.setAttribute("schemeAgencyID", "ETDA");
			        globeID.appendChild(document.createTextNode("2.16.764.1.0.x"));
			        exchangedDocument.appendChild(globeID);
			        
				    // Create IncludedNote element under ExchangedDocument
				    Element includedNote = document.createElement("ram:IncludedNote");
				    exchangedDocument.appendChild(includedNote);
	
				    // Create Subject element under IncludedNote
				    Element subject = document.createElement("ram:Subject");
				    subject.appendChild(document.createTextNode("oM8G2oV7"));
				    includedNote.appendChild(subject);
	
				    // Create Content element under IncludedNote
				    Element content = document.createElement("ram:Content");
				    content.appendChild(document.createTextNode("A2BcyVpQ"));
				    includedNote.appendChild(content);
			        
				    // Append root, rsm:ExchangedDocument, to document 
				    root.appendChild(exchangedDocument);
				    
				    // ############################################################## //
			        // Create sub-root element (3) : SupplyChainTradeTransaction      //
			        // ############################################################## //
				    
			        Element supplyChainTradeTransaction = document.createElement("rsm:SupplyChainTradeTransaction");
			        
			        // Create ApplicableHeaderTradeAgreement element
			        Element applicableHeaderTradeAgreement = document.createElement("ram:ApplicableHeaderTradeAgreement");
			        supplyChainTradeTransaction.appendChild(applicableHeaderTradeAgreement);
			        
			        // ########################################### //
			        // >>> 1.  Create SellerTradeParty element <<< //
			        // ########################################### //
			        Element sellerTradeParty = document.createElement("ram:SellerTradeParty");
			        applicableHeaderTradeAgreement.appendChild(sellerTradeParty);
			        
			        // Create ID element under SellerTradeParty
			        Element elmId = document.createElement("ram:ID");
			        elmId.appendChild(document.createTextNode("T0019"));
			        sellerTradeParty.appendChild(elmId);
			        
			        // Create GlobalID element under SellerTradeParty
			        Element globalId = document.createElement("ram:GlobalID");
			        globalId.appendChild(document.createTextNode("8850001999993"));
			        globalId.setAttribute("schemeID", "GLN");
			        globalId.setAttribute("schemeAgencyID", "GS1");
			        sellerTradeParty.appendChild(globalId);

			        // Create Name element under SellerTradeParty 
			        Element name = document.createElement("ram:Name");
			        name.appendChild(document.createTextNode("Seller Name")); // 
			        sellerTradeParty.appendChild(name);

			        /////////// ###### Seller - SpecifiedTaxRegistration ###### ///////////
			        // Create SpecifiedTaxRegistration element
			        Element taxRegistration = document.createElement("ram:SpecifiedTaxRegistration");
			        sellerTradeParty.appendChild(taxRegistration);
			        
			        // Tax ID element under SpecifiedTaxRegistration
			        Element sellerTaxID = document.createElement("ram:ID");
			        sellerTaxID.setAttribute("schemeID", "TaxID");
			        sellerTaxID.setAttribute("schemeAgencyID", "RD");
			        // sellerTaxID.appendChild(document.createTextNode("333333333333300000")); //
			        sellerTaxID.appendChild(document.createTextNode(invObj.getSellerTaxId()));
			        taxRegistration.appendChild(sellerTaxID);
			        
			        /////////// ###### Seller - DefinedTradeContact ###### ///////////
					// Create DefinedTradeContact element
					Element sellerTradeContact = document.createElement("ram:DefinedTradeContact");
					sellerTradeParty.appendChild(sellerTradeContact);
	
					// Create EmailURIUniversalCommunication element under DefinedTradeContact
					Element sellerEmailURI = document.createElement("ram:EmailURIUniversalCommunication");
					sellerTradeContact.appendChild(sellerEmailURI);
	
					// Create URIID element under EmailURIUniversalCommunication
					Element sellerUriId = document.createElement("ram:URIID");
					sellerUriId.appendChild(document.createTextNode("seller@gmail.com"));
					sellerEmailURI.appendChild(sellerUriId);
					
				    /////////// ###### Seller - PostalTradeAddress ###### ///////////
			        // Create PostalTradeAddress element
				    Element sellerTradeAddress = document.createElement("ram:PostalTradeAddress");
				    sellerTradeParty.appendChild(sellerTradeAddress);
				    
				    // Create PostcodeCode element under PostalTradeAddress
				    Element sellerPostcode = document.createElement("ram:PostcodeCode");
				    sellerPostcode.appendChild(document.createTextNode("10240"));
				    sellerTradeAddress.appendChild(sellerPostcode);
	
				    // Create BuildingName element under PostalTradeAddress
				    Element sellerBuildingName = document.createElement("ram:BuildingName");
				    sellerBuildingName.appendChild(document.createTextNode("เดอะมอลล์บางกะปิ"));
				    sellerTradeAddress.appendChild(sellerBuildingName);
	
				    // Create StreetName element under PostalTradeAddress
				    Element sellerStreetName = document.createElement("ram:StreetName");
				    sellerStreetName.appendChild(document.createTextNode("ลาดพร้าว"));
				    sellerTradeAddress.appendChild(sellerStreetName);
	
				    // Create CityName element under PostalTradeAddress
				    Element sellerCityName = document.createElement("ram:CityName");
				    sellerCityName.appendChild(document.createTextNode("1006"));
				    sellerTradeAddress.appendChild(sellerCityName);
	
				    // Create CitySubDivisionName element under PostalTradeAddress
				    Element sellerCitySubDivisionName = document.createElement("ram:CitySubDivisionName");
				    sellerCitySubDivisionName.appendChild(document.createTextNode("100601"));
				    sellerTradeAddress.appendChild(sellerCitySubDivisionName);
	
				    // Create CountryID element under PostalTradeAddress
				    Element sellerCountryID = document.createElement("ram:CountryID");
				    sellerCountryID.appendChild(document.createTextNode("TH"));
				    sellerTradeAddress.appendChild(sellerCountryID);
				    
				    // Create CountrySubDivisionID element under PostalTradeAddress
				    Element sellerCountrySubDivisionID = document.createElement("ram:CountrySubDivisionID");
				    sellerCountrySubDivisionID.appendChild(document.createTextNode("10"));
				    sellerTradeAddress.appendChild(sellerCountrySubDivisionID);
				    
				    // Create BuildingNumber element under PostalTradeAddress
				    Element sellerBuildingNumber = document.createElement("ram:BuildingNumber");
				    sellerBuildingNumber.appendChild(document.createTextNode("99"));
				    sellerTradeAddress.appendChild(sellerBuildingNumber);
				    
				    // ########################################### //
			        // >>> 2.  Create BuyerTradeParty element <<<  //
				    // ########################################### //
				    
			        Element buyerTradeParty = document.createElement("ram:BuyerTradeParty");
			        applicableHeaderTradeAgreement.appendChild(buyerTradeParty);
			        
			        // Create ID element under BuyerTradeParty 
			        Element buyerId = document.createElement("ram:ID");
			        buyerId.appendChild(document.createTextNode("S000015QgoXpxSf"));
			        buyerTradeParty.appendChild(buyerId);
			        
			        // Create GlobalID element under BuyerTradeParty 
			        Element buyerGlobId = document.createElement("ram:GlobalID");
			        buyerGlobId.appendChild(document.createTextNode("S000015QgoXpxSf"));
			        buyerGlobId.setAttribute("schemeID", "GLN");
			        buyerGlobId.setAttribute("schemeAgencyID", "GS1");
			        buyerTradeParty.appendChild(buyerGlobId);
			        
			        // Create Name element under BuyerTradeParty 
			        Element buyerName = document.createElement("ram:Name");
			        buyerName.appendChild(document.createTextNode("Horizontal Company"));
			        buyerTradeParty.appendChild(buyerName);
			        
			        // Create SpecifiedTaxRegistration element under BuyerTradeParty
			        Element buyerTaxReg = document.createElement("ram:SpecifiedTaxRegistration");
			        buyerTradeParty.appendChild(buyerTaxReg);
			        
			        // ID element under SpecifiedTaxRegistration
			        Element buyerTaxID = document.createElement("ram:ID");
			        // buyerTaxID.appendChild(document.createTextNode("9999999999999"));
			        buyerTaxID.appendChild(document.createTextNode(invObj.getBuyerTaxId()));
			        buyerTaxID.setAttribute("schemeID", "TaxID");
			        buyerTaxID.setAttribute("schemeName", "Tax Registration Number");
			        buyerTaxID.setAttribute("schemeAgencyName", "RD");
			        
			        // Create DefinedTradeContact element
			        Element tradeContact = document.createElement("ram:DefinedTradeContact");
			        buyerTradeParty.appendChild(tradeContact);

			        // Create PersonName element under DefinedTradeContact
			        Element personName = document.createElement("ram:PersonName");
			        personName.appendChild(document.createTextNode("test"));
			        tradeContact.appendChild(personName);

			        // Create DepartmentName element under DefinedTradeContact
			       /* Element departmentName = document.createElement("ram:DepartmentName");
			        departmentName.appendChild(document.createTextNode("test"));
			        tradeContact.appendChild(departmentName);
			        */
			        // Create EmailURIUniversalCommunication element under DefinedTradeContact
			        Element emailURI = document.createElement("ram:EmailURIUniversalCommunication");
			        tradeContact.appendChild(emailURI);
			        // Create URIID element under EmailURIUniversalCommunication
			        Element uriId = document.createElement("ram:URIID");
			        uriId.appendChild(document.createTextNode("test@mail.com"));
			        emailURI.appendChild(uriId);

				    // Create PostalTradeAddress element
				    Element postalTradeAddress = document.createElement("ram:PostalTradeAddress");
				    buyerTradeParty.appendChild(postalTradeAddress);
	
				    // Create PostcodeCode element under PostalTradeAddress
				    Element postcodeCode = document.createElement("ram:PostcodeCode");
				    postcodeCode.appendChild(document.createTextNode("10400"));
				    postalTradeAddress.appendChild(postcodeCode);
	
				    // Create BuildingName element under PostalTradeAddress
				    Element buildingName = document.createElement("ram:BuildingName");
				    buildingName.appendChild(document.createTextNode("อาคารเบญจจินดา"));
				    postalTradeAddress.appendChild(buildingName);
	
				    // Create StreetName element under PostalTradeAddress
				    Element streetName = document.createElement("ram:StreetName");
				    streetName.appendChild(document.createTextNode("กำแพงเพชร 6"));
				    postalTradeAddress.appendChild(streetName);
	
				    // Create CityName element under PostalTradeAddress
				    Element cityName = document.createElement("ram:CityName");
				    cityName.appendChild(document.createTextNode("1030"));
				    postalTradeAddress.appendChild(cityName);
	
				    // Create CitySubDivisionName element under PostalTradeAddress
				    Element citySubDivisionName = document.createElement("ram:CitySubDivisionName");
				    citySubDivisionName.appendChild(document.createTextNode("103001"));
				    postalTradeAddress.appendChild(citySubDivisionName);
	
				    // Create CountryID element under PostalTradeAddress
				    Element countryID = document.createElement("ram:CountryID");
				    countryID.appendChild(document.createTextNode("TH"));
				    postalTradeAddress.appendChild(countryID);
	
				    // Create CountrySubDivisionID element under PostalTradeAddress
				    Element countrySubDivisionID = document.createElement("ram:CountrySubDivisionID");
				    countrySubDivisionID.appendChild(document.createTextNode("10"));
				    postalTradeAddress.appendChild(countrySubDivisionID);
	
				    // Create BuildingNumber element under PostalTradeAddress
				    Element buildingNumber = document.createElement("ram:BuildingNumber");
				    buildingNumber.appendChild(document.createTextNode("40/20"));
				    postalTradeAddress.appendChild(buildingNumber);
				    
				  
				    // ***************************** //
				    // Create ApplicableTradeDeliveryTerms element under ApplicableHeaderTradeAgreement
				    Element applicableTradeDeliveryTerms = document.createElement("ram:ApplicableTradeDeliveryTerms");
				    applicableHeaderTradeAgreement.appendChild(applicableTradeDeliveryTerms);
				    
				    // Create DeliveryTypeCode element under ApplicableTradeDeliveryTerms
				    Element deliveryTypeCode = document.createElement("ram:DeliveryTypeCode");
				    deliveryTypeCode.appendChild(document.createTextNode("FCA"));
				    applicableTradeDeliveryTerms.appendChild(deliveryTypeCode);

				    // ***************************** //
				    // Create BuyerOrderReferencedDocument element under ApplicableHeaderTradeAgreement
				    Element buyerOrderReferencedDocument = document.createElement("ram:BuyerOrderReferencedDocument");
				    applicableHeaderTradeAgreement.appendChild(buyerOrderReferencedDocument);

				    // Create IssuerAssignedID element under BuyerOrderReferencedDocument
				    Element issuerAssignedID = document.createElement("ram:IssuerAssignedID");
				    issuerAssignedID.appendChild(document.createTextNode("123456789012345678901234567890"));
				    buyerOrderReferencedDocument.appendChild(issuerAssignedID);

				    // Create IssueDateTime element under BuyerOrderReferencedDocument
				    Element issueDateTime = document.createElement("ram:IssueDateTime");
				    issueDateTime.appendChild(document.createTextNode("2017-01-29T00:00:00Z"));
				    buyerOrderReferencedDocument.appendChild(issueDateTime);

				    // Create ReferenceTypeCode element under BuyerOrderReferencedDocument
				    Element referenceTypeCode = document.createElement("ram:ReferenceTypeCode");
				    referenceTypeCode.appendChild(document.createTextNode("LAR"));
				    buyerOrderReferencedDocument.appendChild(referenceTypeCode);

				    // ***************************** //
				    // Create AdditionalReferencedDocument element under ApplicableHeaderTradeAgreement
				    Element additionalReferencedDocument = document.createElement("ram:AdditionalReferencedDocument");
				 	applicableHeaderTradeAgreement.appendChild(additionalReferencedDocument);

				 	// Create IssuerAssignedID element under AdditionalReferencedDocument
				 	Element issuerAssignedID2 = document.createElement("ram:IssuerAssignedID");
				 	issuerAssignedID2.appendChild(document.createTextNode("123456789012345678901234567890"));
				 	additionalReferencedDocument.appendChild(issuerAssignedID2);

					// Create IssueDateTime element under AdditionalReferencedDocument
					Element issueDateTime2 = document.createElement("ram:IssueDateTime");
					issueDateTime2.appendChild(document.createTextNode("2017-01-29T10:05:51Z"));
					additionalReferencedDocument.appendChild(issueDateTime2);
	
					// Create ReferenceTypeCode element under AdditionalReferencedDocument
					Element referenceTypeCode2 = document.createElement("ram:ReferenceTypeCode");
					referenceTypeCode2.appendChild(document.createTextNode("BA"));
					additionalReferencedDocument.appendChild(referenceTypeCode2);

					// ####################################################### //
			        // >>> 3.  Create ApplicableHeaderTradeDelivery element <<< //
				    // ####################################################### //
					
					// Create ApplicableHeaderTradeDelivery element
					Element applicableHeaderTradeDelivery = document.createElement("ram:ApplicableHeaderTradeDelivery");
					supplyChainTradeTransaction.appendChild(applicableHeaderTradeDelivery);
					
					// Create ShipToTradeParty element under ApplicableHeaderTradeDelivery
					Element shipToTradeParty = document.createElement("ram:ShipToTradeParty");
					applicableHeaderTradeDelivery.appendChild(shipToTradeParty);

					// Create ID element under ShipToTradeParty
					Element deliveryId = document.createElement("ram:ID");
					deliveryId.appendChild(document.createTextNode("T0019"));
					shipToTradeParty.appendChild(deliveryId);

					// Create GlobalID element under ShipToTradeParty
					Element globalID = document.createElement("ram:GlobalID");
					globalID.appendChild(document.createTextNode("8850001999993"));
					globalID.setAttribute("schemeID", "GLN");
					globalID.setAttribute("schemeAgencyID", "GS1");
					shipToTradeParty.appendChild(globalID);

					// Create Name element under ShipToTradeParty
					Element deliveryName = document.createElement("ram:Name");
					name.appendChild(document.createTextNode("บริษัท แนวตั้ง จำกัด"));
					shipToTradeParty.appendChild(deliveryName);

					// Create DefinedTradeContact element under ShipToTradeParty
					Element definedTradeContact = document.createElement("ram:DefinedTradeContact");
					shipToTradeParty.appendChild(definedTradeContact);

					// Create PersonName element under DefinedTradeContact
					Element deliveryPersonName = document.createElement("ram:PersonName");
					deliveryPersonName.appendChild(document.createTextNode("อติชาต ชัยแสง"));
					definedTradeContact.appendChild(deliveryPersonName);

					// Create DepartmentName element under DefinedTradeContact
					Element deliveryDepartmentName = document.createElement("ram:DepartmentName");
					deliveryDepartmentName.appendChild(document.createTextNode("ฝ่ายขาย"));
					definedTradeContact.appendChild(deliveryDepartmentName);

					// Create TelephoneUniversalCommunication element under DefinedTradeContact
					Element telephoneUniversalCommunication = document.createElement("ram:TelephoneUniversalCommunication");
					definedTradeContact.appendChild(telephoneUniversalCommunication);

					// Create CompleteNumber element under TelephoneUniversalCommunication
					Element completeNumber = document.createElement("ram:CompleteNumber");
					completeNumber.appendChild(document.createTextNode("+66-123-1234"));
					telephoneUniversalCommunication.appendChild(completeNumber);

					// Create PostalTradeAddress element under ShipToTradeParty
					Element shipToPostalAddr = document.createElement("ram:PostalTradeAddress");
					shipToTradeParty.appendChild(shipToPostalAddr);

					// Create PostcodeCode element under PostalTradeAddress
					Element shipToPostalCode = document.createElement("ram:PostcodeCode");
					shipToPostalCode.appendChild(document.createTextNode("10400"));
					shipToPostalAddr.appendChild(shipToPostalCode);

					// Create BuildingName element under PostalTradeAddress
					Element shipToBuildingName = document.createElement("ram:BuildingName");
					shipToBuildingName.appendChild(document.createTextNode("อาคารเบญจจินดา"));
					shipToPostalAddr.appendChild(shipToBuildingName);

					// Create StreetName element under PostalTradeAddress
					Element shipToStreetName = document.createElement("ram:StreetName");
					shipToStreetName.appendChild(document.createTextNode("กำแพงเพชร 6"));
					shipToPostalAddr.appendChild(shipToStreetName);

					// Create CityName element under PostalTradeAddress
					Element shipToCityName = document.createElement("ram:CityName");
					shipToCityName.appendChild(document.createTextNode("1030"));
					shipToPostalAddr.appendChild(shipToCityName);

					// Create CitySubDivisionName element under PostalTradeAddress
					Element shipToCitySubDivisionName = document.createElement("ram:CitySubDivisionName");
					shipToCitySubDivisionName.appendChild(document.createTextNode("103001"));
					shipToPostalAddr.appendChild(shipToCitySubDivisionName);

					// Create CountryID element under PostalTradeAddress
					Element shipToCountryID = document.createElement("ram:CountryID");
					shipToCountryID.appendChild(document.createTextNode("TH"));
					shipToPostalAddr.appendChild(shipToCountryID);

					// Create CountrySubDivisionID element under PostalTradeAddress
					Element shipToCountrySubDivisionID = document.createElement("ram:CountrySubDivisionID");
					shipToCountrySubDivisionID.appendChild(document.createTextNode("10"));
					shipToPostalAddr.appendChild(shipToCountrySubDivisionID);

					// Create BuildingNumber element under PostalTradeAddress
					Element shipToBuildingNo = document.createElement("ram:BuildingNumber");
					shipToBuildingNo.appendChild(document.createTextNode("40/20"));
					shipToPostalAddr.appendChild(shipToBuildingNo);

					// Create ShipFromTradeParty element under ApplicableHeaderTradeDelivery
					Element shipFromTradeParty = document.createElement("ram:ShipFromTradeParty");
					applicableHeaderTradeDelivery.appendChild(shipFromTradeParty);

					// Create ID element under ShipFromTradeParty
					Element id2 = document.createElement("ram:ID");
					id2.appendChild(document.createTextNode("T0019"));
					shipFromTradeParty.appendChild(id2);

					// Create GlobalID element under ShipFromTradeParty
					Element globalID2 = document.createElement("ram:GlobalID");
					globalID2.appendChild(document.createTextNode("8850001999993"));
					globalID2.setAttribute("schemeID", "GLN");
					globalID2.setAttribute("schemeAgencyID", "GS1");
					shipFromTradeParty.appendChild(globalID2);

					// Create Name element under ShipFromTradeParty
					Element name2 = document.createElement("ram:Name");
					name2.appendChild(document.createTextNode("ชื่อผู้ขาย"));
					shipFromTradeParty.appendChild(name2);

					// Create ActualDeliverySupplyChainEvent element under ApplicableHeaderTradeDelivery
					Element actualDeliverySupplyChainEvent = document.createElement("ram:ActualDeliverySupplyChainEvent");
					applicableHeaderTradeDelivery.appendChild(actualDeliverySupplyChainEvent);

					// Create OccurrenceDateTime element under ActualDeliverySupplyChainEvent
					Element occurrenceDateTime = document.createElement("ram:OccurrenceDateTime");
					occurrenceDateTime.appendChild(document.createTextNode("2006-05-04T00:00:00.0"));
					actualDeliverySupplyChainEvent.appendChild(occurrenceDateTime);

					// ####################################################### //
			        // >>> 4.  Create ApplicableHeaderTradeSettlement element <<< //
				    // ####################################################### //
					
					// Create ApplicableHeaderTradeDelivery element
					Element headerTradeSettlement = document.createElement("ram:ApplicableHeaderTradeSettlement");
					supplyChainTradeTransaction.appendChild(headerTradeSettlement);
					
					// Create InvoiceCurrencyCode element under ApplicableHeaderTradeSettlement
					Element invCurrencyCode = document.createElement("ram:InvoiceCurrencyCode");
					invCurrencyCode.appendChild(document.createTextNode("THB"));
					headerTradeSettlement.appendChild(invCurrencyCode);
					
					// Create ApplicableTradeTax element under ApplicableHeaderTradeSettlement
					Element applicableTradeTax = document.createElement("ram:ApplicableTradeTax");
					headerTradeSettlement.appendChild(applicableTradeTax);

					// Create TypeCode element under ApplicableTradeTax
					Element typeCode = document.createElement("ram:TypeCode");
					typeCode.appendChild(document.createTextNode("VAT"));
					applicableTradeTax.appendChild(typeCode);

					// Create CalculatedRate element under ApplicableTradeTax
					Element calculatedRate = document.createElement("ram:CalculatedRate");
					calculatedRate.appendChild(document.createTextNode("7.00"));
					applicableTradeTax.appendChild(calculatedRate);

					// Create BasisAmount element under ApplicableTradeTax
					Element basisAmount = document.createElement("ram:BasisAmount");
					basisAmount.appendChild(document.createTextNode("1000.00"));
					applicableTradeTax.appendChild(basisAmount);

					// Create SpecifiedTradeAllowanceCharge element under ApplicableHeaderTradeSettlement
					Element specifiedTradeAllowanceCharge = document.createElement("ram:SpecifiedTradeAllowanceCharge");
					headerTradeSettlement.appendChild(specifiedTradeAllowanceCharge);

					// Create ChargeIndicator element under SpecifiedTradeAllowanceCharge
					Element chargeIndicator = document.createElement("ram:ChargeIndicator");
					chargeIndicator.appendChild(document.createTextNode("true"));
					specifiedTradeAllowanceCharge.appendChild(chargeIndicator);

					// Create ActualAmount element under SpecifiedTradeAllowanceCharge
					Element actualAmount = document.createElement("ram:ActualAmount");
					actualAmount.appendChild(document.createTextNode("10"));
					specifiedTradeAllowanceCharge.appendChild(actualAmount);

					// Create ReasonCode element under SpecifiedTradeAllowanceCharge
					Element reasonCode = document.createElement("ram:ReasonCode");
					reasonCode.appendChild(document.createTextNode("79"));
					specifiedTradeAllowanceCharge.appendChild(reasonCode);

					// Create SpecifiedTradePaymentTerms element under ApplicableHeaderTradeSettlement
					Element specifiedTradePaymentTerms = document.createElement("ram:SpecifiedTradePaymentTerms");
					specifiedTradePaymentTerms.appendChild(document.createTextNode("49"));
					headerTradeSettlement.appendChild(specifiedTradePaymentTerms);

					// Create SpecifiedTradeSettlementHeaderMonetarySummation element under ApplicableHeaderTradeSettlement
					Element specifiedTradeSettlementHeaderMonetarySummation = document.createElement("ram:SpecifiedTradeSettlementHeaderMonetarySummation");
					headerTradeSettlement.appendChild(specifiedTradeSettlementHeaderMonetarySummation);

					// Create LineTotalAmount element under SpecifiedTradeSettlementHeaderMonetarySummation
					Element lineTotalAmount = document.createElement("ram:LineTotalAmount");
					lineTotalAmount.appendChild(document.createTextNode("1000.00"));
					specifiedTradeSettlementHeaderMonetarySummation.appendChild(lineTotalAmount);

					// Create AllowanceTotalAmount element under SpecifiedTradeSettlementHeaderMonetarySummation
					Element allowanceTotalAmount = document.createElement("ram:AllowanceTotalAmount");
					allowanceTotalAmount.appendChild(document.createTextNode("10"));
					specifiedTradeSettlementHeaderMonetarySummation.appendChild(allowanceTotalAmount);

					// Create TaxTotalAmount element under SpecifiedTradeSettlementHeaderMonetarySummation
					Element taxTotalAmount = document.createElement("ram:TaxTotalAmount");
					taxTotalAmount.appendChild(document.createTextNode("70.00"));
					specifiedTradeSettlementHeaderMonetarySummation.appendChild(taxTotalAmount);

					// Create GrandTotalAmount element under SpecifiedTradeSettlementHeaderMonetarySummation
					Element grandTotalAmount = document.createElement("ram:GrandTotalAmount");
					grandTotalAmount.appendChild(document.createTextNode("1070.00"));
					specifiedTradeSettlementHeaderMonetarySummation.appendChild(grandTotalAmount);

					// ####################################################### //
			        // >>> 5.1  Create IncludedSupplyChainTradeLineItem element <<< //
				    // ####################################################### //
					
					// Create ApplicableHeaderTradeDelivery element
					Element includedSupplyChainTradeLineItem = document.createElement("ram:IncludedSupplyChainTradeLineItem");
					supplyChainTradeTransaction.appendChild(includedSupplyChainTradeLineItem);
					
					// Create AssociatedDocumentLineDocument element under IncludedSupplyChainTradeLineItem
					Element associatedDocumentLineDocument = document.createElement("ram:AssociatedDocumentLineDocument");
					includedSupplyChainTradeLineItem.appendChild(associatedDocumentLineDocument);

					// Create LineID element under AssociatedDocumentLineDocument
					Element lineID = document.createElement("ram:LineID");
					lineID.appendChild(document.createTextNode("1"));
					associatedDocumentLineDocument.appendChild(lineID);

					// Create SpecifiedLineTradeSettlement element under IncludedSupplyChainTradeLineItem
					Element specifiedLineTradeSettlement = document.createElement("ram:SpecifiedLineTradeSettlement");
					includedSupplyChainTradeLineItem.appendChild(specifiedLineTradeSettlement);

					// ####################################################### //
			        // >>> 5.2  Create IncludedSupplyChainTradeLineItem element <<< //
				    // ####################################################### //
					
					// Create ApplicableHeaderTradeDelivery element
					Element includedSupplyChainTradeLineItem1 = document.createElement("ram:IncludedSupplyChainTradeLineItem");
					supplyChainTradeTransaction.appendChild(includedSupplyChainTradeLineItem1);
					
					// Create AssociatedDocumentLineDocument element under IncludedSupplyChainTradeLineItem
					Element associatedDocumentLineDocument1 = document.createElement("ram:AssociatedDocumentLineDocument");
					includedSupplyChainTradeLineItem1.appendChild(associatedDocumentLineDocument1);

					// Create LineID element under AssociatedDocumentLineDocument
					Element lineID1 = document.createElement("ram:LineID");
					lineID1.appendChild(document.createTextNode("LineID1"));
					associatedDocumentLineDocument1.appendChild(lineID1);

					// Create SpecifiedLineTradeSettlement element under IncludedSupplyChainTradeLineItem
					Element specifiedLineTradeSettlement1 = document.createElement("ram:SpecifiedLineTradeSettlement");
					includedSupplyChainTradeLineItem1.appendChild(specifiedLineTradeSettlement1);
					
					// Append sub root, rsm:SupplyChainTradeTransaction, to root
					root.appendChild(supplyChainTradeTransaction); 
					document.appendChild(root); // Append root to document
					
					// ############# Finish : Generating XML tag #############
					// ==================================================================
					// ############# Start : Implementing Digital Signature #############
					
					// Implement KeyStorePasswordProvider
					KeyStorePasswordProvider storePasswordProvider = new KeyStorePasswordProvider() {
						@Override
						public char[] getPassword() {
							return "password".toCharArray(); // Replace with your actual KeyStore password
						}
					};
					
					// Implement KeyEntryPasswordProvider
					KeyEntryPasswordProvider entryPasswordProvider =
							new KeyEntryPasswordProvider() {
								@Override
								public char[] getPassword(String alias, X509Certificate certificate) {
									return "password".toCharArray(); 
								}
							};
					
					// Load the KeyStore
					KeyStore keyStore = KeyStore.getInstance("PKCS12"); // Specify the KeyStore type
					
					// Load the KeyStore file
					File keyStoreFile = new File("src/main/resources/key.p12");
					
					// Define the signing key/certificate
					KeyingDataProvider keyProvider = FileSystemKeyStoreKeyingDataProvider.builder(keyStore.getType(),
							 keyStoreFile.getAbsolutePath(),
                             SigningCertificateSelector.single()).entryPassword(entryPasswordProvider).storePassword(storePasswordProvider).build();
					
					// Define the signed object
					DataObjectDesc obj = new DataObjectReference("")
					        .withTransform(new EnvelopedSignatureTransform())
					        .withDataObjectFormat(new DataObjectFormatProperty("application/xml"));
					
					// Create the signature
					XadesSigner signer = new XadesBesSigningProfile(keyProvider).newSigner();
					signer.sign(new SignedDataObjects(obj), document.getDocumentElement());
					
					// ==================================================================
					
					TransformerFactory tf = TransformerFactory.newInstance();
			        Transformer transformer = tf.newTransformer();
			        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			        // Output XML to StringWriter
			        transformer.transform(new DOMSource(document), new StreamResult(writer));
			        xmlString = writer.getBuffer().toString();


			        // ############# Finish : Transform Document to XML String ############
				  
			    } catch (ParserConfigurationException | TransformerException e) {
			    	e.printStackTrace();
			    } catch (DOMException ex) {
			       ex.printStackTrace();
			    }
			}
		 	
	        // Return XML as response
	        return xmlString;
	 }
	 
	 
	 @GetMapping("/invoices/{id}/download2")
	 public ResponseEntity<String> downloadXml2(@PathVariable Long id) throws Exception {
		 	// Invoice invObj = invoiceService.retrieveInvoice(id).get();
		 	
		 	Invoice invObj = null;
		 	Optional<Invoice> optionalInvObj = invoiceService.retrieveInvoice(id);
		 	if (optionalInvObj.isPresent()) {
		 	    invObj = optionalInvObj.get();
		 	} 
		 	
		 	StringWriter writer = new StringWriter();
		 	String xmlString = "";
		 	if(invObj != null) {
			   try {
			        // ############# Start : Generating XML tag ################
			        // ============================================================
			        // ######################################################### //
			        //    Create root element : Invoice_CrossIndustryInvoice     //
			        // ######################################################### //
			       
				   
				    /// 1 - Import XML Schema: 
			        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			        DocumentBuilder builder = factory.newDocumentBuilder();
			        Document document = builder.parse(new File("src/main/resources/TaxInvoice_CrossIndustryInvoice_2p0.xsd"));
			        /*
			        Element root = document.createElementNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:import");
			        root.setAttributeNS("http://www.w3.org/2001/xmlns/", "xmlns:ram", "src/main/resources/TaxInvoice_ReusableAggregateBusinessInformationEntity_2p0.xsd");
			        root.setAttributeNS("http://www.w3.org/2001/xmlns/", "xmlns:rsm", "src/main/resources/TaxInvoice_CrossIndustryInvoice_2p0.xsd");
			        root.setAttributeNS("http://www.w3.org/2001/xmlns/", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			        root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation", "src/main/resources/TaxInvoice_CrossIndustryInvoice_2p0.xsd");
			         */

					// Element root = document.createElementNS("urn:etda:uncefact:data:standard:TaxInvoice_CrossIndustryInvoice:2", "rsm:Invoice_CrossIndustryInvoice");
					Element root = document.createElementNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:import");
					root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ram", "urn:etda:uncefact:data:standard:TaxInvoice_ReusableAggregateBusinessInformationEntity:2");
					root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:rsm", "urn:etda:uncefact:data:standard:TaxInvoice_CrossIndustryInvoice:2");
					root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
					root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation", "urn:etda:uncefact:data:standard:TaxInvoice_CrossIndustryInvoice:2 src/main/resources/TaxInvoice_CrossIndustryInvoice_2p0.xsd");

					// ######################################################### //
			        //    Create sub-root element (1) : ExchangedDocumentContext //
			        // ######################################################### //
			        
			        Element exchangedDocumentContext = document.createElement("rsm:ExchangedDocumentContext");
			        // Create GuidelineSpecifiedDocumentContextParameter element under ExchangedDocumentContext
			        Element guidelineParameter = document.createElement("ram:GuidelineSpecifiedDocumentContextParameter");
			        exchangedDocumentContext.appendChild(guidelineParameter);
			        // Append sub-root to root
			        root.appendChild(exchangedDocumentContext);
			        // Create ID element under GuidelineSpecifiedDocumentContextParameter
			        Element guideLineId = document.createElement("ram:ID");
			        guideLineId.appendChild(document.createTextNode("ER3-2560"));
			        guideLineId.setAttribute("schemeAgencyID", "ETDA");
			        guideLineId.setAttribute("schemeVersionID", "v2.0");
			        guidelineParameter.appendChild(guideLineId);
			        
			        // ################################################## //
			        // Create sub-root element (2) : ExchangedDocument    //
			        // ################################################## //
			        
			        Element exchangedDocument = document.createElement("rsm:ExchangedDocument");
			        // Create ID element under ExchangedDocument
			        Element exchangeID = document.createElement("ram:ID");
			        exchangeID.appendChild(document.createTextNode("INV01-2564"));
			        exchangedDocument.appendChild(exchangeID);
			        
			        // Create Name element under ExchangedDocument
			        Element exchangeName = document.createElement("ram:Name");
			        exchangeName.appendChild(document.createTextNode("ใบกำกับภาษี"));
			        exchangedDocument.appendChild(exchangeName);
			       
			        // Create TypeCode element under ExchangedDocument
			        Element exchangeTypeCode = document.createElement("ram:TypeCode");
			        exchangeTypeCode.appendChild(document.createTextNode("380"));
			        exchangedDocument.appendChild(exchangeTypeCode);
			        
			        // Create IssueDateTime element under ExchangedDocument
			        Element exchangeIssueDateTime = document.createElement("ram:IssueDateTime");
			        exchangeIssueDateTime.appendChild(document.createTextNode("2023-05-04T00:00:00.0"));
			        exchangedDocument.appendChild(exchangeIssueDateTime);
			        
			        // Create IssueDateTime element under ExchangedDocument
			        Element exchangeCreationDateTime = document.createElement("ram:CreationDateTime");
			        exchangeCreationDateTime.appendChild(document.createTextNode("2023-05-04T00:00:00.0"));
			        exchangedDocument.appendChild(exchangeCreationDateTime);
			        
				    root.appendChild(exchangedDocument);
				    
				    // ############################################################## //
			        // Create sub-root element (3) : SupplyChainTradeTransaction      //
			        // ############################################################## //
				    
			        Element supplyChainTradeTransaction = document.createElement("rsm:SupplyChainTradeTransaction");
			        
			        // Create ApplicableHeaderTradeAgreement element
			        Element applicableHeaderTradeAgreement = document.createElement("ram:ApplicableHeaderTradeAgreement");
			        supplyChainTradeTransaction.appendChild(applicableHeaderTradeAgreement);
			        
			        // ########################################### //
			        // >>> 1.  Create SellerTradeParty element <<< //
			        // ########################################### //
			        Element sellerTradeParty = document.createElement("ram:SellerTradeParty");
			        applicableHeaderTradeAgreement.appendChild(sellerTradeParty);
			        

			        // Create Name element under SellerTradeParty 
			        Element name = document.createElement("ram:Name");
			        name.appendChild(document.createTextNode(invObj.getSellerName())); // 
			        sellerTradeParty.appendChild(name);

			        /////////// ###### Seller - SpecifiedTaxRegistration ###### ///////////
			        // Create SpecifiedTaxRegistration element
			        Element taxRegistration = document.createElement("ram:SpecifiedTaxRegistration");
			        sellerTradeParty.appendChild(taxRegistration);
			        
			        // Tax ID element under SpecifiedTaxRegistration
			        Element sellerTaxID = document.createElement("ram:ID");
			        sellerTaxID.appendChild(document.createTextNode(invObj.getSellerTaxId() + "" + invObj.getSellerBranchId()));
			        sellerTaxID.setAttribute("schemeID", "TXID");
			        taxRegistration.appendChild(sellerTaxID);
			        
			        /////////// ###### Seller - DefinedTradeContact ###### ///////////
					// Create DefinedTradeContact element
					Element sellerTradeContact = document.createElement("ram:DefinedTradeContact");
					sellerTradeParty.appendChild(sellerTradeContact);
	
					///************************///
					// Create EmailURIUniversalCommunication element under DefinedTradeContact
					Element sellerEmailURI = document.createElement("ram:EmailURIUniversalCommunication");
					sellerTradeContact.appendChild(sellerEmailURI);
	
					// Create URIID element under EmailURIUniversalCommunication
					Element sellerUriId = document.createElement("ram:URIID");
					sellerUriId.appendChild(document.createTextNode(invObj.getSellerEmail()));
					sellerEmailURI.appendChild(sellerUriId);
					
					// Create TelephoneUniversalCommunication element under DefinedTradeContact
					Element sellerTelephone = document.createElement("ram:TelephoneUniversalCommunication");
					sellerTradeContact.appendChild(sellerTelephone);
					
					Element sellerCompleteNO = document.createElement("ram:CompleteNumber");
					Text phoneNumber = document.createTextNode("XXX-SELLER-XX"); // Replace with actual phone number
					sellerCompleteNO.appendChild(phoneNumber);
					sellerTelephone.appendChild(sellerCompleteNO);
					///************************///
					
				    /////////// ###### Seller - PostalTradeAddress ###### ///////////
			        // Create PostalTradeAddress element
				    Element sellerTradeAddress = document.createElement("ram:PostalTradeAddress");
				    sellerTradeParty.appendChild(sellerTradeAddress);
				    
				    // Create PostcodeCode element under PostalTradeAddress
				    Element sellerPostcode = document.createElement("ram:PostcodeCode");
				    sellerPostcode.appendChild(document.createTextNode(invObj.getSellerPostCode()));
				    sellerTradeAddress.appendChild(sellerPostcode);
	
				    // Create BuildingName element under PostalTradeAddress
				    Element sellerBuildingName = document.createElement("ram:LineOne");
				    sellerBuildingName.appendChild(document.createTextNode("เดอะมอลล์บางกะปิ"));
				    sellerTradeAddress.appendChild(sellerBuildingName);
	
				    // Create CityName element under PostalTradeAddress
				    Element sellerCityName = document.createElement("ram:CityName");
				    sellerCityName.appendChild(document.createTextNode("1006"));
				    sellerTradeAddress.appendChild(sellerCityName);
	
				    // Create CitySubDivisionName element under PostalTradeAddress
				    Element sellerCitySubDivisionName = document.createElement("ram:CitySubDivisionName");
				    sellerCitySubDivisionName.appendChild(document.createTextNode("100601"));
				    sellerTradeAddress.appendChild(sellerCitySubDivisionName);
	
				    // Create CountryID element under PostalTradeAddress
				    Element sellerCountryID = document.createElement("ram:CountryID");
				    sellerCountryID.appendChild(document.createTextNode("TH"));
				    sellerCountryID.setAttribute("schemeID", "3166-1 alpha-2");
				    sellerTradeAddress.appendChild(sellerCountryID);
				    
				    // Create CountrySubDivisionID element under PostalTradeAddress
				    Element sellerCountrySubDivisionID = document.createElement("ram:CountrySubDivisionID");
				    sellerCountrySubDivisionID.appendChild(document.createTextNode("86"));
				    sellerTradeAddress.appendChild(sellerCountrySubDivisionID);
				    
				    // Create BuildingNumber element under PostalTradeAddress
				    Element sellerBuildingNumber = document.createElement("ram:BuildingNumber");
				    sellerBuildingNumber.appendChild(document.createTextNode("7/99"));
				    sellerTradeAddress.appendChild(sellerBuildingNumber);
				    
				    // ########################################### //
			        // >>> 2.  Create BuyerTradeParty element <<<  //
				    // ########################################### //
				    
			        Element buyerTradeParty = document.createElement("ram:BuyerTradeParty");
			        applicableHeaderTradeAgreement.appendChild(buyerTradeParty);
			      
			        // Create Name element under BuyerTradeParty 
			        Element buyerName = document.createElement("ram:Name");
			        buyerName.appendChild(document.createTextNode(invObj.getBuyerName()));
			        buyerTradeParty.appendChild(buyerName);
			        
			        // Create SpecifiedTaxRegistration element under BuyerTradeParty
			        Element buyerTaxReg = document.createElement("ram:SpecifiedTaxRegistration");
			        buyerTradeParty.appendChild(buyerTaxReg);
			        
			        // ID element under SpecifiedTaxRegistration
			        Element buyerTaxID = document.createElement("ram:ID");
			        buyerTaxID.appendChild(document.createTextNode(invObj.getBuyerTaxId() + "" + invObj.getBuyerBranchId()));
			        buyerTaxID.setAttribute("schemeID", "TXID");
			        buyerTaxReg.appendChild(buyerTaxID);
			        
			        // ############ BuyerTradeParty - DefinedTradeContact ############
			        // Create DefinedTradeContact element
			        Element tradeContact = document.createElement("ram:DefinedTradeContact");
			        buyerTradeParty.appendChild(tradeContact);

			        // Create EmailURIUniversalCommunication element under DefinedTradeContact
			        Element emailURI = document.createElement("ram:EmailURIUniversalCommunication");
			        tradeContact.appendChild(emailURI);
			        // Create URIID element under EmailURIUniversalCommunication
			        Element uriId = document.createElement("ram:URIID");
			        uriId.appendChild(document.createTextNode(invObj.getBuyerEmail()));
			        emailURI.appendChild(uriId);

			        // Create TelephoneUniversalCommunication element under DefinedTradeContact
					Element buyerPhone = document.createElement("ram:TelephoneUniversalCommunication");
					tradeContact.appendChild(buyerPhone);
					// Create CompleteNumber element under TelephoneUniversalCommunication
					Element buyerCompleteNO = document.createElement("ram:CompleteNumber");
					buyerCompleteNO.appendChild(document.createTextNode(invObj.getBuyerPhoneNo()));
					buyerPhone.appendChild(sellerCompleteNO);
			        
			        // ############ BuyerTradeParty - PostalTradeAddress ############
				    // Create PostalTradeAddress element
				    Element postalTradeAddress = document.createElement("ram:PostalTradeAddress");
				    buyerTradeParty.appendChild(postalTradeAddress);
	
				    // Create PostcodeCode element under PostalTradeAddress
				    Element postcodeCode = document.createElement("ram:PostcodeCode");
				    postcodeCode.appendChild(document.createTextNode(invObj.getBuyerPostCode()));
				    postalTradeAddress.appendChild(postcodeCode);
	
				    // Create BuildingName element under PostalTradeAddress
				   //  Element buildingName = document.createElement("ram:BuildingName");
				    Element buildingName = document.createElement("ram:LineOne");
				    buildingName.appendChild(document.createTextNode("อาคารเบญจจินดา"));
				    postalTradeAddress.appendChild(buildingName);
	
				    // Create CityName element under PostalTradeAddress
				    Element cityName = document.createElement("ram:CityName");
				    cityName.appendChild(document.createTextNode("1030"));
				    postalTradeAddress.appendChild(cityName);
	
				    // Create CitySubDivisionName element under PostalTradeAddress
				    Element citySubDivisionName = document.createElement("ram:CitySubDivisionName");
				    citySubDivisionName.appendChild(document.createTextNode("103001"));
				    postalTradeAddress.appendChild(citySubDivisionName);
	
				    // Create CountryID element under PostalTradeAddress
				    Element countryID = document.createElement("ram:CountryID");
				    countryID.setAttribute("schemeID", "3166-1 alpha-2");
				    countryID.appendChild(document.createTextNode("TH"));
				    postalTradeAddress.appendChild(countryID);
				    
				    // Create CountrySubDivisionID element under PostalTradeAddress
				    Element countrySubDivisionID = document.createElement("ram:CountrySubDivisionID");
				    countrySubDivisionID.appendChild(document.createTextNode("10"));
				    postalTradeAddress.appendChild(countrySubDivisionID);
	
				    // Create BuildingNumber element under PostalTradeAddress
				    Element buildingNumber = document.createElement("ram:BuildingNumber");
				    buildingNumber.appendChild(document.createTextNode("40/20"));
				    postalTradeAddress.appendChild(buildingNumber);
				    
					// ####################################################### //
			        // >>> 3.  Create ApplicableHeaderTradeDelivery element << //
				    // ####################################################### //
					
					// Create ApplicableHeaderTradeDelivery element
					Element applicableHeaderTradeDelivery = document.createElement("ram:ApplicableHeaderTradeDelivery");
					supplyChainTradeTransaction.appendChild(applicableHeaderTradeDelivery);
					
					// Create ShipToTradeParty element under ApplicableHeaderTradeDelivery
					Element shipToTradeParty = document.createElement("ram:ShipToTradeParty");
					applicableHeaderTradeDelivery.appendChild(shipToTradeParty);
					
					// Create DefinedTradeContact element under ShipToTradeParty
					Element definedTradeContact = document.createElement("ram:DefinedTradeContact");
					shipToTradeParty.appendChild(definedTradeContact);
					// Create PersonName element under DefinedTradeContact
					Element deliveryPersonName = document.createElement("ram:PersonName");
					deliveryPersonName.appendChild(document.createTextNode(invObj.getDeliveryPerson()));
					definedTradeContact.appendChild(deliveryPersonName);

					/*
					// Create DepartmentName element under DefinedTradeContact
					Element deliveryDepartmentName = document.createElement("ram:DepartmentName");
					deliveryDepartmentName.appendChild(document.createTextNode("ฝ่ายขาย"));
					definedTradeContact.appendChild(deliveryDepartmentName);
					*/
					// ####################################################### //
			        // >>> 4.  Create ApplicableHeaderTradeSettlement element <<< //
				    // ####################################################### //
					
					// Create ApplicableHeaderTradeDelivery element
					Element headerTradeSettlement = document.createElement("ram:ApplicableHeaderTradeSettlement");
					supplyChainTradeTransaction.appendChild(headerTradeSettlement);
					
					// Create InvoiceCurrencyCode element under ApplicableHeaderTradeSettlement
					Element invCurrencyCode = document.createElement("ram:InvoiceCurrencyCode");
					invCurrencyCode.setAttribute("listID", "ISO 4217 3A");
					invCurrencyCode.appendChild(document.createTextNode("THB"));
					headerTradeSettlement.appendChild(invCurrencyCode);
					
					/// ###### Start : ApplicableTradeTax ###### ///
					// Create ApplicableTradeTax element under ApplicableHeaderTradeSettlement
					Element applicableTradeTax = document.createElement("ram:ApplicableTradeTax");
					headerTradeSettlement.appendChild(applicableTradeTax);
					// Create TypeCode element under ApplicableTradeTax
					Element typeCode = document.createElement("ram:TypeCode");
					typeCode.appendChild(document.createTextNode("VAT"));
					applicableTradeTax.appendChild(typeCode);
					// Create CalculatedRate element under ApplicableTradeTax
					Element calculatedRate = document.createElement("ram:CalculatedRate");
					calculatedRate.appendChild(document.createTextNode("7"));
					applicableTradeTax.appendChild(calculatedRate);
					// Create BasisAmount element under ApplicableTradeTax
					Element basisAmount = document.createElement("ram:BasisAmount");
					basisAmount.appendChild(document.createTextNode("1000"));
					applicableTradeTax.appendChild(basisAmount);
					// Create CalculatedAmount element under ApplicableTradeTax
					Element calculatedAmount = document.createElement("ram:CalculatedAmount");
					calculatedAmount.appendChild(document.createTextNode("699.93"));
					applicableTradeTax.appendChild(calculatedAmount);
					/// ###### Finish : ApplicableTradeTax ###### ///
					
					/// ###### Start : SpecifiedTradeSettlementHeaderMonetarySummation ###### ///
					// Create SpecifiedTradeSettlementHeaderMonetarySummation element under ApplicableHeaderTradeSettlement
					Element specifiedTradeSettlementHeaderMonetarySummation = document.createElement("ram:SpecifiedTradeSettlementHeaderMonetarySummation");
					headerTradeSettlement.appendChild(specifiedTradeSettlementHeaderMonetarySummation);

					// Create LineTotalAmount element under SpecifiedTradeSettlementHeaderMonetarySummation
					Element lineTotalAmount = document.createElement("ram:LineTotalAmount");
					lineTotalAmount.appendChild(document.createTextNode("1000.00"));
					specifiedTradeSettlementHeaderMonetarySummation.appendChild(lineTotalAmount);

					// Create AllowanceTotalAmount element under SpecifiedTradeSettlementHeaderMonetarySummation
					Element allowanceTotalAmount = document.createElement("ram:TaxBasisTotalAmount");
					allowanceTotalAmount.appendChild(document.createTextNode("10"));
					specifiedTradeSettlementHeaderMonetarySummation.appendChild(allowanceTotalAmount);

					// Create TaxTotalAmount element under SpecifiedTradeSettlementHeaderMonetarySummation
					Element taxTotalAmount = document.createElement("ram:TaxTotalAmount");
					taxTotalAmount.appendChild(document.createTextNode("70.00"));
					specifiedTradeSettlementHeaderMonetarySummation.appendChild(taxTotalAmount);

					// Create GrandTotalAmount element under SpecifiedTradeSettlementHeaderMonetarySummation
					Element grandTotalAmount = document.createElement("ram:GrandTotalAmount");
					grandTotalAmount.appendChild(document.createTextNode("1070.00"));
					specifiedTradeSettlementHeaderMonetarySummation.appendChild(grandTotalAmount);
					/// ###### Finish : SpecifiedTradeSettlementHeaderMonetarySummation ###### ///
					
					// ####################################################### //
			        // >>> 5.1  Create IncludedSupplyChainTradeLineItem element <<< //
				    // ####################################################### //
					
					// Create ApplicableHeaderTradeDelivery element
					Element includedSupplyChainTradeLineItem = document.createElement("ram:IncludedSupplyChainTradeLineItem");
					supplyChainTradeTransaction.appendChild(includedSupplyChainTradeLineItem);
					
					// Create AssociatedDocumentLineDocument element under IncludedSupplyChainTradeLineItem
					Element associatedDocumentLineDocument = document.createElement("ram:AssociatedDocumentLineDocument");
					includedSupplyChainTradeLineItem.appendChild(associatedDocumentLineDocument);
					// Create LineID element under AssociatedDocumentLineDocument
					Element lineID = document.createElement("ram:LineID");
					lineID.appendChild(document.createTextNode("1"));
					associatedDocumentLineDocument.appendChild(lineID);

					// Create SpecifiedTradeProduct element under IncludedSupplyChainTradeLineItem
					Element specifiedTradeProduct = document.createElement("ram:SpecifiedTradeProduct");
					includedSupplyChainTradeLineItem.appendChild(specifiedTradeProduct);
					// Create Name element under SpecifiedTradeProduct
					Element tradeProdName = document.createElement("ram:Name");
					tradeProdName.appendChild(document.createTextNode("สินค้าทดสอบ"));
					specifiedTradeProduct.appendChild(tradeProdName);
					
					
					// Create SpecifiedLineTradeAgreement element under IncludedSupplyChainTradeLineItem
					Element specifiedLineTradeAgreement = document.createElement("ram:SpecifiedLineTradeAgreement");
					includedSupplyChainTradeLineItem.appendChild(specifiedLineTradeAgreement);
					// Create Name element under SpecifiedTradeProduct
					Element grossPrice = document.createElement("ram:GrossPriceProductTradePrice");
					Element chargeAmount = document.createElement("ram:ChargeAmount");
					chargeAmount.appendChild(document.createTextNode("9999"));
					grossPrice.appendChild(chargeAmount);
					specifiedLineTradeAgreement.appendChild(grossPrice);
					
					// Create SpecifiedLineTradeDelivery element under IncludedSupplyChainTradeLineItem
					Element specifiedLineTradeDelivery = document.createElement("ram:SpecifiedLineTradeDelivery");
					includedSupplyChainTradeLineItem.appendChild(specifiedLineTradeDelivery);
					// Create Name element under SpecifiedTradeProduct
					Element billedQuantity = document.createElement("ram:BilledQuantity");
					billedQuantity.appendChild(document.createTextNode("1"));
					billedQuantity.setAttribute("unitCode", "AS");
					specifiedLineTradeDelivery.appendChild(billedQuantity);
					
					// Create SpecifiedLineTradeSettlement element under IncludedSupplyChainTradeLineItem
					Element specifiedLineTradeSettlement = document.createElement("ram:SpecifiedLineTradeSettlement");
					includedSupplyChainTradeLineItem.appendChild(specifiedLineTradeSettlement);
					// SpecifiedTradeSettlementLineMonetarySummation under SpecifiedLineTradeSettlement
					Element specifiedTradeLineMonetarySum = document.createElement("ram:SpecifiedTradeSettlementLineMonetarySummation");
					specifiedLineTradeSettlement.appendChild(specifiedTradeLineMonetarySum);
					// NetLineTotalAmount
					Element netLineTotalAmount = document.createElement("ram:NetLineTotalAmount");
					netLineTotalAmount.appendChild(document.createTextNode("9999"));
					specifiedTradeLineMonetarySum.appendChild(netLineTotalAmount);
					// NetIncludingTaxesLineTotalAmount
					Element netIncludingTaxesLineTotalAmount = document.createElement("ram:NetIncludingTaxesLineTotalAmount");
					netIncludingTaxesLineTotalAmount.appendChild(document.createTextNode("10698.93"));
					specifiedTradeLineMonetarySum.appendChild(netIncludingTaxesLineTotalAmount);
					
					// Append sub root, rsm:SupplyChainTradeTransaction, to root
					root.appendChild(supplyChainTradeTransaction); 
					// document.appendChild(root); // Append root to document
				    document.getDocumentElement().appendChild(root);
				
					// ############# Finish : Generating XML tag #############
					// ==================================================================
					// ############# Start : Implementing Digital Signature #############
					
				    /*
					// Implement KeyStorePasswordProvider
					KeyStorePasswordProvider storePasswordProvider = new KeyStorePasswordProvider() {
						@Override
						public char[] getPassword() {
							return "password".toCharArray(); // Replace with your actual KeyStore password
						}
					};
					
					// Implement KeyEntryPasswordProvider
					KeyEntryPasswordProvider entryPasswordProvider =
							new KeyEntryPasswordProvider() {
								@Override
								public char[] getPassword(String alias, X509Certificate certificate) {
									return "password".toCharArray(); 
								}
							};
					
					// Load the KeyStore
					KeyStore keyStore = KeyStore.getInstance("PKCS12"); // Specify the KeyStore type
					
					// Load the KeyStore file
					File keyStoreFile = new File("src/main/resources/key.p12");
					
					// Define the signing key/certificate
					KeyingDataProvider keyProvider = FileSystemKeyStoreKeyingDataProvider.builder(keyStore.getType(),
							 keyStoreFile.getAbsolutePath(),
                             SigningCertificateSelector.single()).entryPassword(entryPasswordProvider).storePassword(storePasswordProvider).build();
					
					// Define the signed object
					DataObjectDesc obj = new DataObjectReference("")
					        .withTransform(new EnvelopedSignatureTransform())
					        .withDataObjectFormat(new DataObjectFormatProperty("text/xml"));
					
					// Create the signature
					XadesSigner signer = new XadesBesSigningProfile(keyProvider).newSigner();
					signer.sign(new SignedDataObjects(obj), document.getDocumentElement());
					*/
					// ==================================================================
					
				    
				    
				    /*
					System.out.println("========= Validing xml schema =========");
					
					// Create SchemaFactory and Schema
		            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
		            Schema schema = schemaFactory.newSchema(new File("src/main/resources/TaxInvoice_CrossIndustryInvoice_2p0.xsd"));
		            Validator validator = schema.newValidator();  // Create Validator
		            DOMSource source = new DOMSource(document);
		            validator.validate(source); // Validate the XML Document
		            
		            System.out.println("**** XML is valid against the XSD ****");
		            
		            System.out.println("========= Validing xml schematron =========");
					
		            // Load Schematron Schema
		            ISchematronResource aResSCH = SchematronResourceSCH.fromClassPath("src/main/resources/TaxInvoice_Schematron_2p0.sch");
		            if (!aResSCH.isValidSchematron()) {
		                throw new IllegalArgumentException("Invalid Schematron!");
		            }
		            // Validate the XML document
		            SchematronOutputType aSOT = aResSCH.applySchematronValidationToSVRL(new DOMSource(document));

		            // Get all failed assertions
		            List<SVRLFailedAssert> aFAs = SVRLHelper.getAllFailedAssertions(aSOT);

		            // Handle validation errors
		            for (SVRLFailedAssert aFA : aFAs) {
		                System.out.println("Validation error: " + aFA.getText());
		            }

		            if (aFAs.isEmpty()) {
		                System.out.println("XML document is valid.");
		            }
					*/
					// =====================================================================
					
					TransformerFactory tf = TransformerFactory.newInstance();
			        Transformer transformer = tf.newTransformer();
			        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			        // Output XML to StringWriter
			        transformer.transform(new DOMSource(document), new StreamResult(writer));
			        xmlString = writer.getBuffer().toString();


			        // ############# Finish : Transform Document to XML String ############
				  
			    } catch (ParserConfigurationException | TransformerException e) {
			    	e.printStackTrace();
			    } catch (DOMException ex) {
			       ex.printStackTrace();
			    }
			}
		 	
	        // Return XML as response
	        return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(xmlString);
		 	// return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(xmlString.replace("&#13;", ""));
	 }
}
