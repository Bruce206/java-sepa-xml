package org.java.sepaxml;

import lombok.Getter;
import lombok.Setter;
import org.java.sepaxml.validator.exception.SEPAValidatorIBANFormatException;

@Setter
@Getter
public class SEPABankAccount {

    private String IBAN;
    private String BIC;
    private String name;
    private String countryIso = "";
    private String addressLine1 = "";
    private String addressLine2 = "";

    public SEPABankAccount(String IBAN, String name) {
        this(IBAN, null, name);
    }

    public SEPABankAccount(String IBAN, String BIC, String name) {
        this.IBAN = IBAN;
        this.BIC = BIC;
        this.name = name;
    }

    public SEPABankAccount(String IBAN, String BIC, String name, String countryIso, String addressLine1, String addressLine2) {
        this(IBAN, BIC, name);
        this.countryIso = countryIso;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
    }
}
