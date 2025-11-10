package com.github.csucsuy;

import java.time.LocalDate;

// Adatmodell osztály egy szerződés reprezentálására.
public class Contract {

    private int id; // Adatbázis azonosító
    private String szerzodesNeve; // Kötelező
    private LocalDate letrejotte; // Szerződés létrejöttének a dátuma
    private LocalDate vege; // Szerződés lejáratának a dátuma
    private double osszeg; // Szerződés keretösszege
    private String szerzodoFel1; // **Kötelező** szerződő fél1
    private String szerzodoFel2; // **Kötelező** szerződő fél2
    private String dokumentumPath; // A fájl elérési útja

    public Contract(String szerzodesNeve, LocalDate letrejotte, LocalDate vege, 
                    double osszeg, String szerzodoFel1, String szerzodoFel2, 
                    String dokumentumPath) {
        this.szerzodesNeve = szerzodesNeve;
        this.letrejotte = letrejotte;
        this.vege = vege;
        this.osszeg = osszeg;
        this.szerzodoFel1 = szerzodoFel1;
        this.szerzodoFel2 = szerzodoFel2;
        this.dokumentumPath = dokumentumPath;
    }
    
    public Contract() {}

    
    // --- Getterek és Setterek ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSzerzodesNeve() {
        return szerzodesNeve;
    }

    public void setSzerzodesNeve(String szerzodesNeve) {
        this.szerzodesNeve = szerzodesNeve;
    }

    public LocalDate getLetrejotte() {
        return letrejotte;
    }

    public void setLetrejotte(LocalDate letrejotte) {
        this.letrejotte = letrejotte;
    }

    public LocalDate getVege() {
        return vege;
    }

    public void setVege(LocalDate vege) {
        this.vege = vege;
    }

    public double getOsszeg() {
        return osszeg;
    }

    public void setOsszeg(double osszeg) {
        this.osszeg = osszeg;
    }

    public String getSzerzodoFel1() {
        return szerzodoFel1;
    }

    public void setSzerzodoFel1(String szerzodoFel1) {
        this.szerzodoFel1 = szerzodoFel1;
    }

    public String getSzerzodoFel2() {
        return szerzodoFel2;
    }

    public void setSzerzodoFel2(String szerzodoFel2) {
        this.szerzodoFel2 = szerzodoFel2;
    }

    public String getDokumentumPath() {
        return dokumentumPath;
    }

    public void setDokumentumPath(String dokumentumPath) {
        this.dokumentumPath = dokumentumPath;
    }

    // Annak érdekében, hogy a lista olvasható legyen a GUI-n (pl. JList)
    @Override
    public String toString() {
        return this.szerzodesNeve + " (" + this.szerzodoFel1 + ")";
    }
}