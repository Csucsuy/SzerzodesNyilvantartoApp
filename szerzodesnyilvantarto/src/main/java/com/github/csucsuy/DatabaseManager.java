package com.github.csucsuy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

/**
 * Ez az osztály felelős az adatbázis kapcsolatért és 
 * az alapvető struktúra (táblák) létrehozásáért.
 */
public class DatabaseManager {

    // Megadjuk az adatbázis fájl nevét.
    // Ez a projekt gyökerében fog létrejönni.
    private static final String DATABASE_URL = "jdbc:sqlite:szerzodesek.db";

    /**
     * Létrehozza a kapcsolatot az adatbázishoz.
     * Ha a fájl nem létezik, a driver létrehozza.
     * @return Connection objektum
     */
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DATABASE_URL);
            // System.out.println("Sikeres csatlakozás az SQLite adatbázishoz.");
        } catch (SQLException e) {
            System.err.println("Hiba a csatlakozás során: " + e.getMessage());
        }
        return conn;
    }

    /**
     * Létrehozza a szerződések tárolására szolgáló táblát,
     * ha az még nem létezik.
     */
    public static void initializeDatabase() {
        
        // A CREATE TABLE parancs kiegészítve az "IF NOT EXISTS"-el, hogy ne legyen hiba, ha már létezik.
        String sqlCreateTable = "CREATE TABLE IF NOT EXISTS contracts ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "szerzodes_neve VARCHAR(255) NOT NULL,"
            + "letrejotte DATE,"
            + "vege DATE,"
            + "osszeg DECIMAL(15, 2),"
            + "szerzodo_fel_1 VARCHAR(255) NOT NULL,"
            + "szerzodo_fel_2 VARCHAR(255),"
            + "dokumentum_path TEXT"
            + ");";

        // Try-with-resources: automatikusan lezárja a kapcsolatot és a statement-et
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            
            // Tábla létrehozása
            stmt.execute(sqlCreateTable);
            System.out.println("Adatbázis tábla sikeresen létrehozva (vagy már létezett).");

        } catch (SQLException e) {
            System.err.println("Hiba az adatbázis inicializálása során: " + e.getMessage());
        }
    }

    /**
     * A program indításakor hívjuk meg ezt a main metódust a teszteléshez.
     */
    public static void main(String[] args) {
    // 1. Adatbázis inicializálása (tábla létrehozása, ha kell)
        DatabaseManager.initializeDatabase();

        // 2. DAO példányosítása
        ContractDAO dao = new ContractDAO();

        // 3. Teszt: Új szerződés létrehozása
        System.out.println("--- Új szerződés mentése ---");
        Contract ujSzerzodes = new Contract();
        ujSzerzodes.setSzerzodesNeve("Teszt Szerződés 2025");
        ujSzerzodes.setSzerzodoFel1("Java Program Kft.");
        ujSzerzodes.setOsszeg(150000.50);
        ujSzerzodes.setLetrejotte(LocalDate.of(2025, 1, 15));
        ujSzerzodes.setDokumentumPath("C:/temp/teszt.pdf");

        // 4. Mentés az adatbázisba
        dao.addContract(ujSzerzodes);

        // 5. Teszt: Összes szerződés lekérdezése
        System.out.println("\n--- Adatbázisban lévő szerződések ---");
        List<Contract> contracts = dao.getAllContracts();

        if (contracts.isEmpty()) {
            System.out.println("Az adatbázis üres.");
        } else {
            for (Contract c : contracts) {
                // A Contract.java-ban lévő toString() metódus fog meghívódni
                System.out.println(c.toString());
            }
        }
    }
}