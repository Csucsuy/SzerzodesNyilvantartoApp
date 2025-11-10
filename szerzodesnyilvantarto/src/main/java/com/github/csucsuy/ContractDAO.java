package com.github.csucsuy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) osztály a Contract objektumok
 * adatbázis-perzisztenciájának kezelésére.
 * Ez végzi a tényleges SQL műveleteket.
 */
public class ContractDAO {

    /**
     * Új szerződés hozzáadása az adatbázishoz.
     * @param contract A menteni kívánt Contract objektum.
     */
    public void addContract(Contract contract) {
        // SQL parancs (INSERT)
        String sql = "INSERT INTO contracts(szerzodes_neve, letrejotte, vege, osszeg, " +
                       "szerzodo_fel_1, szerzodo_fel_2, dokumentum_path) " +
                       "VALUES(?, ?, ?, ?, ?, ?, ?)";

        // Try-with-resources (automatikusan lezárja a kapcsolatot és a statement-et)
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Paraméterek beállítása
            pstmt.setString(1, contract.getSzerzodesNeve());
            
            // Dátumkezelés (lehet null)
            if (contract.getLetrejotte() != null) {
                pstmt.setString(2, contract.getLetrejotte().toString());
            } else {
                pstmt.setNull(2, java.sql.Types.DATE);
            }
            
            if (contract.getVege() != null) {
                pstmt.setString(3, contract.getVege().toString());
            } else {
                pstmt.setNull(3, java.sql.Types.DATE);
            }

            pstmt.setDouble(4, contract.getOsszeg());
            pstmt.setString(5, contract.getSzerzodoFel1());
            pstmt.setString(6, contract.getSzerzodoFel2());
            pstmt.setString(7, contract.getDokumentumPath());

            // SQL parancs futtatása
            pstmt.executeUpdate();
            System.out.println("Szerződés sikeresen mentve: " + contract.getSzerzodesNeve());

        } catch (SQLException e) {
            System.err.println("Hiba a szerződés mentésekor: " + e.getMessage());
        }
    }

    /**
     * Az összes szerződés lekérdezése az adatbázisból.
     * @return Contract objektumok listája.
     */
    public List<Contract> getAllContracts() {
        List<Contract> contracts = new ArrayList<>();
        String sql = "SELECT * FROM contracts ORDER BY szerzodes_neve";

        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Végigmegyünk az eredményeken
            while (rs.next()) {
                Contract contract = new Contract();
                
                // Adatok kiolvasása a ResultSet-ből és beállítása a Contract objektumon
                contract.setId(rs.getInt("id"));
                contract.setSzerzodesNeve(rs.getString("szerzodes_neve"));
                
                // A dátumokat String-ként olvassuk ki és LocalDate-é alakítjuk
                String letrejotteStr = rs.getString("letrejotte");
                if (letrejotteStr != null) {
                    contract.setLetrejotte(LocalDate.parse(letrejotteStr));
                }
                
                String vegeStr = rs.getString("vege");
                if (vegeStr != null) {
                    contract.setVege(LocalDate.parse(vegeStr));
                }
                
                contract.setOsszeg(rs.getDouble("osszeg"));
                contract.setSzerzodoFel1(rs.getString("szerzodo_fel_1"));
                contract.setSzerzodoFel2(rs.getString("szerzodo_fel_2"));
                contract.setDokumentumPath(rs.getString("dokumentum_path"));

                // Hozzáadás a listához
                contracts.add(contract);
            }

        } catch (SQLException e) {
            System.err.println("Hiba a szerződések lekérdezésekor: " + e.getMessage());
        }

        return contracts; // Visszatérés a listával
    }

    /**
     * Töröl egy szerződést az adatbázisból az azonosítója alapján.
     * @param id A törlendő szerződés ID-ja.
     */
    public void deleteContract(int id) {
        String sql = "DELETE FROM contracts WHERE id = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Beállítjuk a törlési feltétel paraméterét (az ID-t)
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate(); // Futtatjuk a törlést
            
            if (affectedRows > 0) {
                System.out.println("Szerződés (ID: " + id + ") sikeresen törölve.");
            } else {
                System.out.println("A törlés nem sikerült, nem található szerződés ezzel az ID-val: " + id);
            }

        } catch (SQLException e) {
            System.err.println("Hiba a szerződés törlésekor: " + e.getMessage());
        }
    }

    /**
     * Frissít egy meglévő szerződést az adatbázisban.
     * Az azonosítás a contract objektumban lévő ID alapján történik.
     * @param contract A módosított Contract objektum.
     */
    public void updateContract(Contract contract) {
        String sql = "UPDATE contracts SET " +
                    "szerzodes_neve = ?, " +
                    "letrejotte = ?, " +
                    "vege = ?, " +
                    "osszeg = ?, " +
                    "szerzodo_fel_1 = ?, " +
                    "szerzodo_fel_2 = ?, " +
                    "dokumentum_path = ? " +
                    "WHERE id = ?"; // Frissítés ID alapján

        try (Connection conn = DatabaseManager.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Paraméterek beállítása
            pstmt.setString(1, contract.getSzerzodesNeve());

            if (contract.getLetrejotte() != null) {
                pstmt.setString(2, contract.getLetrejotte().toString());
            } else {
                pstmt.setNull(2, java.sql.Types.DATE);
            }

            if (contract.getVege() != null) {
                pstmt.setString(3, contract.getVege().toString());
            } else {
                pstmt.setNull(3, java.sql.Types.DATE);
            }

            pstmt.setDouble(4, contract.getOsszeg());
            pstmt.setString(5, contract.getSzerzodoFel1());
            pstmt.setString(6, contract.getSzerzodoFel2());
            pstmt.setString(7, contract.getDokumentumPath());

            // A WHERE feltétel paramétere (az ID)
            pstmt.setInt(8, contract.getId());

            // SQL parancs futtatása
            pstmt.executeUpdate();
            System.out.println("Szerződés sikeresen frissítve: " + contract.getSzerzodesNeve());

        } catch (SQLException e) {
            System.err.println("Hiba a szerződés frissítésekor: " + e.getMessage());
        }
    }
}