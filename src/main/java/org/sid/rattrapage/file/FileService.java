package org.sid.rattrapage.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;

@Service
@Slf4j
public class FileService {

    private static final String OUTPUT_FILE_EXTENTION = ".sql";
    private static final String SPLITOR_CSV = ";";
    private static final String SQL_REQUEST_SUFFIX = ";\n";

    /********Passer les demandes à Active********/
    public HashMap readFileActive(MultipartFile inputFileCsv) throws IOException {
        HashMap<String, String> mapConcatCorrelationObjetAndDateAbonnement = new HashMap<>();

     //   File file = new File(inputFileCsv);
        File file = convertToFile(inputFileCsv);
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);

            // Lire la première ligne (ligne d'en-tête) et l'ignorer
            String headerLine = br.readLine();

            String line = "";
            String[] tab;
            String concatCorrelationObjet;
            String dateDebutAbonnement;

            // Le pattern de format pour la date initiale (entré)
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
            // Le pattern de format pour le résultat final
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            while ((line = br.readLine()) !=null){
                tab = line.split(SPLITOR_CSV);

                concatCorrelationObjet = tab[0].replaceAll(" ", "");

                dateDebutAbonnement = tab[1];

                // Conversion de la date string en objet Date
                Date dateParse = inputFormat.parse(dateDebutAbonnement);
                // Ajout de l'heure "23:00:00" à la date
                dateParse.setHours(23);
                dateParse.setMinutes(0);
                dateParse.setSeconds(0);
                // Formatage de la date avec l'heure ajoutée
                String formattedDateDebutAbonnement = outputFormat.format(dateParse);

                mapConcatCorrelationObjetAndDateAbonnement.put(concatCorrelationObjet, formattedDateDebutAbonnement);

            }
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return mapConcatCorrelationObjetAndDateAbonnement;
    }



    public File writeInFileDemandesOK(String outputFileSql, HashMap<String, String> mapConcatCorrelationObjetAndDateAbonnement, String nameTable){
        File file = new File(outputFileSql+"output_requetes_active_"+java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"))+OUTPUT_FILE_EXTENTION);
        FileWriter myWriter;
        try {
            if (file.createNewFile()) {
                log.info("File created: "+file.getName());
                myWriter = new FileWriter(file);

                mapConcatCorrelationObjetAndDateAbonnement.forEach((concat, dateAbonnement) -> {
                    try {
                        String sqlRequest = "UPDATE "+nameTable+" SET ETAPE = 'FREDI_MS', STATUT_ETAPE = 'OK', DATE_DERNIERE_MAJ = NOW(), DATE_DEBUT_ABONNEMENT = '"+dateAbonnement+"'" +
                                " WHERE STATUT_DEMANDE IN ('RENOUVELLEMENT_EN_COURS', 'ACTIVATION_EN_COURS') AND CONCAT_CORRELATION_OBJET = '"+concat+"'";
                        myWriter.write(sqlRequest);
                        myWriter.write(SQL_REQUEST_SUFFIX);
                    } catch (IOException e) {
                        e.printStackTrace();
                        log.error(e.getMessage());
                    }
                });

                myWriter.close();

                log.info("Successfully wrote to the file. \n ---------");
                return file;

            } else {
                log.warn("File already exists. \n -----------");
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return null;
        }

        return file;
    }





    /****** Passer les demandes à Echec Activation/Renouvellement ******/
    public HashMap readFileEchec(MultipartFile inputFileCsv) throws IOException {
        HashMap<String, String> mapConcatCorrelationObjetAndCodeStatut = new HashMap<>();

        File file = convertToFile(inputFileCsv);
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);

            // Lire la première ligne (ligne d'en-tête) et l'ignorer
            String headerLine = br.readLine();

            String line = "";
            String[] tab;
            String concatCorrelationObjet;
            String codeStatut;

            while ((line = br.readLine()) !=null){
                tab = line.split(SPLITOR_CSV);

                concatCorrelationObjet = tab[0].replaceAll(" ", "");

                codeStatut = tab[1].replaceAll(" ", "");

                mapConcatCorrelationObjetAndCodeStatut.put(concatCorrelationObjet, codeStatut);

            }
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mapConcatCorrelationObjetAndCodeStatut;
    }


    public File writeInFileDemandesKO(String outputFileSql, HashMap<String, String> mapConcatCorrelationObjetAndCodeStatut, String nameTable) {
        File file = new File(outputFileSql+"output_requetes_echec_"+java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"))+OUTPUT_FILE_EXTENTION);
        FileWriter myWriter;
        try {
            if (file.createNewFile()) {
                log.info("File created: " + file.getName());
                myWriter = new FileWriter(file);

                mapConcatCorrelationObjetAndCodeStatut.forEach((concat, codeStatut) -> {
                    try {
                        String sqlRequest = "UPDATE "+nameTable+" SET ETAPE = 'FREDI_MS', STATUT_ETAPE = 'KO', NBR_REJEU_FONC = 2, DATE_DERNIERE_MAJ = NOW(), CODE_ERREUR_TECH_CR = '"+codeStatut+"' ," +
                                "TEMPLATE_MAIL_ERREUR = CASE WHEN STATUT_DEMANDE = 'ACTIVATION_EN_COURS' THEN 'M_ACCORD_ET_PURGE_KO' ELSE TEMPLATE_MAIL_ERREUR END,  MAIL_ERREUR = CASE WHEN STATUT_DEMANDE = 'ACTIVATION_EN_COURS' THEN 'MAIL_A_ENVOYER' ELSE MAIL_ERREUR END " +
                                "WHERE STATUT_DEMANDE IN ('RENOUVELLEMENT_EN_COURS', 'ACTIVATION_EN_COURS') AND DATE_DEBUT_ABONNEMENT IS NULL AND CONCAT_CORRELATION_OBJET = '"+concat+"'";
                        myWriter.write(sqlRequest);
                        myWriter.write(SQL_REQUEST_SUFFIX);
                    } catch (IOException e) {
                        e.printStackTrace();
                        log.error(e.getMessage());
                    }
                });

                myWriter.close();

                log.info("Successfully wrote to the file. \n ---------");
                return file;

            } else {
                log.warn("File already exists. \n -----------");
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return null;
        }

        return file;
    }



    public static File convertToFile(MultipartFile multipartFile) throws IOException {
        // Crée un fichier temporaire
        File tempFile = File.createTempFile("temp", null);

        // Copie le contenu du MultipartFile dans le fichier temporaire
        try (var inputStream = multipartFile.getInputStream()) {
            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        return tempFile;
    }


}
