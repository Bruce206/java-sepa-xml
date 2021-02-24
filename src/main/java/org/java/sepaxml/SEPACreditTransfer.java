package org.java.sepaxml;

import org.java.sepaxml.format.SEPAFormatDate;
import org.java.sepaxml.xml.XMLNode;
import org.java.sepaxml.xml.XMLNode;

import java.util.Date;
import java.util.List;

public class SEPACreditTransfer extends SEPA {

    public SEPACreditTransfer(SEPABankAccount sender, List<SEPATransaction> transactions) {
        this(sender, transactions, new Date());
    }

    public SEPACreditTransfer(SEPABankAccount sender, List<SEPATransaction> transactions, Date executionDate) {
        super(sender, transactions, executionDate);
        this.build();
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
        this.nodePmtInf.append("PmtMtd").value("TRF");
        this.nodePmtInf.append("BtchBookg").value("true");
        this.nodePmtInf.append("NbOfTxs").value(this.transactions.size());
        this.nodePmtInf.append("CtrlSum").value(this.getTransactionVolume().doubleValue());

        XMLNode nodePmtTpInf = this.nodePmtInf.append("PmtTpInf");
        nodePmtTpInf.append("SvcLvl").append("Cd").value("SEPA");
        // nodePmtTpInf.append("LclInstrm").append("Cd").value("CORE"); // only necessary for PAIN 008 (Lastschrift)
        // nodePmtTpInf.append("SeqTp").append("Cd").value("FRST"); // only necessary for PAIN 008 (Lastschrift)

        this.nodePmtInf.append("ReqdExctnDt").value(SEPAFormatDate.formatDateShort(executionDate));
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

    protected String getRootElementName() {
        return "CstmrCdtTrfInitn";
    }

    protected void attachSchemaInformation(XMLNode document) {
        document.attr("xmlns", "urn:iso:std:iso:20022:tech:xsd:pain.001.001.03")
                .attr("xsi:schemaLocation", "urn:iso:std:iso:20022:tech:xsd:pain.001.001.03 pain.001.001.03.xsd")
                .attr("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
    }

    @Override
    protected String getType() {
        return "Db";
    }

    @Override
    protected void addTransactions() {
        for (SEPATransaction transaction : this.transactions) {
            XMLNode nodeCdtTrfTxInf = this.nodePmtInf.append("CdtTrfTxInf");

            nodeCdtTrfTxInf.append("PmtId")
                    .append("EndToEndId").value(transaction.getEndToEndId());

            nodeCdtTrfTxInf.append("Amt").
                    append("InstdAmt")
                    .attr("Ccy", transaction.getCurrency().toString())
                    .value(transaction.getValue().doubleValue());

            nodeCdtTrfTxInf.append("CdtrAgt")
                    .append("FinInstnId").append("BIC")
                    .value(transaction.getBankAccount().getBIC());

            XMLNode cdtr = nodeCdtTrfTxInf.append("Cdtr");
            cdtr.append("Nm").value(transaction.getBankAccount().getName());
            XMLNode pstlAdr = cdtr.append("PstlAdr");
            pstlAdr.append("Ctry").value(transaction.getBankAccount().getCountryIso());
            pstlAdr.append("AdrLine").value(transaction.getBankAccount().getAddressLine1());
            pstlAdr.append("AdrLine").value(transaction.getBankAccount().getAddressLine2());

            nodeCdtTrfTxInf.append("CdtrAcct")
                    .append("Id").append("IBAN")
                    .value(transaction.getBankAccount().getIBAN());

            nodeCdtTrfTxInf.append("RmtInf")
                    .append("Ustrd")
                    .value(transaction.getSubject());

            if (transaction.getRemittance() != null) {
                nodeCdtTrfTxInf.append("RmtInf")
                        .append("Ustrd")
                        .value(transaction.getRemittance());
            } else {
                nodeCdtTrfTxInf.append("RmtInf")
                        .append("Ustrd")
                        .value(transaction.getSubject());
            }
        }
    }
}
