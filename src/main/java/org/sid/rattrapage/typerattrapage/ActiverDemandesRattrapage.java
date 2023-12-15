package org.sid.rattrapage.typerattrapage;

import lombok.extern.slf4j.Slf4j;
import org.sid.rattrapage.file.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

@Service
@Slf4j
public class ActiverDemandesRattrapage implements IStrategyRattrapage {

    @Autowired
    private FileService fileService;

    @Override
    public File generateFileSQL(String nameTable, MultipartFile inputFileCsv, String outputFileSql) throws IOException {
        HashMap<String, String> mapConcatCorrelationObjetAndDateAbonnement = fileService.readFileActive(inputFileCsv);
        File outputFile = fileService.writeInFileDemandesOK(outputFileSql, mapConcatCorrelationObjetAndDateAbonnement, nameTable);
        if (outputFile != null && outputFile.exists()){
            log.info("Le fichier a été généré avec succès : " + outputFile.getAbsolutePath());
        } else {
            log.error("La génération du fichier a échoué.");
        }
        return outputFile;
    }


}
