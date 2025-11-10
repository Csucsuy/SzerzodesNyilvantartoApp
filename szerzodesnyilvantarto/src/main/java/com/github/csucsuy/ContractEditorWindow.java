package com.github.csucsuy;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Egy modális JDialog ablak új szerződések rögzítésére.
 * Kezeli az adatbevitelt, validálást és a mentést.
 */
public class ContractEditorWindow extends JDialog {

    // --- GUI Komponensek ---
    private JTextField nevField;
    private JTextField letrejotteField; // YYYY-MM-DD
    private JTextField vegeField;       // YYYY-MM-DD
    private JTextField osszegField;
    private JTextField fel1Field;
    private JTextField fel2Field;
    private JTextField filePathField;
    private JButton fileChooserButton;
    private JButton saveButton;
    private JButton cancelButton;
    // Ez tárolja a szerződést, amit éppen szerkesztünk.
    // Ha új szerződést hozunk létre, ez 'null' marad.
    private Contract editingContract;

    // --- Referenciák ---
    private ContractDAO contractDAO;
    private MainAppWindow parentWindow; // A főablak, hogy frissíthessük a listát

    /**
     * Konstruktor ÚJ szerződés létrehozásához.
     */
    public ContractEditorWindow(MainAppWindow parent, ContractDAO dao) {
        // Meghívjuk a másik konstruktort 'null' szerződéssel
        this(parent, dao, null); 
    }

