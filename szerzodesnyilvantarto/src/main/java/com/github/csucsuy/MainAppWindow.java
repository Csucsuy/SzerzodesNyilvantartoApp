package com.github.csucsuy;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.io.File;
import java.io.IOException;

/**
 * A fő alkalmazás ablak (JFrame).
 * Megjeleníti a szerződések listáját  és a kiválasztott szerződés
 * részleteit.
 */
public class MainAppWindow extends JFrame {

    // --- GUI Komponensek ---
    private JList<Contract> contractList; // A szerződések listája 
    private DefaultListModel<Contract> listModel; // A lista modellje
    private JTextArea detailsArea; // A részletek megjelenítésére 
    private JButton addContractButton;
    private JButton openFileButton;
    private JButton editContractButton; // Módosítás
    private JButton deleteContractButton; // Törlés

    // --- Adatbázis ---
    private ContractDAO contractDAO;

    public MainAppWindow() {
        this.contractDAO = new ContractDAO(); // DAO inicializálása
        
        // Ablak alapbeállításai
        setTitle("Szerződésnyilvántartó");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Középre igazítás

        // GUI komponensek inicializálása
        initComponents();
        
        // Eseménykezelők hozzáadása
        addListeners();

        // Adatok betöltése az adatbázisból a listába
        loadContracts();
    }

