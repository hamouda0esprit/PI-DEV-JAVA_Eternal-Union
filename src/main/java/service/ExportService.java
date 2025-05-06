package service;

import entite.Examen;
import org.apache.poi.xwpf.usermodel.*;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExportService {
    
    private static final Logger LOGGER = Logger.getLogger(ExportService.class.getName());
    
    /**
     * Exporte une liste d'examens vers un document Word
     * @param examens Liste des examens à exporter
     * @param stage Stage pour l'affichage de la boîte de dialogue
     * @return true si l'exportation a réussi, false sinon
     */
    public boolean exportExamensToWord(List<Examen> examens, Stage stage) {
        if (examens == null || examens.isEmpty()) {
            LOGGER.warning("La liste d'examens est vide ou null, impossible d'exporter");
            return false;
        }
        
        try {
            // Création de la boîte de dialogue pour sélectionner où enregistrer le fichier
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le fichier Word");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Documents Word", "*.docx"));
            fileChooser.setInitialFileName("examens.docx");
            File file = fileChooser.showSaveDialog(stage);
            
            if (file == null) {
                LOGGER.info("L'utilisateur a annulé l'exportation");
                return false; // L'utilisateur a annulé l'opération
            }
            
            LOGGER.info("Début de l'exportation vers " + file.getAbsolutePath());
            
            // Création du document Word
            XWPFDocument document = new XWPFDocument();
            
            // Ajout d'un titre au document
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            titleRun.setText("Liste des Examens");
            titleRun.addBreak();
            
            // Ajout d'une description
            XWPFParagraph descriptionParagraph = document.createParagraph();
            XWPFRun descriptionRun = descriptionParagraph.createRun();
            descriptionRun.setFontSize(12);
            descriptionRun.setText("Ce document contient la liste des examens exportés depuis l'application.");
            descriptionRun.addBreak();
            descriptionRun.addBreak();
            
            // Création d'un tableau pour les examens
            XWPFTable table = document.createTable(examens.size() + 1, 7);
            table.setWidth("100%");
            
            // Définition des en-têtes du tableau
            XWPFTableRow headerRow = table.getRow(0);
            headerRow.getCell(0).setText("Titre");
            headerRow.getCell(1).setText("Matière");
            headerRow.getCell(2).setText("Type");
            headerRow.getCell(3).setText("Date");
            headerRow.getCell(4).setText("Durée (min)");
            headerRow.getCell(5).setText("Nb Essais");
            headerRow.getCell(6).setText("Description");
            
            // Style des en-têtes
            for (int i = 0; i < 7; i++) {
                XWPFTableCell cell = headerRow.getCell(i);
                cell.setColor("3498db");
                XWPFParagraph paragraph = cell.getParagraphs().get(0);
                XWPFRun run = paragraph.createRun();
                run.setBold(true);
                run.setColor("FFFFFF");
            }
            
            // Ajout des données d'examen
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            for (int i = 0; i < examens.size(); i++) {
                Examen examen = examens.get(i);
                XWPFTableRow row = table.getRow(i + 1);
                
                // Vérification des valeurs nulles
                String titre = examen.getTitre() != null ? examen.getTitre() : "";
                String matiere = examen.getMatiere() != null ? examen.getMatiere() : "";
                String type = examen.getType() != null ? examen.getType() : "";
                String date = examen.getDate() != null ? dateFormat.format(examen.getDate()) : "";
                String description = examen.getDescription() != null ? examen.getDescription() : "";
                
                row.getCell(0).setText(titre);
                row.getCell(1).setText(matiere);
                row.getCell(2).setText(type);
                row.getCell(3).setText(date);
                row.getCell(4).setText(String.valueOf(examen.getDuree()));
                row.getCell(5).setText(String.valueOf(examen.getNbrEssai()));
                row.getCell(6).setText(description);
                
                // Alternance de couleurs pour les lignes
                if (i % 2 == 1) {
                    for (int j = 0; j < 7; j++) {
                        row.getCell(j).setColor("f2f2f2");
                    }
                }
            }
            
            // Ajout de la date d'exportation
            XWPFParagraph dateParagraph = document.createParagraph();
            dateParagraph.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun dateRun = dateParagraph.createRun();
            dateRun.setFontSize(10);
            dateRun.setItalic(true);
            dateRun.setText("Document généré le " + dateFormat.format(new java.util.Date()));
            
            // Écriture du document
            try (FileOutputStream out = new FileOutputStream(file)) {
                document.write(out);
                LOGGER.info("Exportation terminée avec succès");
            }
            document.close();
            
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'exportation vers Word", e);
            e.printStackTrace();
            return false;
        }
    }
} 