    /**
     * Konstruktor MEGLÉVŐ szerződés módosításához.
     */
    public ContractEditorWindow(MainAppWindow parent, ContractDAO dao, Contract contractToEdit) {
        super(parent, true); // Modális dialógus

        this.parentWindow = parent;
        this.contractDAO = dao;
        this.editingContract = contractToEdit; // Elmentjük a szerkesztendő szerződést

        setSize(500, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        initComponents();
        addListeners();

        // Ha módosításról van szó (editingContract nem null),
        // töltsük fel a mezőket a meglévő adatokkal.
        if (editingContract != null) {
            setTitle("Szerződés módosítása");
            populateFields();
        } else {
            setTitle("Új szerződés rögzítése");
        }
    }

    private void initComponents() {
        // --- 1. Adatbeviteli űrlap (Középen) ---
        // GridBagLayout-ot használunk a szép elrendezéshez
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.insets = new Insets(5, 5, 5, 5); // Térköz
        gbc.anchor = GridBagConstraints.WEST; // Igazítás

        // Címkék (Label)
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Szerződés neve*:"), gbc);

        gbc.gridy = 1;
        formPanel.add(new JLabel("Szerződő fél 1*:"), gbc);

        gbc.gridy = 2;
        formPanel.add(new JLabel("Szerződő fél 2:"), gbc);

        gbc.gridy = 3;
        formPanel.add(new JLabel("Létrejötte (ÉÉÉÉ-HH-NN):"), gbc);

        gbc.gridy = 4;
        formPanel.add(new JLabel("Vége (ÉÉÉÉ-HH-NN):"), gbc);

        gbc.gridy = 5;
        formPanel.add(new JLabel("Szerződés összege:"), gbc);

        gbc.gridy = 6;
        formPanel.add(new JLabel("Dokumentum:"), gbc);

        // Beviteli mezők (TextField)
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL; // Töltse ki a helyet
        gbc.weightx = 1.0; // Szélességben nyúljon
        nevField = new JTextField(20);
        formPanel.add(nevField, gbc);

        gbc.gridy = 1;
        fel1Field = new JTextField(20);
        formPanel.add(fel1Field, gbc);

        gbc.gridy = 2;
        fel2Field = new JTextField(20);
        formPanel.add(fel2Field, gbc);

        gbc.gridy = 3;
        letrejotteField = new JTextField(10);
        formPanel.add(letrejotteField, gbc);

        gbc.gridy = 4;
        vegeField = new JTextField(10);
        formPanel.add(vegeField, gbc);

        gbc.gridy = 5;
        osszegField = new JTextField(10);
        formPanel.add(osszegField, gbc);

        // Fájlválasztó panel
        JPanel filePanel = new JPanel(new BorderLayout(5, 0));
        filePathField = new JTextField(15);
        filePathField.setEditable(false);
        fileChooserButton = new JButton("...");
        filePanel.add(filePathField, BorderLayout.CENTER);
        filePanel.add(fileChooserButton, BorderLayout.EAST);
        
        gbc.gridy = 6;
        formPanel.add(filePanel, gbc);

        add(formPanel, BorderLayout.CENTER);

        // --- 2. Gombok (Alul) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Mentés");
        cancelButton = new JButton("Mégse");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addListeners() {
        // "Mégse" gomb
        cancelButton.addActionListener(e -> dispose()); // Bezárja az ablakot

        // "Fájlválasztó" gomb
        fileChooserButton.addActionListener(e -> openFileChooser());

        // "Mentés" gomb 
        saveButton.addActionListener(e -> parseAndSaveContract());
    }

    /**
     * Megnyitja a fájlválasztó ablakot.
     */
    private void openFileChooser() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void parseAndSaveContract() {
        // 1. Validálás (Kötelező mezők ellenőrzése)
        String nev = nevField.getText();
        String fel1 = fel1Field.getText();

        if (nev.isBlank() || fel1.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "A 'Szerződés neve' és a 'Szerződő fél 1' mező kitöltése kötelező!",
                    "Hiányzó adatok", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Eldöntjük, hogy új objektumot hozunk létre, vagy a meglévőt frissítjük
            Contract contractToSave;
            boolean isUpdate = (editingContract != null);

            if (isUpdate) {
                contractToSave = editingContract; // A meglévő objektumot használjuk
            } else {
                contractToSave = new Contract(); // Új objektum
            }

            // 2. Objektum feltöltése a mezőkből
            contractToSave.setSzerzodesNeve(nev);
            contractToSave.setSzerzodoFel1(fel1);
            contractToSave.setSzerzodoFel2(fel2Field.getText());
            contractToSave.setDokumentumPath(filePathField.getText());

            // Összeg parsolása
            if (!osszegField.getText().isBlank()) {
                contractToSave.setOsszeg(Double.parseDouble(osszegField.getText()));
            } else {
                contractToSave.setOsszeg(0); // Vagy valamilyen alapértelmezett
            }

            // Dátumok parsolása
            if (!letrejotteField.getText().isBlank()) {
                contractToSave.setLetrejotte(LocalDate.parse(letrejotteField.getText()));
            } else {
                contractToSave.setLetrejotte(null);
            }
            if (!vegeField.getText().isBlank()) {
                contractToSave.setVege(LocalDate.parse(vegeField.getText()));
            } else {
                contractToSave.setVege(null);
            }

            // 3. Mentés vagy Frissítés az adatbázisba
            if (isUpdate) {
                contractDAO.updateContract(contractToSave);
            } else {
                contractDAO.addContract(contractToSave);
            }

            // 4. Főablak listájának frissítése
            parentWindow.loadContracts();

            // 5. Ablak bezárása
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Az 'Összeg' mező érvénytelen szám (pl. 150000.50).",
                    "Formátum hiba", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "A dátum formátuma érvénytelen! Helyes formátum: ÉÉÉÉ-HH-NN (pl. 2025-10-30).",
                    "Formátum hiba", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Feltölti a beviteli mezőket a szerkesztendő szerződés adataival.
     */
    private void populateFields() {
        if (editingContract == null) return;

        nevField.setText(editingContract.getSzerzodesNeve());
        fel1Field.setText(editingContract.getSzerzodoFel1());

        // Null-kezelés
        fel2Field.setText(editingContract.getSzerzodoFel2() != null ? editingContract.getSzerzodoFel2() : "");
        osszegField.setText(String.valueOf(editingContract.getOsszeg()));
        filePathField.setText(editingContract.getDokumentumPath() != null ? editingContract.getDokumentumPath() : "");

        if (editingContract.getLetrejotte() != null) {
            letrejotteField.setText(editingContract.getLetrejotte().toString());
        }
        if (editingContract.getVege() != null) {
            vegeField.setText(editingContract.getVege().toString());
        }
    }
}