    /**
     * Inicializálja és elrendezi a GUI komponenseket.
     */
    private void initComponents() {
        setLayout(new BorderLayout(5, 5)); // Fő elrendezés

        // --- 1. Lista (Bal oldal) ---
        listModel = new DefaultListModel<>();
        contractList = new JList<>(listModel);
        contractList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Egyszerre csak egy választható
        
        // Gördítősáv hozzáadása a listához
        JScrollPane listScrollPane = new JScrollPane(contractList);
        listScrollPane.setPreferredSize(new Dimension(250, 0)); // Szélesség beállítása
        add(listScrollPane, BorderLayout.WEST);

        // --- 2. Részletek (Közép) ---
        detailsArea = new JTextArea();
        detailsArea.setEditable(false); // Ne lehessen szerkeszteni
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        detailsArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane detailsScrollPane = new JScrollPane(detailsArea);
        add(detailsScrollPane, BorderLayout.CENTER);

        // --- 3. Gombok (Alul) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        addContractButton = new JButton("Szerződés hozzáadása");
        openFileButton = new JButton("Dokumentum megnyitása");
        editContractButton = new JButton("Szerződés módosítása");
        deleteContractButton = new JButton("Szerződés törlése");

        // Kezdetben a fájl megnyitása és a törlés/módosítás gomb inaktív
        openFileButton.setEnabled(false);
        editContractButton.setEnabled(false);
        deleteContractButton.setEnabled(false);

        buttonPanel.add(addContractButton);
        buttonPanel.add(editContractButton);
        buttonPanel.add(deleteContractButton);
        buttonPanel.add(openFileButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Hozzáadja az eseménykezelőket a komponensekhez.
     */
    private void addListeners() {
        
        // Lista elem kiválasztásának figyelése 
        contractList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    // A kiválasztott szerződés lekérése
                    Contract selected = contractList.getSelectedValue();
                    
                    if (selected != null) {
                        // Részletek megjelenítése 
                        updateDetailsArea(selected);
                        
                        // Gombok aktiválása/deaktiválása
                        editContractButton.setEnabled(true);
                        deleteContractButton.setEnabled(true);
                        
                        // Fájl megnyitása gomb csak akkor aktív, ha van megadott elérési út
                        openFileButton.setEnabled(selected.getDokumentumPath() != null && 
                                                  !selected.getDokumentumPath().isEmpty());
                    } else {
                        // Ha nincs kiválasztva semmi
                        detailsArea.setText("");
                        openFileButton.setEnabled(false);
                        editContractButton.setEnabled(false);
                        deleteContractButton.setEnabled(false);
                    }
                }
            }
        });

        // "Szerződés hozzáadása" gomb 
        addContractButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Létrehozzuk és láthatóvá tesszük az új szerződés ablakot 
                // Átadjuk neki a főablakot (this) és a DAO-t
                ContractEditorWindow editorWindow = new ContractEditorWindow(MainAppWindow.this, contractDAO);
                editorWindow.setVisible(true);
                
                // A program futása itt megáll, amíg az editorWindow-t
                // be nem zárják (mivel modális).
                // A frissítést (loadContracts()) maga az editorWindow
                // végzi el mentéskor.
            }
        });

        // "Dokumentum megnyitása" gomb
        openFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Contract selected = contractList.getSelectedValue();

                // Ellenőrizzük, van-e kiválasztott elem és van-e hozzá mentett elérési út
                if (selected == null || selected.getDokumentumPath() == null || selected.getDokumentumPath().isEmpty()) {
                    JOptionPane.showMessageDialog(MainAppWindow.this, 
                        "Ehhez a szerződéshez nincs csatolva dokumentum.", 
                        "Hiányzó fájl", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    File file = new File(selected.getDokumentumPath());

                    // Ellenőrizzük, hogy a Desktop funkció támogatott-e
                    if (Desktop.isDesktopSupported()) {
                        if (file.exists()) {
                            // Fájl megnyitása az alapértelmezett programmal
                            Desktop.getDesktop().open(file);
                        } else {
                            JOptionPane.showMessageDialog(MainAppWindow.this, 
                                "A fájl nem található a megadott helyen:\n" + selected.getDokumentumPath(), 
                                "Fájl hiba", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(MainAppWindow.this, 
                            "Az operációs rendszer nem támogatja a fájlok automatikus megnyitását.", 
                            "Kompatibilitási hiba", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (IOException ex) {
                    // Hibakezelés, pl. ha nincs jogosultság a fájl megnyitásához
                    JOptionPane.showMessageDialog(MainAppWindow.this, 
                        "Hiba történt a fájl megnyitása során: " + ex.getMessage(), 
                        "Megnyitási hiba", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // "Szerződés törlése" gomb
        deleteContractButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Contract selected = contractList.getSelectedValue();
                
                // Ellenőrizzük, hogy van-e valami kiválasztva
                if (selected == null) {
                    return; 
                }

                // Megerősítő párbeszédablak megjelenítése
                int response = JOptionPane.showConfirmDialog(
                    MainAppWindow.this, 
                    "Biztosan törölni szeretné a következő szerződést?\n" + selected.getSzerzodesNeve(),
                    "Törlés megerősítése", 
                    JOptionPane.YES_NO_OPTION, // Igen/Nem gombok
                    JOptionPane.WARNING_MESSAGE // Figyelmeztető ikon
                );

                // Ha a felhasználó az 'Igen'-t választotta
                if (response == JOptionPane.YES_OPTION) {
                    try {
                        // 1. Törlés az adatbázisból a DAO segítségével
                        contractDAO.deleteContract(selected.getId());
                        
                        // 2. A lista frissítése a GUI-n
                        // A loadContracts() újratölti az adatokat az adatbázisból,
                        // és mivel a törölt elem már nincs ott, eltűnik a listából.
                        loadContracts();
                        
                        // A detailsArea automatikusan kiürül, mert a
                        // loadContracts() után a list selection listener lefut
                        
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MainAppWindow.this, 
                            "Hiba történt a törlés során: " + ex.getMessage(),
                            "Törlési hiba", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // "Szerződés módosítása" gomb
        editContractButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Contract selected = contractList.getSelectedValue();

                // Ellenőrizzük, hogy van-e valami kiválasztva
                if (selected == null) {
                    return; 
                }

                // Létrehozzuk és láthatóvá tesszük az szerkesztő ablakot
                // Átadjuk neki a főablakot, a DAO-t, és a KIVÁLASZTOTT szerződést
                ContractEditorWindow editorWindow = new ContractEditorWindow(MainAppWindow.this, contractDAO, selected);
                editorWindow.setVisible(true);

                // A lista frissítését az editorWindow végzi, 
                // miután a mentés (frissítés) sikeres volt.
            }
        });
    }

    /**
     * Frissíti a részletek szövegmezőt a kiválasztott szerződés alapján. 
     */
    private void updateDetailsArea(Contract c) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("ID:\t\t%d\n", c.getId()));
        sb.append(String.format("NÉV*:\t\t%s\n", c.getSzerzodesNeve()));
        sb.append(String.format("SZERZŐDŐ 1*:\t%s\n", c.getSzerzodoFel1()));
        sb.append("--------------------------------------\n");
        sb.append(String.format("LÉTREJÖTTE:\t%s\n", (c.getLetrejotte() != null) ? c.getLetrejotte() : "N/A"));
        sb.append(String.format("VÉGE:\t\t%s\n", (c.getVege() != null) ? c.getVege() : "N/A"));
        sb.append(String.format("ÖSSZEG:\t\t%,.2f Ft\n", c.getOsszeg()));
        sb.append(String.format("SZERZŐDŐ 2:\t%s\n", (c.getSzerzodoFel2() != null) ? c.getSzerzodoFel2() : "N/A"));
        sb.append(String.format("DOKUMENTUM:\t%s\n", (c.getDokumentumPath() != null) ? c.getDokumentumPath() : "N/A"));
        
        detailsArea.setText(sb.toString());
        detailsArea.setCaretPosition(0); // Görgessen a tetejére
    }

    /**
     * Betölti (vagy frissíti) a szerződések listáját az adatbázisból.
     */
    public void loadContracts() {
        listModel.clear(); // Lista kiürítése
        List<Contract> contracts = contractDAO.getAllContracts();
        
        for (Contract c : contracts) {
            listModel.addElement(c); // Hozzáadás a listamodellhez
        }
    }

    /**
     * A program belépési pontja.
     */
    public static void main(String[] args) {
        // 1. Adatbázis inicializálása (táblák létrehozása, ha kell)
        DatabaseManager.initializeDatabase();
        
        // 2. A GUI futtatása az Event Dispatch Thread-en (EDT)
        // Ez kötelező a Swing alkalmazásoknál!
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainAppWindow().setVisible(true);
            }
        });
    }
}