package org.sid.rattrapage.commun.constante;

public enum Energie {
    ELEC("E"), GAZ("G");

    private String value;

    private Energie(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }


}
