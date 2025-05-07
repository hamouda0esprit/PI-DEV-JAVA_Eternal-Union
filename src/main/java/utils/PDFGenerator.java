package utils;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import entite.Examen;
import entite.Question;
import entite.Reponse;
import service.QuestionService;
import service.ReponseService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.awt.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

/**
 * Utilitaire pour générer des PDF à partir des données d'examen
 */
public class PDFGenerator {
    
    /**
     * Génère un PDF pour un examen spécifique
     * @param examen L'examen à exporter
     * @param stage La fenêtre parent pour le dialogue de sauvegarde
     * @return true si le PDF a été généré avec succès, false sinon
     */
    public static boolean generateExamPDF(Examen examen, Stage stage) {
        try {
            // Configurer le sélecteur de fichier
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le PDF");
            fileChooser.setInitialFileName(examen.getTitre().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
            );
            
            // Afficher le dialogue de sauvegarde
            File file = fileChooser.showSaveDialog(stage);
            if (file == null) {
                return false; // L'utilisateur a annulé
            }
            
            // Créer le document PDF
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(file));
            
            document.open();
            
            // Ajouter les metadata
            document.addTitle("Examen: " + examen.getTitre());
            document.addAuthor("Système d'examens");
            document.addCreator("Application JavaFX");
            
            // Style pour les titres
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.DARK_GRAY);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font smallBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            
            // En-tête
            Paragraph title = new Paragraph("EXAMEN: " + examen.getTitre(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Tableau des informations générales de l'examen
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(10);
            infoTable.setSpacingAfter(20);
            
            // Formater la date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String dateStr = examen.getDate() != null ? dateFormat.format(examen.getDate()) : "Non définie";
            
            // Ajouter les informations de l'examen
            addTableRow(infoTable, "Matière:", examen.getMatiere(), smallBoldFont, normalFont);
            addTableRow(infoTable, "Type:", examen.getType(), smallBoldFont, normalFont);
            addTableRow(infoTable, "Date:", dateStr, smallBoldFont, normalFont);
            addTableRow(infoTable, "Durée:", examen.getDuree() + " minutes", smallBoldFont, normalFont);
            addTableRow(infoTable, "Nombre d'essais:", String.valueOf(examen.getNbrEssai()), smallBoldFont, normalFont);
            
            document.add(infoTable);
            
            // Description
            Paragraph descTitle = new Paragraph("Description:", subtitleFont);
            descTitle.setSpacingAfter(10);
            document.add(descTitle);
            
            Paragraph description = new Paragraph(examen.getDescription(), normalFont);
            description.setSpacingAfter(20);
            document.add(description);
            
            // Récupérer les questions et réponses
            QuestionService questionService = new QuestionService();
            ReponseService reponseService = new ReponseService();
            
            List<Question> questions = questionService.recupererParExamen(examen.getId());
            
            if (questions.isEmpty()) {
                Paragraph noQuestions = new Paragraph("Aucune question n'a été définie pour cet examen.", normalFont);
                document.add(noQuestions);
            } else {
                // Titre de la section Questions
                Paragraph questionsTitle = new Paragraph("Questions:", subtitleFont);
                questionsTitle.setSpacingAfter(10);
                document.add(questionsTitle);
                
                // Ajouter chaque question
                for (int i = 0; i < questions.size(); i++) {
                    Question question = questions.get(i);
                    
                    // Texte de la question
                    Paragraph questionText = new Paragraph((i + 1) + ". " + question.getQuestion() + 
                                                         " (" + question.getNbr_points() + " points)", smallBoldFont);
                    questionText.setSpacingBefore(10);
                    questionText.setSpacingAfter(5);
                    document.add(questionText);
                    
                    // Récupérer les réponses pour cette question
                    List<Reponse> reponses = reponseService.recupererParQuestion(question.getId());
                    if (!reponses.isEmpty()) {
                        // Créer une liste pour les réponses
                        com.lowagie.text.List responseList = new com.lowagie.text.List(false, 10);
                        responseList.setListSymbol("□ ");
                        
                        for (Reponse reponse : reponses) {
                            // Ne pas indiquer quelle réponse est correcte dans le PDF exporté
                            ListItem item = new ListItem(reponse.getReponse(), normalFont);
                            responseList.add(item);
                        }
                        
                        document.add(responseList);
                    } else {
                        Paragraph noResponses = new Paragraph("   Aucune réponse définie.", normalFont);
                        document.add(noResponses);
                    }
                }
            }
            
            // Ajouter un pied de page
            Paragraph footer = new Paragraph("Document généré automatiquement",
                                           FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY));
            footer.setSpacingBefore(30);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);
            
            document.close();
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Ajoute une ligne à un tableau PDF
     */
    private static void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setBackgroundColor(new Color(240, 240, 240));
        labelCell.setPadding(5);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
} 