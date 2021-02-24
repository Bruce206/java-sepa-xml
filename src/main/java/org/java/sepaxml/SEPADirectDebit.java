package org.java.sepaxml;

import org.java.sepaxml.xml.XMLNode;
import org.java.sepaxml.format.SEPAFormatDate;
import org.java.sepaxml.xml.XMLNode;

import java.util.Date;
import java.util.List;

public class SEPADirectDebit extends SEPA {

    private String creditorID;

    public SEPADirectDebit(SEPABankAccount reciever, List<SEPATransaction> transactions, String creditorID) {
        this(reciever, transactions, new Date(), creditorID);
    }

    public SEPADirectDebit(SEPABankAccount reciever, List<SEPATransaction> transactions, Date executionDate, String creditorID) {
        super(reciever, transactions, executionDate);
        this.creditorID = creditorID;
        this.build();
    }

    protected String getRootElementName() {
        return "CstmrDrctDbtInitn";
    }

    protected void attachSchemaInformation(XMLNode document) {
        document.attr("xmlns", "urn:iso:std:iso:20022:tech:xsd:pain.008.001.02")
                .attr("xsi:schemaLocation", "urn:iso:std:iso:20022:tech:xsd:pain.008.001.02 pain.008.001.02.xsd")
                .attr("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
    }

    @Override
    protected void build() {
        this.document = new XMLNode().append("Document");

        attachSchemaInformation(document);

        XMLNode nodeCstmrDrctDbtInitn = this.document.append(getRootElementName());
        XMLNode nodeGrpHdr = nodeCstmrDrctDbtInitn.append("GrpHdr");

        nodeGrpHdr.append("MsgId").value(this.reciver.getBIC() + "00" + SEPAFormatDate.formatDate(executionDate));
        nodeGrpHdr.append("CreDtTm").value(SEPAFormatDate.formatDateLong(executionDate));
        nodeGrpHdr.append("NbOfTxs").value(this.transactions.size());
        nodeGrpHdr.append("CtrlSum").value(this.getTransactionVolume().doubleValue());
        nodeGrpHdr.append("InitgPty").append("Nm").value(this.reciver.getName());

        this.nodePmtInf = nodeCstmrDrctDbtInitn.append("PmtInf");
        this.nodePmtInf.append("PmtInfId").value("PMT-ID0-" + SEPAFormatDate.formatDate(executionDate));
        this.nodePmtInf.append("PmtMtd").value("DD");
        this.nodePmtInf.append("BtchBookg").value("true");
        this.nodePmtInf.append("NbOfTxs").value(this.transactions.size());
        this.nodePmtInf.append("CtrlSum").value(this.getTransactionVolume().doubleValue());

        XMLNode nodePmtTpInf = this.nodePmtInf.append("PmtTpInf");
        nodePmtTpInf.append("SvcLvl").append("Cd").value("SEPA");
        nodePmtTpInf.append("LclInstrm").append("Cd").value("CORE");
        nodePmtTpInf.append("SeqTp").value("OOFF");

        this.nodePmtInf.append("ReqdColltnDt").value(SEPAFormatDate.formatDateShort(executionDate));
        this.nodePmtInf.append(this.getType() + "tr")
                .append("Nm").value(this.reciver.getName());

        this.nodePmtInf.append(this.getType() + "trAcct")
                .append("Id")
                .append("IBAN")
                .value(this.reciver.getIBAN());

        if (this.reciver.getBIC() != null) {
            this.nodePmtInf.append(this.getType() + "trAgt")
                    .append("FinInstnId")
                    .append("BIC")
                    .value(this.reciver.getBIC());
        }

        this.nodePmtInf.append("ChrgBr").value("SLEV");

        this.addTransactions();
    }

    @Override
    protected String getType() {
        return "Cd";
    }

    @Override
    protected void addTransactions() {
        XMLNode nodeOthr = this.nodePmtInf.append("CdtrSchmeId")
                .append("Id")
                .append("PrvtId")
                .append("Othr");

        nodeOthr.append("Id").value(this.creditorID);
        nodeOthr.append("SchmeNm")
                .append("Prtry")
                .value("SEPA");

        for (SEPATransaction transaction : this.transactions) {
            XMLNode nodeDrctDbtTxInf = this.nodePmtInf.append("DrctDbtTxInf");

            XMLNode pmtId = nodeDrctDbtTxInf.append("PmtId");
            if (transaction.getId() != null) {
                pmtId.append("InstrId").value(transaction.getId());
            }
            pmtId.append("EndToEndId").value(transaction.getEndToEndId() != null
                    ? transaction.getEndToEndId()
                    : "NOTPROVIDED");

            nodeDrctDbtTxInf.append("InstdAmt")
                    .attr("Ccy", transaction.getCurrency().toString())
                    .value(transaction.getValue().doubleValue());

            XMLNode nodeMndtRltdInf = nodeDrctDbtTxInf.append("DrctDbtTx")
                    .append("MndtRltdInf");

            nodeMndtRltdInf.append("MndtId")
                    .value(transaction.getMandatReference());

            nodeMndtRltdInf.append("DtOfSgntr")
                    .value(SEPAFormatDate.formatDateShort(transaction.getMandatReferenceDate()));

            nodeMndtRltdInf.append("AmdmntInd")
                    .value("false");

            nodeDrctDbtTxInf.append("DbtrAgt")
                    .append("FinInstnId")
                    .append("BIC")
                    .value(transaction.getBankAccount().getBIC());

            XMLNode dbtr = nodeDrctDbtTxInf.append("Dbtr");
            dbtr.append("Nm").value(transaction.getBankAccount().getName());
            XMLNode pstlAdr = dbtr.append("PstlAdr");
            pstlAdr.append("Ctry").value(transaction.getBankAccount().getCountryIso());
            pstlAdr.append("AdrLine").value(transaction.getBankAccount().getAddressLine1());
            pstlAdr.append("AdrLine").value(transaction.getBankAccount().getAddressLine2());

            nodeDrctDbtTxInf.append("DbtrAcct").
                    append("Id")
                    .append("IBAN")
                    .value(transaction.getBankAccount().getIBAN());

            nodeDrctDbtTxInf.append("UltmtDbtr")
                    .append("Nm")
                    .value(transaction.getBankAccount().getName());

            nodeDrctDbtTxInf.append("RmtInf")
                    .append("Ustrd")
                    .value(transaction.getSubject());
        }
    }
}