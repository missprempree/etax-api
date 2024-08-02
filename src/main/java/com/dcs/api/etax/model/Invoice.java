package com.dcs.api.etax.model;

import java.math.BigDecimal;
import java.util.Date;
import jakarta.persistence.*;

@Entity
@Table(name = "invoice")
public class Invoice {

	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String dataType;

    // ########## Document Type ##########
   
    @Column(nullable = false, length = 10)
    private String documentNo;
    
    @Column(nullable = false, length = 50, columnDefinition = "NVARCHAR(100) COLLATE Thai_CI_AS")
    private String documentName;

    @Column(nullable = false, length = 5)
    private String documentType;

    @Column(nullable = false, length = 5)
    private String documentTypeCode;

    @Column(nullable = true)
    private Date documentIssueDt;
    
    // ########## Trade Transaction : SellerTradeParty ##########

    @Column(nullable = false, length = 13)
    private String sellerTaxId;
    
    @Column(nullable = false, length = 5)
    private String sellerBranchId;
    
    @Column(nullable = true, length = 100, columnDefinition = "NVARCHAR(100) COLLATE Thai_CI_AS")
    private String sellerName;
    
    @Column(nullable = true, length = 50)
    private String sellerEmail;
    
    @Column(nullable = true, length = 50)
    private String sellerPhoneNo;
    
    @Column(nullable = true, length = 5)
    private String sellerPostCode;
    
    // ########## Trade Transaction : BuyerTradeParty ##########
    
    @Column(nullable = false, length = 13)
    private String buyerTaxId;
    
	@Column(nullable = false, length = 5, columnDefinition = "VARCHAR(5) default '00000'")
	private String buyerBranchId;
    
	@Column(nullable = true, length = 100, columnDefinition = "NVARCHAR(100) COLLATE Thai_CI_AS")
	private String buyerName;
    
    @Column(nullable = true, length = 50)
    private String buyerEmail;
    
    @Column(nullable = true, length = 50)
    private String buyerPhoneNo;
    
    @Column(nullable = true, length = 5)
    private String buyerPostCode;
    
    /////////// Reference Document ///////////
    
    @Column(nullable = false)
    private String refInvoiceNo;
    
    @Column(nullable = false)
    private String refReturnDocNo;
    
    
    // ########## ShipToTrade Party ##########
    @Column(nullable = true, length = 100, columnDefinition = "NVARCHAR(100) COLLATE Thai_CI_AS")
    private String deliveryPerson;
    
    
    // ########## Trade Settlement ##########
    
    @Column(nullable = false, length = 3)
    private String docCurrencyCode;
    
    @Column(nullable = false, precision = 9, scale = 2)
    private BigDecimal transferCharge;
    
    @Column(nullable = false, precision = 9, scale = 2)
    private BigDecimal insuranceCharge;
    
    @Column(nullable = false)
    private String paymentTerm;
    
    @Column(nullable = true)
    private String remark;
    
    @Column(nullable = true)
    private String tradeDeliveryTerm;
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getSellerTaxId() {
        return sellerTaxId;
    }

    public void setSellerTaxId(String sellerTaxId) {
        this.sellerTaxId = sellerTaxId;
    }

    public String getSellerBranchId() {
        return sellerBranchId;
    }

    public void setSellerBranchId(String sellerBranchId) {
        this.sellerBranchId = sellerBranchId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentTypeCode() {
        return documentTypeCode;
    }

    public void setDocumentTypeCode(String documentTypeCode) {
        this.documentTypeCode = documentTypeCode;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public Date getDocumentIssueDt() {
        return documentIssueDt;
    }

    public void setDocumentIssueDt(Date documentIssueDt) {
        this.documentIssueDt = documentIssueDt;
    }

	public String getDocumentNo() {
		return documentNo;
	}

	public void setDocumentNo(String documentNo) {
		this.documentNo = documentNo;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getBuyerTaxId() {
		return buyerTaxId;
	}

	public void setBuyerTaxId(String buyerTaxId) {
		this.buyerTaxId = buyerTaxId;
	}

	public String getTradeDeliveryTerm() {
		return tradeDeliveryTerm;
	}

	public void setTradeDeliveryTerm(String tradeDeliveryTerm) {
		this.tradeDeliveryTerm = tradeDeliveryTerm;
	}

	public String getRefInvoiceNo() {
		return refInvoiceNo;
	}

	public void setRefInvoiceNo(String refInvoiceNo) {
		this.refInvoiceNo = refInvoiceNo;
	}

	public String getRefReturnDocNo() {
		return refReturnDocNo;
	}

	public void setRefReturnDocNo(String refReturnDocNo) {
		this.refReturnDocNo = refReturnDocNo;
	}

	public String getDocCurrencyCode() {
		return docCurrencyCode;
	}

	public void setDocCurrencyCode(String docCurrencyCode) {
		this.docCurrencyCode = docCurrencyCode;
	}

	public BigDecimal getTransferCharge() {
		return transferCharge;
	}

	public void setTransferCharge(BigDecimal transferCharge) {
		this.transferCharge = transferCharge;
	}

	public BigDecimal getInsuranceCharge() {
		return insuranceCharge;
	}

	public void setInsuranceCharge(BigDecimal insuranceCharge) {
		this.insuranceCharge = insuranceCharge;
	}

	public String getPaymentTerm() {
		return paymentTerm;
	}

	public void setPaymentTerm(String paymentTerm) {
		this.paymentTerm = paymentTerm;
	}

	public String getSellerName() {
		return sellerName;
	}

	public void setSellerName(String sellerName) {
		this.sellerName = sellerName;
	}

	public String getSellerEmail() {
		return sellerEmail;
	}

	public void setSellerEmail(String sellerEmail) {
		this.sellerEmail = sellerEmail;
	}

	public String getSellerPhoneNo() {
		return sellerPhoneNo;
	}

	public void setSellerPhoneNo(String sellerPhoneNo) {
		this.sellerPhoneNo = sellerPhoneNo;
	}

	public String getSellerPostCode() {
		return sellerPostCode;
	}

	public void setSellerPostCode(String sellerPostCode) {
		this.sellerPostCode = sellerPostCode;
	}

	public String getBuyerBranchId() {
		return buyerBranchId;
	}

	public void setBuyerBranchId(String buyerBranchId) {
		this.buyerBranchId = buyerBranchId;
	}

	public String getBuyerName() {
		return buyerName;
	}

	public void setBuyerName(String buyerName) {
		this.buyerName = buyerName;
	}

	public String getBuyerEmail() {
		return buyerEmail;
	}

	public void setBuyerEmail(String buyerEmail) {
		this.buyerEmail = buyerEmail;
	}

	public String getBuyerPhoneNo() {
		return buyerPhoneNo;
	}

	public void setBuyerPhoneNo(String buyerPhoneNo) {
		this.buyerPhoneNo = buyerPhoneNo;
	}

	public String getBuyerPostCode() {
		return buyerPostCode;
	}

	public void setBuyerPostCode(String buyerPostCode) {
		this.buyerPostCode = buyerPostCode;
	}

	public String getDeliveryPerson() {
		return deliveryPerson;
	}

	public void setDeliveryPerson(String deliveryPerson) {
		this.deliveryPerson = deliveryPerson;
	}
    
	
	
}



