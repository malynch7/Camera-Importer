import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

public class PhotoSorter {
    
    static String INPUT_DIRECTORY = "input";
    static String OUTPUT_DIRECTORY = "output";
    
    public static void main(String[] args){

        ApplyArgs(args);
        File[] fileList = new File(INPUT_DIRECTORY).listFiles();

        if (fileList == null || fileList.length == 0){
            System.out.println("ERROR: Empty Input Directory " + INPUT_DIRECTORY);
            return;
        }
        System.out.println("Moving " + fileList.length + " files");

        for (File file : fileList){
            LocalDate fileCreationDate = GetFileCreationDate(file);

            if (!MoveFileToSortedDestination(file, fileCreationDate)){
                System.out.println("FAILED: " + file.getAbsolutePath());
            }
        }
    }

    private static boolean MoveFileToSortedDestination(File file, LocalDate fileCreationDate) {
        if(fileCreationDate.getYear() > 1950) {  // 1900 indicates year not retrieved
            int year = fileCreationDate.getYear();
            int month = fileCreationDate.getMonthValue();
            String monthString = String.valueOf(month);

            if (month < 10) {
                monthString = "0" + monthString;
            }
            String outputPath = OUTPUT_DIRECTORY + "\\" + year + "\\" + monthString;

            if (new File(outputPath).mkdirs()) {
                outputPath += "\\" + file.getName();
                return file.renameTo(new File(outputPath));
            }
        }
        return false;
    }

    private static LocalDate GetFileCreationDate(File file) {
        String fileName = file.getName();
        LocalDate localDate = LocalDate.of(1900, 1, 1);

        if(fileName.startsWith("VID")){
            int year = Integer.parseInt(fileName.substring(4, 8));
            int month = Integer.parseInt(fileName.substring(8, 10));
            int day = Integer.parseInt(fileName.substring(10, 12));
            localDate = LocalDate.of(year, month, day);
        }else{
            try {
                Metadata metadata = ImageMetadataReader.readMetadata(file);
                Directory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            } catch (ImageProcessingException | IOException e) {
                System.out.println(e);
            }
        }
        return localDate;
    }

    private static void ApplyArgs(String[] args) {
        switch (args.length){
            case 2:
                OUTPUT_DIRECTORY = args[1];
            case 1:
                INPUT_DIRECTORY = args[0];
        }
        if( !new File(INPUT_DIRECTORY).exists() || !new File(INPUT_DIRECTORY).isDirectory()){
            System.out.println("ERROR: Invalid Input Directory " + INPUT_DIRECTORY);
            System.exit(1);
        }
    }
}
