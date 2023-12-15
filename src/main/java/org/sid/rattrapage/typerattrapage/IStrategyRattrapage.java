package org.sid.rattrapage.typerattrapage;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public interface IStrategyRattrapage {

    public File generateFileSQL(String nameTable, MultipartFile inputFileCsv, String outputFileSql) throws IOException;

}
