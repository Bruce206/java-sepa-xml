package org.java.sepaxml;

import org.java.sepaxml.xml.XMLNode;
import org.java.sepaxml.format.SEPAFormatDate;
import org.java.sepaxml.xml.XMLNode;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public abstract class SEPA {
    protected SEPABankAccount reciver;
    protected List<SEPATransaction> transactions;

    protected Date executionDate;

    protected XMLNode document;
    protected XMLNode nodePmtInf;

    public SEPA(SEPABankAccount reciver, List<SEPATransaction> transactions) {
        this(reciver, transactions, new Date());
    }

    public SEPA(SEPABankAccount reciver, List<SEPATransaction> transactions, Date executionDate) {
        this.reciver = reciver;
        this.transactions = transactions;
        this.executionDate = executionDate;
    }

    protected abstract void build();

    protected abstract String getType();

    protected abstract void addTransactions();

    protected BigDecimal getTransactionVolume() {
        BigDecimal volume = BigDecimal.ZERO;
        for (SEPATransaction transaction : this.transactions) {
            volume = volume.add(transaction.getValue());
        }
        return volume;
    }

    public void write(OutputStream outputStream) {
        this.document.write(outputStream);
    }

    public String toString() {
        return this.document.toString();
    }
}
