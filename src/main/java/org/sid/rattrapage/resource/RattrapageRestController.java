package org.sid.rattrapage.resource;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sid.rattrapage.typerattrapage.IStrategyRattrapage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/rattrapages")
@Slf4j
public class RattrapageRestController {

    @Autowired
    @Qualifier("activerDemandesRattrapage")
    private IStrategyRattrapage activerDemandesRattrapage;

    @Autowired
    @Qualifier("echecDemandesRattrapage")
    private IStrategyRattrapage echecDemandesRattrapage;


    @PostMapping(value = "/active")
    public ResponseEntity<String> generateRequestActive(@RequestParam("file") @Valid MultipartFile inputFile, @RequestParam("outputFile") @NotEmpty @NotBlank @NonNull String outputFile,
                                                        @RequestParam("nameTable") @NotBlank String nameTable) throws IOException {
        /***Validation personnalisée pour les params***/
        // Validation : Le fichier ne peut pas être vide et doit avoir l'extension .csv
        if (inputFile == null || inputFile.isEmpty() || !inputFile.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            return ResponseEntity.badRequest().body("Le fichier doit être non nul, non vide et avoir l'extension .csv.");
        } else if (StringUtils.isEmpty(outputFile)) {
            return ResponseEntity.badRequest().body("Le paramètre 'outputFile' ne peut pas être vide.");
        } else if (StringUtils.isEmpty(nameTable)) {
            return ResponseEntity.badRequest().body("Le paramètre 'nameTable' ne peut pas être vide.");
        } else {
            log.info("Génération fichier sql pour les demandes OK");
            try {
                File outputFileSql = activerDemandesRattrapage.generateFileSQL(nameTable, inputFile, outputFile);
                return ResponseEntity.ok("Fichier " + inputFile.getOriginalFilename() + " traité avec succès - fichier -> "+outputFileSql.getName()+" est généré");
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body("Échec du traitement du fichier : "+inputFile.getOriginalFilename()+".");
            }
        }
    }


    @PostMapping(value = "/echec")
    public ResponseEntity<String> generateRequestEchec(@RequestParam("file") @Valid MultipartFile inputFile, @RequestParam("outputFile") @NotNull @NotBlank @NotEmpty String outputFile,
                                                        @RequestParam("nameTable") @NotNull @NotBlank @NotEmpty String nameTable) throws IOException {
        /***Validation personnalisée pour les params***/
        // Validation : Le fichier ne peut pas être vide et doit avoir l'extension .csv
        if (inputFile == null || inputFile.isEmpty() || !inputFile.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            return ResponseEntity.badRequest().body("Le fichier doit être non nul, non vide et avoir l'extension .csv.");
        } else if (StringUtils.isEmpty(outputFile)) {
            return ResponseEntity.badRequest().body("Le paramètre 'outputFile' ne peut pas être vide.");
        } else if (StringUtils.isEmpty(nameTable)) {
            return ResponseEntity.badRequest().body("Le paramètre 'nameTable' ne peut pas être vide.");
        } else {
            log.info("Génération fichier sql pour les demandes KO");
            try {
                File outputFileSql = echecDemandesRattrapage.generateFileSQL(nameTable, inputFile, outputFile);
                return ResponseEntity.ok("Fichier " + inputFile.getOriginalFilename() + " traité avec succès - fichier -> "+outputFileSql.getName()+" est généré");
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body("Échec du traitement du fichier : "+inputFile.getOriginalFilename()+".");
            }
        }
    }


}
